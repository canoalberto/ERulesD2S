#include "parameters.h"
#include "functions.cu"

SEMAPHORE wait_sem[MAX_THREADS],post_sem[MAX_THREADS];
CUTThread threadID[MAX_THREADS];
Plan plan[MAX_THREADS];

int numClasses, numberAttributes, currentnumberInstances, maxnumberInstances, maxnumberInstances_A, numThreads, populationSize, classifiedClass;
bool evaluate = false;
bool copy = false;
float *h_instancesData;
jobject algorithm;

void releaseGPU(JNIEnv *env, jobject obj)
{
	evaluate = false;
	copy = false;
	
    // Wake up threads to finish them
	for(int i = 0; i < numThreads; i++)
		SEM_POST (&wait_sem[i]);
		
	free(h_instancesData);
	
	cutWaitForThreads(threadID, numThreads);

	#if _WIN32
	for(int i = 0; i < numThreads; i++)
	{
		CloseHandle(wait_sem[i]);
		CloseHandle(post_sem[i]);
	}
	#endif
}

static void Get_VM(JavaVM** jvm_p, JNIEnv** env_p) {

	JavaVM jvmBuffer;
	JavaVM* vmBuf = &jvmBuffer;
	jsize jvmTotalNumberFound = 0;  
	jint resCheckVM = JNI_GetCreatedJavaVMs(&vmBuf, 1, &jvmTotalNumberFound);
	
	if (jvmTotalNumberFound < 1)
	{
		fprintf(stderr, "No JVM found\n");
		exit(0);
	}
	*jvm_p = vmBuf;

	(*jvm_p)->AttachCurrentThread((void**)env_p, NULL);
}

__global__ void coverageKernel(unsigned char* result, float* instancesData, int* rulesConsequent, int currentnumberInstances, int maxnumberInstances_A, int numberAttributes, float* expressions) 
{
   int instance = blockDim.y * blockIdx.y + threadIdx.y;
   int resultMemPosition = blockIdx.x * maxnumberInstances_A + instance;
   
   if(instance < currentnumberInstances)
   {
      if(covers(&expressions[MAX_EXPR_LEN * blockIdx.x], instance, instancesData, maxnumberInstances_A))
      {
         if(rulesConsequent[blockIdx.x] == instancesData[(numberAttributes-1)*maxnumberInstances_A + instance])
            result[resultMemPosition] = 0; // TRUE POSITIVE
         else
            result[resultMemPosition] = 2; // FALSE POSITIVE
      }
      else
      {
         if(rulesConsequent[blockIdx.x] != instancesData[(numberAttributes-1)*maxnumberInstances_A + instance])
            result[resultMemPosition] = 1; // TRUE NEGATIVE
         else
            result[resultMemPosition] = 3; // FALSE NEGATIVE   
      }
   }
}

__global__ void fitnessKernel(unsigned char* result, int currentnumberInstances, int maxnumberInstances_A, float* fitness) 
{
   __shared__ int MC[512];
   
   MC[threadIdx.y] = 0;
   MC[threadIdx.y+128] = 0;
   MC[threadIdx.y+256] = 0;
   MC[threadIdx.y+384] = 0;
   
   int base = blockIdx.x*maxnumberInstances_A + threadIdx.y;
   int top =  blockIdx.x*maxnumberInstances_A + currentnumberInstances - base;
   
   // Performs the reduction of the thread corresponding values
   for(int i = 0; i < top; i+=128)
   {
      MC[threadIdx.y*4 + result[base + i]]++;
   }
   
   __syncthreads();
   
    // Calculates the final amount
   if(threadIdx.y < 4)
   {
      for(int i = 4; i < 512; i+=4)
      {
         MC[0] += MC[i];     // Number of true positives
         MC[1] += MC[i+1];   // Number of true negatives
         MC[2] += MC[i+2];   // Number of false positives
         MC[3] += MC[i+3];   // Number of false negatives
      }
   }
   
   if(threadIdx.y == 0)
   {
      int tp = MC[0], tn = MC[1], fp = MC[2], fn = MC[3];
      
      float se, sp;

	  if(tp + fn == 0)
		se = 1.0f;
	  else
		se = tp / (float) (tp + fn);

	  if(tn + fp == 0)
		sp = 1.0f;
	  else
		sp = tn / (float) (tn + fp);

      // Set the fitness to the individual
      fitness[blockIdx.x] = se * sp;
   }
}

