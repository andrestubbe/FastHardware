# FastHardware Architecture

## Overview
FastHardware is designed to bypass the traditional, heavy Java abstractions for hardware monitoring. Instead of relying on `OperatingSystemMXBean` or executing external processes like `wmic`, it interacts directly with the Windows kernel APIs using C++ and JNI.

## 1. Native Integration (C++)

### CPU Monitoring (PDH)
We use the **Performance Data Helper (PDH)** API for ultra-fast CPU polling. 
- PDH allows us to register the `\Processor(_Total)\% Processor Time` and core-specific counters exactly once.
- Once registered, querying `PdhCollectQueryData` returns data in microseconds without any OS process-spawning overhead.

### Memory Monitoring (GlobalMemoryStatusEx)
We use the standard Win32 `GlobalMemoryStatusEx` function. This is a direct kernel memory table read, executing in nanoseconds.

### Thermal Sensors (WMI)
Temperatures are fetched using **Windows Management Instrumentation (WMI)**.
- For CPU package temps, we query the `MSAcpi_ThermalZoneTemperature` namespace.
- WMI COM objects are initialized asynchronously, and the JNI bridge requests the `CurrentTemperature` property, which is returned in tenths of Kelvin and converted to Celsius natively.

## 2. JNI Boundary
All JNI methods in `fasthardware_jni.h` use primitives (`jlong`, `jdouble`, `jdoubleArray`) to completely avoid object allocation on the Java heap. 
The Java-side `NativeFastHardware.java` acts as the bridge.

## 3. FastCore Loading
The DLL (`fasthardware.dll`) is embedded inside the compiled JAR's `resources` directory. 
At runtime, `fastcore.FastCore.loadLibrary("fasthardware")` dynamically extracts this DLL into a temporary system directory and executes `System.load()`.
