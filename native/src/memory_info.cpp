#include "fasthardware_jni.h"
#include <windows.h>

JNIEXPORT jlong JNICALL Java_fasthardware_internal_NativeFastHardware_nativeGetTotalMemoryBytes
  (JNIEnv *env, jclass clazz) {
    MEMORYSTATUSEX memInfo;
    memInfo.dwLength = sizeof(MEMORYSTATUSEX);
    if (GlobalMemoryStatusEx(&memInfo)) {
        return (jlong)memInfo.ullTotalPhys;
    }
    return 0;
}

JNIEXPORT jlong JNICALL Java_fasthardware_internal_NativeFastHardware_nativeGetFreeMemoryBytes
  (JNIEnv *env, jclass clazz) {
    MEMORYSTATUSEX memInfo;
    memInfo.dwLength = sizeof(MEMORYSTATUSEX);
    if (GlobalMemoryStatusEx(&memInfo)) {
        return (jlong)memInfo.ullAvailPhys;
    }
    return 0;
}
