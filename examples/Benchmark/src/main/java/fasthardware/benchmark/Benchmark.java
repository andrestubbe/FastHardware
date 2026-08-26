package fasthardware.benchmark;

import fasthardware.FastHardware;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class Benchmark {

    private FastHardware hardware;

    @Setup
    public void setup() {
        try {
            hardware = FastHardware.create();
        } catch (Exception e) {
            hardware = null;
        }
    }

    @org.openjdk.jmh.annotations.Benchmark
    public long benchmarkGetFreeMemory() {
        if (hardware != null) {
            return hardware.getFreeMemoryBytes();
        }
        return 0L;
    }

    @org.openjdk.jmh.annotations.Benchmark
    public double benchmarkGetGlobalCpuUsage() {
        if (hardware != null) {
            return hardware.getGlobalCpuUsage();
        }
        return 0.0;
    }
}
