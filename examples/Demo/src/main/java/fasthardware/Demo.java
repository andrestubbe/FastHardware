package fasthardware;

/**
 * FastHardware Terminal Demo
 *
 * Pure ANSI terminal dashboard — no Swing, no mouse, no keyboard.
 * Shows CPU%, CPU Temp, RAM, GPU Temp as live bars + scrolling waveform.
 *
 * Press Ctrl+C to exit.
 */
public class Demo {

    // ANSI escape helpers
    private static final String ESC   = "\u001B[";
    private static final String RESET = "\u001B[0m";
    private static final String BOLD  = "\u001B[1m";
    private static final String CLEAR = "\u001B[2J\u001B[H";
    private static final String HIDE_CURSOR = "\u001B[?25l";
    private static final String SHOW_CURSOR = "\u001B[?25h";
    private static final String ALT_ON  = "\u001B[?1049h";
    private static final String ALT_OFF = "\u001B[?1049l";

    // Block chars for bar rendering
    private static final char[] BLOCKS = {' ', '▏','▎','▍','▌','▋','▊','▉','█'};
    private static final char[] WAVE   = {' ','▂','▃','▄','▅','▆','▇','█'};

    // 24-bit color helpers
    private static String fg(int r, int g, int b) { return ESC + "38;2;" + r + ";" + g + ";" + b + "m"; }
    private static String bg(int r, int g, int b) { return ESC + "48;2;" + r + ";" + g + ";" + b + "m"; }
    private static String mv(int row, int col)     { return ESC + row + ";" + col + "H"; }

    // Palette
    private static final String C_CPU   = fg(56,  189, 248);  // sky blue
    private static final String C_TEMP  = fg(255, 107,  53);  // orange
    private static final String C_RAM   = fg(167, 139, 250);  // violet
    private static final String C_GPU   = fg(52,  211, 153);  // emerald
    private static final String C_WARN  = fg(239,  68,  68);  // red
    private static final String C_DIM   = fg(71,   85, 105);  // slate-500
    private static final String C_MID   = fg(148, 163, 184);  // slate-300
    private static final String C_WHITE = fg(238, 242, 255);  // indigo-50
    private static final String BG_CARD = bg(23,  28,  34);   // near-black card
    private static final String BG_MAIN = bg(16,  20,  24);   // darkest bg

    // History ring buffer
    private static final int HIST = 80;
    private final double[] cpuH  = new double[HIST];
    private final double[] tempH = new double[HIST];
    private final double[] ramH  = new double[HIST];
    private final double[] gpuH  = new double[HIST];
    private int head = 0;

    // Smoothed live values
    private double cpuS = 0, tempS = 0, ramS = 0, gpuS = 0;
    private double totalRamGB = 0;
    private final FastHardware hw;

    public Demo(FastHardware hw) {
        this.hw = hw;
        totalRamGB = hw.getTotalMemoryBytes() / (1024.0 * 1024 * 1024);
    }

    private void poll() {
        HardwareSnapshot s = hw.getSnapshot();
        double cpu  = s.cpuUsagePercent();
        double temp = s.cpuTemperatureCelsius();
        double ram  = (1.0 - (double) s.freeRamBytes() / s.totalRamBytes()) * 100.0;
        double gpu  = s.gpuTemperatureCelsius();
        cpuS  = cpuS  * 0.75 + cpu  * 0.25;
        tempS = tempS * 0.88 + temp * 0.12;
        ramS  = ramS  * 0.80 + ram  * 0.20;
        gpuS  = gpuS  * 0.88 + gpu  * 0.12;
        head  = (head + 1) % HIST;
        cpuH[head]  = cpuS;
        tempH[head] = tempS;
        ramH[head]  = ramS;
        gpuH[head]  = gpuS;
    }

    /** Render a single bar row: label, bar, value */
    private String bar(String color, String label, double pct, String value, int barWidth, boolean warn) {
        String accent = warn ? C_WARN : color;
        int filled8 = (int) Math.max(0, Math.min(barWidth * 8, barWidth * 8 * pct / 100.0));
        int fullBlocks = filled8 / 8;
        int partial    = filled8 % 8;
        StringBuilder b = new StringBuilder();
        b.append(BG_CARD);
        b.append(accent).append(BOLD).append(String.format("  %-14s", label)).append(RESET).append(BG_CARD);
        b.append(C_DIM).append("│").append(RESET).append(BG_CARD);
        b.append(accent);
        for (int i = 0; i < fullBlocks; i++) b.append('█');
        if (partial > 0 && fullBlocks < barWidth) b.append(BLOCKS[partial]);
        int remaining = barWidth - fullBlocks - (partial > 0 ? 1 : 0);
        b.append(C_DIM);
        for (int i = 0; i < remaining; i++) b.append('░');
        b.append(RESET).append(BG_CARD);
        b.append(C_DIM).append("│").append(RESET).append(BG_CARD);
        b.append(warn ? C_WARN : C_WHITE).append(BOLD).append(String.format(" %-12s", value)).append(RESET).append(BG_CARD);
        b.append(RESET);
        return b.toString();
    }

