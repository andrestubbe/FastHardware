package fasthardware.internal;

import fasthardware.FastHardware;
import fasthardware.HardwareSnapshot;
import fastcore.FastCore;

public class NativeFastHardware implements FastHardware {

    static {
        // Automatically unpacks and loads the native DLL via FastCore
        FastCore.loadLibrary("fasthardware");
    }

    // --- Native JNI Bindings ---
    private static native double nativeGetGlobalCpuUsage();
    private static native double[] nativeGetPerCoreCpuUsage();
    private static native long nativeGetTotalMemoryBytes();
    private static native long nativeGetFreeMemoryBytes();
    private static native double nativeGetCpuTemperatureCelsius();
    private static native double nativeGetGpuTemperatureCelsius();

    @Override
    public HardwareSnapshot getSnapshot() {
        long total = nativeGetTotalMemoryBytes();
        long free = nativeGetFreeMemoryBytes();
        long used = total - free;
        
        return new HardwareSnapshot(
            nativeGetGlobalCpuUsage(),
            nativeGetPerCoreCpuUsage(),
            used,
            total,
            nativeGetCpuTemperatureCelsius(),
            nativeGetGpuTemperatureCelsius()
        );
    }

    @Override
    public double getGlobalCpuUsage() {
        return nativeGetGlobalCpuUsage();
    }

    @Override
    public double[] getPerCoreCpuUsage() {
        return nativeGetPerCoreCpuUsage();
    }

    @Override
    public long getTotalMemoryBytes() {
        return nativeGetTotalMemoryBytes();
    }

    @Override
    public long getFreeMemoryBytes() {
        return nativeGetFreeMemoryBytes();
    }

    @Override
    public double getCpuTemperatureCelsius() {
        return nativeGetCpuTemperatureCelsius();
    }

    @Override
    public double getGpuTemperatureCelsius() {
        return nativeGetGpuTemperatureCelsius();
    }
}
