# FastHardware API Reference

## Classes

### `FastHardware` (Interface)
The main entry point for the library.
*   `static FastHardware create()`: Initializes the native library and returns a hardware monitor instance.
*   `HardwareSnapshot getSnapshot()`: Returns a frozen record of all hardware telemetry at the current millisecond.
*   `double getGlobalCpuUsage()`: Returns global CPU usage (0.0 to 100.0).
*   `double[] getPerCoreCpuUsage()`: Returns an array of CPU usage per logical core.
*   `long getTotalMemoryBytes()`: Returns total physical RAM.
*   `long getFreeMemoryBytes()`: Returns available physical RAM.
*   `double getCpuTemperatureCelsius()`: Returns CPU package temperature.
*   `double getGpuTemperatureCelsius()`: Returns GPU temperature.

### `HardwareSnapshot` (Record)
A Java 17 record containing the complete telemetry payload.
*   `double cpuUsagePercent`
*   `double[] perCoreCpuUsage`
*   `long usedRamBytes`
*   `long totalRamBytes`
*   `double cpuTemperatureCelsius`
*   `double gpuTemperatureCelsius`
*   `long freeRamBytes()` (Helper Method)