CUT_THREADPROC gpuThreadTan(Plan *plan)
{
	cudaError_t err;
	
	cudaSetDevice(plan->device);
	
	int threadPopulationSize;
	
	float *d_rules, *h_rules;
	float *d_instancesData;
	int *d_rulesConsequent;
	int *h_rulesConsequent;
	
	float *h_fitness, *d_fitness;
	
	unsigned char* d_result;
	
	JNIEnv* env;
	JavaVM* jvm;
	
	// Signal: thread is ready to evaluate
	SEM_POST(&post_sem[plan->thread]);
	
	cudaMalloc((void**) &d_rules, BLOCK_SIZE_RULES * MAX_EXPR_LEN * sizeof(float));
	cudaMalloc((void**) &d_instancesData, numberAttributes * maxnumberInstances_A * sizeof(float));
	cudaMalloc((void**) &d_rulesConsequent, BLOCK_SIZE_RULES * sizeof(int));
	cudaMalloc((void**) &d_fitness, BLOCK_SIZE_RULES * sizeof(float));
    cudaMalloc((void**) &d_result, BLOCK_SIZE_RULES * maxnumberInstances_A * sizeof(unsigned char));
    
	cudaMallocHost((void**)&h_rules, BLOCK_SIZE_RULES *  MAX_EXPR_LEN * sizeof(float));
	cudaMallocHost((void**)&h_rulesConsequent, BLOCK_SIZE_RULES *  sizeof(int));
	cudaMallocHost((void**)&h_fitness, BLOCK_SIZE_RULES * sizeof(float));
	
	err = cudaGetLastError();
	
    if(cudaSuccess != err)
    {
    	printf( "Cuda error: %s.\n",  cudaGetErrorString( err) );
    	exit(0);
    }
    
	Get_VM(&jvm, &env);
	
	dim3 threads_coverage(1, THREADS_EVAL_BLOCK);
	dim3 threads_fitness(1, 128);
	
	do
	{
		// Wait until evaluation is required
		SEM_WAIT (&wait_sem[plan->thread]);
		
		if(evaluate)
		{
			// Get the methods from Java
			jclass cls = env->GetObjectClass(algorithm);
			jmethodID getAntecedent = env->GetMethodID(cls, "getAntecedent", "(I)[F");
			jmethodID setFitness = env->GetMethodID(cls, "setFitness", "(IF)V");
			jmethodID getConsequent = env->GetMethodID(cls, "getConsequent", "(I)I");
		
			// Calculate the thread population size
			threadPopulationSize = (int)ceil(populationSize/(float)numThreads);
			
			// If population overflow, recalculate the thread actual population size
			if((plan->thread + 1) * threadPopulationSize > populationSize)
			{
				if((threadPopulationSize = populationSize - threadPopulationSize * plan->thread) < 0)
					threadPopulationSize = 0;
			}
			if(threadPopulationSize > 0)
			{
				// Calculate the base index of the individual for this thread
				int base = plan->thread * (int)ceil(populationSize/(float)numThreads);
				
				int numberIndstoEvaluate = BLOCK_SIZE_RULES;	
				
				// Population is evaluated using blocks of BLOCK_SIZE_RULES individuals
				for(int j = 0; j < threadPopulationSize; j += BLOCK_SIZE_RULES)
				{
					// If the last block size is smaller, fix the block size to the number of the rest of individuals 
					if(j+BLOCK_SIZE_RULES > threadPopulationSize)
						numberIndstoEvaluate = threadPopulationSize - j;
					
					memset(h_rules, 0, BLOCK_SIZE_RULES * MAX_EXPR_LEN * sizeof(float));
					
					for(int i = 0; i < numberIndstoEvaluate; i++)
					{
						jfloatArray antecedent = (jfloatArray) env->CallObjectMethod(algorithm, getAntecedent, base+j+i); 
  						
  						float *antecedentElements = (float*) env->GetFloatArrayElements(antecedent, 0);
  						
					    memcpy(&h_rules[i*MAX_EXPR_LEN], &antecedentElements[1], (antecedentElements[0] * sizeof(float)));
					    
					    env->ReleaseFloatArrayElements(antecedent, antecedentElements, 0);
					    env->DeleteLocalRef(antecedent);
					    
						h_rulesConsequent[i] = env->CallIntMethod(algorithm, getConsequent, base+j+i);
					}
					
					cudaMemcpy(d_rules, h_rules, BLOCK_SIZE_RULES * MAX_EXPR_LEN * sizeof(float), cudaMemcpyHostToDevice);
					cudaMemcpy(d_rulesConsequent , h_rulesConsequent, BLOCK_SIZE_RULES * sizeof(int), cudaMemcpyHostToDevice);
					
					// Setup evaluation grid size	
					dim3 grid_coverage(numberIndstoEvaluate, (int)ceil(currentnumberInstances/(float)THREADS_EVAL_BLOCK));
					dim3 grid_fitness(numberIndstoEvaluate, 1);
					
					coverageKernel <<< grid_coverage, threads_coverage >>> (d_result, d_instancesData, d_rulesConsequent, currentnumberInstances, maxnumberInstances_A, numberAttributes, d_rules);
					
					fitnessKernel <<< grid_fitness, threads_fitness >>> (d_result, currentnumberInstances, maxnumberInstances_A, d_fitness);
					
	                cudaMemcpy(h_fitness, d_fitness, numberIndstoEvaluate * sizeof(float), cudaMemcpyDeviceToHost );
                
	                for(int i = 0; i < numberIndstoEvaluate; i++)
	               		env->CallVoidMethod(algorithm, setFitness, base + j + i, h_fitness[i]);
				}
			}
		}
		else if(copy)
		{
		    // Copy instances data and classes to the GPU
			cudaMemcpy(d_instancesData, h_instancesData, numberAttributes*maxnumberInstances_A*sizeof(float), cudaMemcpyHostToDevice );
		}
		else
		{
			 // Algorithm finished, free dynamic memory
			 cudaFree(d_rules);
	         cudaFree(d_instancesData);
	         cudaFree(d_rulesConsequent);
	         cudaFree(d_fitness);
    		 cudaFree(d_result);
	         
	         cudaFreeHost(h_rules);
	         cudaFreeHost(h_rulesConsequent);
	         cudaFreeHost(h_fitness);
		}
		
		// Evaluation finished
		SEM_POST(&post_sem[plan->thread]);
		
	}while(evaluate || copy);

	jvm->DetachCurrentThread();
	
	CUT_THREADEND;
}

