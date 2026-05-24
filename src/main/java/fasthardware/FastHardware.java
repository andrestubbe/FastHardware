package fasthardware;

import fasthardware.internal.NativeFastHardware;

/**
 * FastHardware - Native Hardware Telemetry API
 */
public interface FastHardware {
    
    /** Factory method to instantiate the native implementation. */
    static FastHardware create() { 
        return new NativeFastHardware(); 
    }

    /** Returns a full snapshot of the current hardware telemetry. */
    HardwareSnapshot getSnapshot();

    /** Returns the global CPU usage across all cores (0.0 to 100.0). */
    double getGlobalCpuUsage();

    /** Returns the CPU usage per logical core (0.0 to 100.0). */
    double[] getPerCoreCpuUsage();

    /** Returns total physical memory in bytes. */
    long getTotalMemoryBytes();

    /** Returns free physical memory in bytes. */
    long getFreeMemoryBytes();

    /** Returns the CPU package temperature in Celsius. */
    double getCpuTemperatureCelsius();

    /** Returns the GPU temperature in Celsius (if available). */
    double getGpuTemperatureCelsius();
}
