import fasthardware.*;
public class TempDebug {
    public static void main(String[] args) throws Exception {
        FastHardware hw = FastHardware.create();
        hw.getSnapshot(); Thread.sleep(1200);
        // Poll fast — if ACPI updates at all we should see it change
        System.out.println("Polling temp 20x at 500ms...");
        double first = hw.getCpuTemperatureCelsius();
        double min = first, max = first;
        for (int i = 0; i < 20; i++) {
            Thread.sleep(500);
            double t = hw.getCpuTemperatureCelsius();
            if (t < min) min = t;
            if (t > max) max = t;
            System.out.printf("  [%ds] %.2f C%n", i, t);
        }
        System.out.printf("Range: %.2f - %.2f C  (delta=%.2f)%n", min, max, max-min);
        if (max - min < 0.01) System.out.println("=> STATIC: ACPI zone does not update on this hardware");
        else System.out.println("=> LIVE: sensor is updating!");
    }
}