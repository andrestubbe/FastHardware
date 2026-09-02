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

    /**
     * Starts continuous background polling at the specified interval in milliseconds.
     * On each tick, the listener receives an atomic HardwareSnapshot.
     *
     * @param intervalMs polling frequency in milliseconds (e.g. 500ms or 1000ms)
     * @param listener callback receiving fresh snapshots
     */
    void startPolling(long intervalMs, java.util.function.Consumer<HardwareSnapshot> listener);

    /**
     * Stops any active background polling loop.
     */
    void stopPolling();

    /**
     * Checks if background polling is currently active.
     */
    boolean isPolling();
}