JNIEXPORT void JNICALL
Java_net_sf_jclec_problem_classification_evolutionarylearner_RuleEvaluatorGPU_releaseGPU(JNIEnv *env, jobject obj)
{
	releaseGPU(env,obj);
}

JNIEXPORT void JNICALL
Java_net_sf_jclec_problem_classification_evolutionarylearner_RuleEvaluatorGPU_allocateMemoryGPU(JNIEnv *env, jobject obj, jint popSize, jint jmaxnumberInstances, jint jnumberAttributes, jint jnumClasses, jobject jalgorithm)
{
	algorithm = jalgorithm;
	numberAttributes = jnumberAttributes;
	numClasses = jnumClasses;
	maxnumberInstances = jmaxnumberInstances;
	maxnumberInstances_A = ceil(maxnumberInstances/(float)ALIGNMENT)*ALIGNMENT;
	
	h_instancesData = (float*)malloc(numberAttributes*maxnumberInstances_A*sizeof(float));

	int deviceCount;
	cudaGetDeviceCount(&deviceCount);
	int deviceCountComputeCapability = 0;
	int deviceID[16];
	
	for (int dev = 0; dev < deviceCount; dev++)
    {
        cudaSetDevice(dev);
        cudaDeviceProp deviceProp;
        cudaGetDeviceProperties(&deviceProp, dev);
        
        //if((deviceProp.major == 3 && deviceProp.minor == 5) || (deviceProp.major == 5 && deviceProp.minor == 0) || (deviceProp.major == 5 && deviceProp.minor == 2) || (deviceProp.major == 6 && deviceProp.minor == 0) || (deviceProp.major == 6 && deviceProp.minor == 1))
        {
       		deviceID[deviceCountComputeCapability] = dev;
        	deviceCountComputeCapability++;
        }
    }
    
    if(deviceCountComputeCapability < 1)
	{
		fprintf(stderr, "CUDA devices count is %d\n", deviceCount);
		exit(0);
	}
    
    numThreads = deviceCountComputeCapability;
	
	// Set up semaphores
	for(int i = 0; i < numThreads; i++)
	{
		SEM_INIT (&wait_sem[i], 0);
		SEM_INIT (&post_sem[i], 0);

		plan[i].thread = i;
		plan[i].device = deviceID[i];
		plan[i].size = (int)ceil(popSize/(float)numThreads);
	}
	
	for(int i = 0; i < numThreads; i++)
		threadID[i] = cutStartThread((CUT_THREADROUTINE) gpuThreadTan, (void *)&plan[i]);

	// SIGNAL: threads ready to evaluate
	for(int i = 0; i < numThreads; i++)
		SEM_WAIT (&post_sem[i]);
}

JNIEXPORT void JNICALL
Java_net_sf_jclec_problem_classification_evolutionarylearner_RuleEvaluatorGPU_copyDatasetGPU(JNIEnv *env, jobject obj, jint jcurrentnumberInstances, jobject jalgorithm)
{
	algorithm = jalgorithm;
	currentnumberInstances = jcurrentnumberInstances;
	jclass cls = env->GetObjectClass(algorithm);
	jmethodID getValue = env->GetMethodID(cls, "getValue", "(II)F");
	
	// Copy dataset data from Java
	for(int i = 0; i < jcurrentnumberInstances; i++)
		for(int j = 0; j < numberAttributes; j++)
			h_instancesData[j*maxnumberInstances_A+i] = env->CallFloatMethod(algorithm,getValue,i,j);
			
	copy = true;
	evaluate = false;
	
	// SIGNAL: wake up threads to copy
	for(int i = 0; i < numThreads; i++)
       SEM_POST (&wait_sem[i]);
	
	// Wait until threads finish
	for(int i = 0; i < numThreads; i++)
	   SEM_WAIT (&post_sem[i]);
}

JNIEXPORT void JNICALL
Java_net_sf_jclec_problem_classification_evolutionarylearner_RuleEvaluatorGPU_evaluateGPU(JNIEnv *env, jobject obj, jint size, jobject jalgorithm)
{
	algorithm = jalgorithm;
	populationSize = size;
	
	copy = false;
	evaluate = true;

	// SIGNAL: wake up threads to evaluate
	for(int i = 0; i < numThreads && i < size; i++)
       SEM_POST (&wait_sem[i]);
	
	// Wait until threads finish
	for(int i = 0; i < numThreads && i < size; i++)
	   SEM_WAIT (&post_sem[i]);
}