    /** Render a mini sparkline history graph for one metric (1 row, HIST chars wide) */
    private String sparkline(String color, double[] history, double maxVal, boolean warn) {
        StringBuilder b = new StringBuilder();
        b.append(BG_CARD).append("  ").append(C_DIM).append("└─ ");
        String accent = warn ? C_WARN : color;
        b.append(accent);
        for (int i = 0; i < HIST; i++) {
            int idx = (head - (HIST - 1 - i) + HIST * 100) % HIST;
            double v = history[idx];
            int level = (int) Math.max(0, Math.min(7, (v / maxVal) * 8.0));
            b.append(WAVE[level]);
        }
        b.append(RESET);
        return b.toString();
    }

    private String divider(int width) {
        return BG_CARD + C_DIM + "  " + "─".repeat(width - 2) + RESET;
    }

    private String header() {
        return BG_MAIN + C_WHITE + BOLD +
               "  ⚡ FastHardware v0.1.0  —  Real-Time System Monitor  " +
               RESET + BG_MAIN + C_DIM + "  [Ctrl+C to exit]" + RESET;
    }

    private void frame(StringBuilder out, int termW) {
        int barW = Math.max(20, Math.min(HIST, termW - 34));

        out.append(ALT_ON);
        out.append(mv(1, 1));
        out.append(BG_MAIN);
        out.append(CLEAR);
        out.append(mv(1, 1));

        // Header
        out.append(header()).append("\n");
        out.append(BG_MAIN).append(C_DIM).append("  " + "─".repeat(termW - 4) + "\n").append(RESET);

        // Section: CPU
        out.append("\n");
        out.append(BG_CARD).append(C_CPU).append(BOLD)
           .append("  ── CPU ──────────────────────────────────\n").append(RESET);
        out.append(bar(C_CPU, "CPU Usage", cpuS, String.format("%.1f%%", cpuS), barW, cpuS > 85)).append("\n");
        out.append(sparkline(C_CPU, cpuH, 100.0, cpuS > 85)).append("\n");
        out.append(bar(C_TEMP, "CPU Temp", tempS, String.format("%.0f°C", tempS), barW, tempS > 90)).append("\n");
        out.append(sparkline(C_TEMP, tempH, 110.0, tempS > 90)).append("\n");

        // Section: Memory
        out.append("\n");
        out.append(BG_CARD).append(C_RAM).append(BOLD)
           .append("  ── MEMORY ───────────────────────────────\n").append(RESET);
        String ramVal = String.format("%.1f%% / %.1fGB", ramS, totalRamGB);
        out.append(bar(C_RAM, "RAM Used", ramS, ramVal, barW, ramS > 90)).append("\n");
        out.append(sparkline(C_RAM, ramH, 100.0, ramS > 90)).append("\n");

        // Section: GPU
        out.append("\n");
        out.append(BG_CARD).append(C_GPU).append(BOLD)
           .append("  ── GPU ──────────────────────────────────\n").append(RESET);
        out.append(bar(C_GPU, "GPU Temp", gpuS, String.format("%.0f°C", gpuS), barW, gpuS > 85)).append("\n");
        out.append(sparkline(C_GPU, gpuH, 110.0, gpuS > 85)).append("\n");

        // Footer
        out.append("\n");
        out.append(BG_MAIN).append(C_DIM)
           .append(String.format("  Native Win32 PDH · WMI · JNI  |  Total RAM: %.1f GB  |  Polling: 500ms", totalRamGB))
           .append(RESET).append("\n");
    }

    public void run() {
        // Shutdown hook restores terminal
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.print(ALT_OFF + SHOW_CURSOR + RESET);
            System.out.flush();
        }));

        System.out.print(ALT_ON + HIDE_CURSOR);
        System.out.flush();

        // Try to detect terminal width
        int termW = 120;
        try {
            String cols = System.getenv("COLUMNS");
            if (cols != null) termW = Integer.parseInt(cols.trim());
        } catch (Throwable ignored) {}

        while (true) {
            poll();
            StringBuilder out = new StringBuilder(4096);
            frame(out, termW);
            System.out.print(out);
            System.out.flush();
            try { Thread.sleep(500); } catch (InterruptedException ignored) { break; }
        }
    }

    public static void main(String[] args) {
        // Force UTF-8 output on Windows
        try {
            System.setOut(new java.io.PrintStream(System.out, true, "UTF-8"));
        } catch (Throwable ignored) {}

        FastHardware hw = FastHardware.create();
        new Demo(hw).run();
    }
}