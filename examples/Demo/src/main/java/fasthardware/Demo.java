package fasthardware;

import fastansi.FastANSI;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ⚡ FastHardware — Clean Minimalist Terminal Telemetry Demo
 * 
 * Demonstrates user-driven asynchronous polling with FastANSI gray/white theme.
 */
public class Demo {

    private static String darkGray(String text) {
        return FastANSI.fg(240) + text + FastANSI.RESET;
    }

    private static String gray(String text) {
        return FastANSI.fg(245) + text + FastANSI.RESET;
    }

    private static String white(String text) {
        return FastANSI.FG_BRIGHT_WHITE + text + FastANSI.RESET;
    }

    private static String boldWhite(String text) {
        return FastANSI.BOLD + FastANSI.FG_BRIGHT_WHITE + text + FastANSI.RESET;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(darkGray("========================================================================================================================"));
        System.out.println(" " + boldWhite("FastHardware") + darkGray(" — Native System Telemetry & Polling Engine (Win32 PDH + WMI + Kernel RAM)"));
        System.out.println(darkGray(" Continuous asynchronous background polling emitting atomic snapshot records"));
        System.out.println(darkGray("========================================================================================================================"));
        System.out.println();

        FastHardware hw = FastHardware.create();
        AtomicInteger sampleCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            hw.stopPolling();
            System.out.println("\n" + darkGray("========================================================================================================================"));
            System.out.println(" " + boldWhite("FastHardware") + darkGray(" polling stopped gracefully."));
            System.out.println(darkGray("========================================================================================================================"));
        }));

        System.out.println(darkGray("[Initialization] Starting hardware telemetry polling at ") + boldWhite("500 ms") + darkGray(" intervals... (Press Ctrl+C to stop)\n"));

        // Register async polling listener directly on FastHardware
        hw.startPolling(500, snapshot -> {
            int count = sampleCount.incrementAndGet();

            double cpu = snapshot.cpuUsagePercent();
            double temp = snapshot.cpuTemperatureCelsius();
            long freeMb = snapshot.freeRamBytes() / (1024 * 1024);
            long totalMb = snapshot.totalRamBytes() / (1024 * 1024);
            double ramPct = (1.0 - ((double) snapshot.freeRamBytes() / snapshot.totalRamBytes())) * 100.0;
            double gpu = snapshot.gpuTemperatureCelsius();

            // Progress branch indicators
            String branch = (count % 10 == 0) ? "└──" : "├──";
            String badge = boldWhite(String.format("[%04d]", count));

            // Format telemetry string in clean gray/white
            String cpuStr = String.format("CPU: %5.1f%%", cpu);
            String tempStr = (temp > 0) ? String.format("Temp: %4.1f°C", temp) : "Temp: N/A";
            String ramStr = String.format("RAM: %4.1f%% (%d / %d MB)", ramPct, (totalMb - freeMb), totalMb);
            String gpuStr = (gpu > 0) ? String.format("GPU: %4.1f°C", gpu) : "GPU: N/A";

            String bar = renderSimpleBar(cpu, 24);

            System.out.printf("  %s %s %s %s  %s  %s  %s\n",
                    darkGray(branch),
                    badge,
                    white(cpuStr),
                    darkGray("[" + bar + "]"),
                    gray(tempStr),
                    white(ramStr),
                    darkGray(gpuStr)
            );
        });

        // Keep main thread alive
        latch.await();
    }

    private static String renderSimpleBar(double percent, int width) {
        int filled = (int) Math.round((percent / 100.0) * width);
        if (filled < 0) filled = 0;
        if (filled > width) filled = width;

        StringBuilder sb = new StringBuilder(width);
        for (int i = 0; i < filled; i++) {
            sb.append("|");
        }
        for (int i = filled; i < width; i++) {
            sb.append(".");
        }
        return sb.toString();
    }
}