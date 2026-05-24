package fasthardware;

import fastterminal.FastTerminal;

public class Demo {
    public static void main(String[] args) {
        FastTerminal.init();
        FastTerminal.clearScreen();
        
        System.out.println(FastTerminal.color(FastTerminal.CYAN) + "===========================================");
        System.out.println(FastTerminal.color(FastTerminal.YELLOW) + " FastHardware v0.1.0 - Native Telemetry ");
        System.out.println(FastTerminal.color(FastTerminal.CYAN) + "===========================================" + FastTerminal.reset());
        
        System.out.println(FastTerminal.color(FastTerminal.GRAY) + "Initializing native PDH and WMI..." + FastTerminal.reset());
        
        try {
            FastHardware hw = FastHardware.create();
            System.out.println(FastTerminal.color(FastTerminal.GREEN) + "Success! Native bridge established.\n" + FastTerminal.reset());
            
            while (true) {
                HardwareSnapshot snap = hw.getSnapshot();
                
                System.out.print("\033[H\033[2J"); // ANSI clear screen to refresh
                System.out.flush();
                
                System.out.println(FastTerminal.color(FastTerminal.CYAN) + "╔══════════════════════════════════════════╗");
                System.out.println("║        FastHardware Live Monitor         ║");
                System.out.println("╚══════════════════════════════════════════╝" + FastTerminal.reset());
                System.out.println();
                
                // CPU Usage
                String cpuColor = snap.cpuUsagePercent() > 80 ? FastTerminal.RED : FastTerminal.GREEN;
                System.out.printf(FastTerminal.color(FastTerminal.WHITE) + "CPU Usage:   " + FastTerminal.color(cpuColor) + "%6.2f %%\n" + FastTerminal.reset(), snap.cpuUsagePercent());
                
                // Temperatures
                String tempColor = snap.cpuTemperatureCelsius() > 85 ? FastTerminal.RED : FastTerminal.YELLOW;
                System.out.printf(FastTerminal.color(FastTerminal.WHITE) + "CPU Temp:    " + FastTerminal.color(tempColor) + "%6.2f °C\n" + FastTerminal.reset(), snap.cpuTemperatureCelsius());
                
                if (snap.gpuTemperatureCelsius() > 0) {
                    System.out.printf(FastTerminal.color(FastTerminal.WHITE) + "GPU Temp:    " + FastTerminal.color(tempColor) + "%6.2f °C\n" + FastTerminal.reset(), snap.gpuTemperatureCelsius());
                }
                
                // Memory
                double freeGB = snap.freeRamBytes() / (1024.0 * 1024 * 1024);
                double totalGB = snap.totalRamBytes() / (1024.0 * 1024 * 1024);
                System.out.printf(FastTerminal.color(FastTerminal.WHITE) + "RAM Free:    " + FastTerminal.color(FastTerminal.GREEN) + "%6.2f GB " + FastTerminal.color(FastTerminal.GRAY) + "/ %.2f GB\n" + FastTerminal.reset(), freeGB, totalGB);
                
                System.out.println();
                System.out.println(FastTerminal.color(FastTerminal.GRAY) + "(Press Ctrl+C to exit)" + FastTerminal.reset());
                
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            System.err.println(FastTerminal.color(FastTerminal.RED) + "Failed to initialize FastHardware: " + e.getMessage() + FastTerminal.reset());
            e.printStackTrace();
        }
    }
}
