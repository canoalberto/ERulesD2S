ARCH     = -m64
GPU_ARCH = 
JAVA_DIR = /usr/lib/jvm/default-java
CUDA_INC = /usr/local/cuda/include
CUDA_LIB = /usr/local/cuda/lib64

todo: javah multithreading native lib
javah: 
	javac -classpath ../../../../target/ERulesD2S-1.0-jar-with-dependencies.jar:../../../../lib/JCLEC4-base-1.0-jar-with-dependencies.jar -h jni ../../java/net/sf/jclec/problem/classification/evolutionarylearner/RuleEvaluatorGPU.java
multithreading: multithreading.cpp
	g++ $(ARCH) -fPIC -O3 -I$(CUDA_INC) -c -o multithreading.o multithreading.cpp -lpthread
native:
	nvcc --ptxas-options=-v $(GPU_ARCH) -Xcompiler -fPIC $(ARCH) -O3 -shared -I$(CUDA_INC) -I$(JAVA_DIR)/include/linux -I$(JAVA_DIR)/include -c gpuEvaluator.cu -o gpuEvaluator.o -lpthread -lm 
ptx:
	nvcc --ptxas-options=-v $(GPU_ARCH) $(ARCH) -fPIC -ptx gpuEvaluator.cu -I$(JAVA_DIR)/include/linux -I$(JAVA_DIR)/include -I$(CUDA_INC) -L$(CUDA_LIB) -O3 $(ARCH) -lcuda -lcudart
lib: gpuEvaluator.o
	g++ $(ARCH) -fPIC -O3 -shared -I$(CUDA_INC) -L$(CUDA_LIB) -Wl,-soname,gpuEvaluator multithreading.o gpuEvaluator.o -o libERulesD2SGPU.so -lc -lpthread -lcuda -lcudart
clean:
	rm multithreading.o gpuEvaluator.o libERulesD2SGPU.so