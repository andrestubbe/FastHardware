#include "fasthardware_jni.h"
#include <windows.h>
#include <pdh.h>
#include <pdhmsg.h>
#include <vector>
#include <string>

#pragma comment(lib, "pdh.lib")

// Global PDH state
static PDH_HQUERY cpuQuery = NULL;
static PDH_HCOUNTER totalCpuCounter = NULL;
static std::vector<PDH_HCOUNTER> coreCounters;
static bool pdhInitialized = false;

void initPdh() {
    if (pdhInitialized) return;
    
    if (PdhOpenQuery(NULL, 0, &cpuQuery) != ERROR_SUCCESS) return;

    // Add total CPU counter
    PdhAddEnglishCounter(cpuQuery, "\\Processor(_Total)\\% Processor Time", 0, &totalCpuCounter);

    // Discover cores dynamically (simplified for v0.1.0: try 0-64)
    for (int i = 0; i < 64; ++i) {
        PDH_HCOUNTER coreCounter;
        char path[128];
        snprintf(path, sizeof(path), "\\Processor(%d)\\%% Processor Time", i);
        if (PdhAddEnglishCounter(cpuQuery, path, 0, &coreCounter) == ERROR_SUCCESS) {
            coreCounters.push_back(coreCounter);
        } else {
            break; // Stop at first failed core (assumes sequential numbering)
        }
    }
    
    PdhCollectQueryData(cpuQuery);
    pdhInitialized = true;
}

double getPdhValue(PDH_HCOUNTER counter) {
    PDH_FMT_COUNTERVALUE counterVal;
    if (PdhGetFormattedCounterValue(counter, PDH_FMT_DOUBLE, NULL, &counterVal) == ERROR_SUCCESS) {
        return counterVal.doubleValue;
    }
    return 0.0;
}

JNIEXPORT jdouble JNICALL Java_fasthardware_internal_NativeFastHardware_nativeGetGlobalCpuUsage
  (JNIEnv *env, jclass clazz) {
    if (!pdhInitialized) initPdh();
    
    PdhCollectQueryData(cpuQuery);
    return getPdhValue(totalCpuCounter);
}

JNIEXPORT jdoubleArray JNICALL Java_fasthardware_internal_NativeFastHardware_nativeGetPerCoreCpuUsage
  (JNIEnv *env, jclass clazz) {
    if (!pdhInitialized) initPdh();
    
    PdhCollectQueryData(cpuQuery);
    
    size_t numCores = coreCounters.size();
    jdoubleArray result = env->NewDoubleArray((jsize)numCores);
    if (result == NULL) return NULL; // Out of memory
    
    jdouble* cores = new jdouble[numCores];
    for (size_t i = 0; i < numCores; ++i) {
        cores[i] = getPdhValue(coreCounters[i]);
    }
    
    env->SetDoubleArrayRegion(result, 0, (jsize)numCores, cores);
    delete[] cores;
    
    return result;
}
