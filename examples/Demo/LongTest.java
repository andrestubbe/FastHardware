import fasthardware.*;
public class LongTest {
    public static void main(String[] args) throws Exception {
        FastHardware hw = FastHardware.create();
        hw.getSnapshot(); // warmup
        Thread.sleep(1100);
        System.out.println("Time     CPU%      CPU°C   RAM Free  GPU°C");
        System.out.println("-------- --------- ------- --------- ------");
        for (int i = 0; i < 15; i++) {
            HardwareSnapshot s = hw.getSnapshot();
            System.out.printf("[%2ds]    %5.1f%%   %5.1f°C  %4dMB   %4.1f°C%n",
                i,
                s.cpuUsagePercent(),
                s.cpuTemperatureCelsius(),
                s.freeRamBytes() / 1024 / 1024,
                s.gpuTemperatureCelsius()
            );
            Thread.sleep(1000);
        }
    }
}