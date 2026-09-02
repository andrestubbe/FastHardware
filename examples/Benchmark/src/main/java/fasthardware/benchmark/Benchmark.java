package fasthardware.benchmark;

import fasthardware.FastHardware;
import fasthardware.HardwareSnapshot;
import org.openjdk.jmh.annotations.*;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.util.concurrent.TimeUnit;

/**
 * FastHardware JMH Benchmark Suite
 *
 * Compares FastHardware native telemetry (Win32 PDH/WMI via JNI)
 * against standard Java equivalents (JMX / Runtime API).
 *
 * Run via: run-benchmark.bat
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class Benchmark {

    private FastHardware hw;
    private OperatingSystemMXBean osMXBean;
    private MemoryMXBean memMXBean;
    private Runtime runtime;

    @Setup(Level.Trial)
    public void setup() {
        hw = FastHardware.create();
        osMXBean = ManagementFactory.getOperatingSystemMXBean();
        memMXBean = ManagementFactory.getMemoryMXBean();
        runtime = Runtime.getRuntime();
    }

    // =========================================================================
    // GROUP 1: Full Snapshot — atomic read of all telemetry at once
    // =========================================================================

    /** FastHardware: single atomic snapshot of ALL telemetry (recommended API). */
    @org.openjdk.jmh.annotations.Benchmark
    public HardwareSnapshot fastHardware_getSnapshot() {
        return hw.getSnapshot();
    }

    /** Java JMX baseline: 3 separate MXBean calls (no temperature, no per-core). */
    @org.openjdk.jmh.annotations.Benchmark
    public double java_jmx_combinedSnapshot() {
        double cpu = osMXBean.getSystemLoadAverage();
        long heapUsed = memMXBean.getHeapMemoryUsage().getUsed();
        long free = runtime.freeMemory();
        return cpu + heapUsed + free;
    }

    // =========================================================================
    // GROUP 2: CPU Usage
    // =========================================================================

    @org.openjdk.jmh.annotations.Benchmark
    public double fastHardware_getCpuUsage() {
        return hw.getGlobalCpuUsage();
    }

    /** Java: system load average — not the same as CPU%, only 1-min rolling average. */
    @org.openjdk.jmh.annotations.Benchmark
    public double java_jmx_getCpuUsage() {
        return osMXBean.getSystemLoadAverage();
    }

    // =========================================================================
    // GROUP 3: Per-Core CPU Usage
    // =========================================================================

    /** FastHardware: per-core PDH counters — native, real-time, per logical core. */
    @org.openjdk.jmh.annotations.Benchmark
    public double[] fastHardware_getPerCoreCpuUsage() {
        return hw.getPerCoreCpuUsage();
    }

    /** Java has no per-core CPU API. Closest equivalent is single system load average. */
    @org.openjdk.jmh.annotations.Benchmark
    public double java_jmx_systemLoadAverage() {
        return osMXBean.getSystemLoadAverage();
    }

    // =========================================================================
    // GROUP 4: Free RAM (OS-level physical vs JVM heap)
    // =========================================================================

    /** FastHardware: true OS-level free physical RAM via GlobalMemoryStatusEx. */
    @org.openjdk.jmh.annotations.Benchmark
    public long fastHardware_getFreeMemoryBytes() {
        return hw.getFreeMemoryBytes();
    }

    /** Java: reports JVM heap free only — NOT system-wide physical RAM. */
    @org.openjdk.jmh.annotations.Benchmark
    public long java_runtime_getFreeMemory() {
        return runtime.freeMemory();
    }

    // =========================================================================
    // GROUP 5: Total RAM
    // =========================================================================

    @org.openjdk.jmh.annotations.Benchmark
    public long fastHardware_getTotalMemoryBytes() {
        return hw.getTotalMemoryBytes();
    }

    @org.openjdk.jmh.annotations.Benchmark
    public long java_runtime_getTotalMemory() {
        return runtime.totalMemory();
    }

    // =========================================================================
    // GROUP 6: CPU Temperature — FastHardware exclusive, Java has no equivalent
    // =========================================================================

    /** FastHardware: CPU package temperature via WMI MSAcpi_ThermalZoneTemperature. */
    @org.openjdk.jmh.annotations.Benchmark
    public double fastHardware_getCpuTemperature() {
        return hw.getCpuTemperatureCelsius();
    }

    /** Java has NO thermal sensor API. nanoTime shown as "the best Java can do". */
    @org.openjdk.jmh.annotations.Benchmark
    public long java_nanoTime_baseline() {
        return System.nanoTime();
    }

    // =========================================================================
    // GROUP 7: GPU Temperature — FastHardware exclusive
    // =========================================================================

    @org.openjdk.jmh.annotations.Benchmark
    public double fastHardware_getGpuTemperature() {
        return hw.getGpuTemperatureCelsius();
    }
}