package fasthardware;

public record HardwareSnapshot(
    double cpuUsagePercent,
    double[] perCoreCpuUsage,
    long usedRamBytes,
    long totalRamBytes,
    double cpuTemperatureCelsius,
    double gpuTemperatureCelsius
) {
    public long freeRamBytes() {
        return totalRamBytes - usedRamBytes;
    }
}
