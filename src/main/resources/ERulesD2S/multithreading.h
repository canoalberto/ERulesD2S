#ifndef MULTITHREADING_H
#define MULTITHREADING_H

//Simple portable thread library.

#if _WIN32
    //Windows threads.
    #include <windows.h>

    typedef HANDLE CUTThread;
    typedef unsigned (WINAPI *CUT_THREADROUTINE)(void *);

    #define CUT_THREADPROC unsigned WINAPI
    #define CUT_THREADEND return 0
    #define SEMAPHORE HANDLE
	 #define SEM_INIT(pobject,pattr) (*pobject=CreateSemaphore(NULL,0,1,NULL))
	 #define SEM_WAIT(pobject) WaitForSingleObject(*pobject,INFINITE)
    #define SEM_POST(pobject) ReleaseSemaphore(*pobject,1,NULL)

#else
    //POSIX threads.
    #include <pthread.h>
    #include <semaphore.h>

    typedef pthread_t CUTThread;
    typedef void *(*CUT_THREADROUTINE)(void *);

    #define CUT_THREADPROC void
    #define CUT_THREADEND pthread_exit(NULL)
    #define SEMAPHORE sem_t
	 #define SEM_INIT(pobject,pattr) sem_init(pobject,0,0)
	 #define SEM_WAIT(pobject) sem_wait(pobject)
	 #define SEM_POST(pobject) sem_post(pobject)

#endif

#ifdef __cplusplus
    extern "C" {
#endif

//Create thread.
CUTThread cutStartThread(CUT_THREADROUTINE, void *data);

//Wait for thread to finish.
void cutEndThread(CUTThread thread);

//Destroy thread.
void cutDestroyThread(CUTThread thread);

//Wait for multiple threads.
void cutWaitForThreads(const CUTThread *threads, int num);

#ifdef __cplusplus
} //extern "C"
#endif

#endif //MULTITHREADING_H
