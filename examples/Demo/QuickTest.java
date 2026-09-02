import fasthardware.*;
public class QuickTest {
    public static void main(String[] args) throws Exception {
        System.out.println("Creating FastHardware...");
        FastHardware hw = FastHardware.create();
        System.out.println("Type: " + hw.getClass().getName());
        
        // Poll mehrfach weil PDH Counters den ersten Frame brauchen
        for (int i = 0; i < 3; i++) {
            Thread.sleep(1100);
            HardwareSnapshot s = hw.getSnapshot();
            System.out.println("--- Poll " + (i+1) + " ---");
            System.out.println("  CPU%:     " + s.cpuUsagePercent());
            System.out.println("  CPU Temp: " + s.cpuTemperatureCelsius());
            System.out.println("  RAM Free: " + (s.freeRamBytes() / 1024 / 1024) + " MB");
            System.out.println("  RAM Total:" + (s.totalRamBytes() / 1024 / 1024) + " MB");
            System.out.println("  GPU Temp: " + s.gpuTemperatureCelsius());
        }
    }
}