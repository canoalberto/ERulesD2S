#ifndef _PARAM_H_
#define _PARAM_H_

// Required includes
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <jni.h>
#include <time.h>
#include <math.h>
#include <ctype.h>
#include <float.h>
#if _WIN32
#include "multithreading.cpp"
#include <windows.h>
#else
#include "multithreading.h"
#endif

// Include CUDA libs if GPU compilation
#include <cuda.h>

// Include JNI interfaces
#include "jni/net_sf_jclec_problem_classification_evolutionarylearner_RuleEvaluatorGPU.h"

using namespace std;

// Maximum number of GPU devices
#define MAX_THREADS 8
// Number of threads per block at evaluation kernels
#define THREADS_EVAL_BLOCK 256
// Segment size alignment
#define ALIGNMENT THREADS_EVAL_BLOCK

#define BLOCK_SIZE_RULES 512

// Plan structure to let tasks know its thread number and population size
typedef struct {
    int thread;
    int device;
    int size;
} Plan;

// Maximum number of conditions of a rule
#define MAX_NUMBER_CONDITIONS 16
// Maximum stack depth
#define MAX_STACK MAX_NUMBER_CONDITIONS
// Maximum rule characters length
#define MAX_EXPR_LEN 5*MAX_NUMBER_CONDITIONS

#define AND 1
#define OR 2
#define _IN 3
#define _OUT 4
#define GREATER 5
#define GREATER_EQ 6
#define LESS 7
#define LESS_EQ 8
#define _EQ 9
#define _NEQ 10
#define NOT 11
#define END_EXPR 0

#endif
