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

    private final java.util.concurrent.atomic.AtomicBoolean pollingActive = new java.util.concurrent.atomic.AtomicBoolean(false);
    private Thread pollingThread;

    @Override
    public double getCpuTemperatureCelsius() {
        return nativeGetCpuTemperatureCelsius();
    }

    @Override
    public double getGpuTemperatureCelsius() {
        return nativeGetGpuTemperatureCelsius();
    }

    @Override
    public synchronized void startPolling(long intervalMs, java.util.function.Consumer<HardwareSnapshot> listener) {
        if (listener == null) throw new IllegalArgumentException("Listener cannot be null");
        if (intervalMs < 10) intervalMs = 10;

        stopPolling();
        pollingActive.set(true);

        final long period = intervalMs;
        pollingThread = new Thread(() -> {
            // Initial PDH warmup
            getSnapshot();
            try {
                Thread.sleep(Math.min(period, 1000));
            } catch (InterruptedException ignored) {}

            while (pollingActive.get() && !Thread.currentThread().isInterrupted()) {
                long t0 = System.currentTimeMillis();
                try {
                    HardwareSnapshot snapshot = getSnapshot();
                    listener.accept(snapshot);
                } catch (Throwable t) {
                    // Suppress and continue
                }
                long elapsed = System.currentTimeMillis() - t0;
                long sleepTime = period - elapsed;
                if (sleepTime > 0) {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }, "FastHardware-Poller");
        pollingThread.setDaemon(true);
        pollingThread.start();
    }

    @Override
    public synchronized void stopPolling() {
        pollingActive.set(false);
        if (pollingThread != null) {
            pollingThread.interrupt();
            pollingThread = null;
        }
    }

    @Override
    public boolean isPolling() {
        return pollingActive.get();
    }
}
