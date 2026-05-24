package fasthardware;

import fastterminal.FastTerminal;

public class Demo {
    // ANSI color codes
    private static final String RESET = "\033[0m";
    private static final String RED = "\033[31m";
    private static final String GREEN = "\033[32m";
    private static final String YELLOW = "\033[33m";
    private static final String CYAN = "\033[36m";
    private static final String WHITE = "\033[37m";
    private static final String GRAY = "\033[90m";

    public static void main(String[] args) {
        // Enable ANSI Virtual Terminal processing in Windows console
        try {
            FastTerminal.setAnsiRawMode(true);
        } catch (Throwable t) {
            // Ignore if already enabled or unsupported
        }

        System.out.println(CYAN + "===========================================");
        System.out.println(YELLOW + " FastHardware v0.1.0 - Native Telemetry ");
        System.out.println(CYAN + "===========================================" + RESET);
        
        System.out.println(GRAY + "Initializing native PDH and WMI..." + RESET);
        
        try {
            FastHardware hw = FastHardware.create();
            System.out.println(GREEN + "Success! Native bridge established.\n" + RESET);
            
            while (true) {
                HardwareSnapshot snap = hw.getSnapshot();
                
                System.out.print("\033[H\033[2J"); // ANSI clear screen
                System.out.flush();
                
                System.out.println(CYAN + "╔══════════════════════════════════════════╗");
                System.out.println("║        FastHardware Live Monitor         ║");
                System.out.println("╚══════════════════════════════════════════╝" + RESET);
                System.out.println();
                
                // CPU Usage
                String cpuColor = snap.cpuUsagePercent() > 80 ? RED : GREEN;
                System.out.printf(WHITE + "CPU Usage:   " + cpuColor + "%6.2f %%\n" + RESET, snap.cpuUsagePercent());
                
                // Temperatures
                String tempColor = snap.cpuTemperatureCelsius() > 85 ? RED : YELLOW;
                System.out.printf(WHITE + "CPU Temp:    " + tempColor + "%6.2f °C\n" + RESET, snap.cpuTemperatureCelsius());
                
                if (snap.gpuTemperatureCelsius() > 0) {
                    System.out.printf(WHITE + "GPU Temp:    " + tempColor + "%6.2f °C\n" + RESET, snap.gpuTemperatureCelsius());
                }
                
                // Memory
                double freeGB = snap.freeRamBytes() / (1024.0 * 1024 * 1024);
                double totalGB = snap.totalRamBytes() / (1024.0 * 1024 * 1024);
                System.out.printf(WHITE + "RAM Free:    " + GREEN + "%6.2f GB " + GRAY + "/ %.2f GB\n" + RESET, freeGB, totalGB);
                
                System.out.println();
                System.out.println(GRAY + "(Press Ctrl+C to exit)" + RESET);
                
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            System.err.println(RED + "Failed to initialize FastHardware: " + e.getMessage() + RESET);
            e.printStackTrace();
        } finally {
            try {
                FastTerminal.setAnsiRawMode(false);
            } catch (Throwable t) {}
        }
    }
}
