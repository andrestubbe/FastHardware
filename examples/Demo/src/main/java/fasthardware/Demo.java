package fasthardware;

import fastansi.FastANSI;
import fastmouse.FastMouseListener;
import fastterminal.AnsiMouse;
import fastterminal.FastTerminal;
import fastterminal.FastTerminalRenderer;
import fastterminal.FastTerminalScene;
import fastterminal.Gradient;
import fastterminal.ui.Panel;
import fastterminal.ui.Button;
import fastterminal.ui.ProgressBar;
import fastterminal.ui.Dropdown;
import fastterminal.ui.Component;
import fastkeyboard.FastKeyboard;
import fastkeyboard.FastKeyboardImpl;

/**
 * 🌌 Real-Time Native Mouse Interactive Visualizer.
 * Displays real-time cell coordinate tracking, absolute screen pixel offsets,
 * active button click indicators, and a beautiful glowing neon crosshair.
 */
public class Demo {

    // 🎨 UI Customization Color Constants (modify these to style the panel)
    public static final int PANEL_BG_COLOR = 0xFFFFFF;     // Sleek Zinc Light Gray background
    public static final int PANEL_BORDER_COLOR = 0xE4E4E7; // Zinc border
    public static final int PANEL_HEADER_BG = 0xF0A500;    // 🟠 BeOS classic golden amber titlebar
    public static final int PANEL_HEADER_FG = 0x3D1C00;    // Deep brown text on amber
    public static final int PANEL_SHADOW_FG = 0x000000;    // Pure black shadow foreground
    public static final int PANEL_SHADOW_BG = 0x000000;    // Pure black shadow background
    public static final double PANEL_SHADOW_ALPHA = 0.25;   // Opacity of the transparent shadow


    private static volatile int mouseCellX = -1;
    private static volatile int mouseCellY = -1;
    private static volatile int absoluteX = 0;
    private static volatile int absoluteY = 0;
    private static volatile boolean isLeftPressed = false;
    private static volatile boolean isRightPressed = false;
    private static volatile int clickCount = 0;
    private static volatile boolean isDragging = false;
    private static volatile boolean isResizing = false;
    private static volatile int dragOffsetX = 0;
    private static volatile int dragOffsetY = 0;
    private static volatile boolean isMinimizePressed = false;

    // Volatile real-time window metrics
    private static volatile int clientX = 0;
    private static volatile int clientY = 0;
    private static volatile int clientWidth = 0;
    private static volatile int clientHeight = 0;
    private static volatile int fontW = 8;
    private static volatile int fontH = 16;
    private static volatile int currentCols = 100;
    private static volatile int currentRows = 30;
    private static volatile java.awt.image.BufferedImage backgroundImage = null;
    private static volatile boolean lastCursorHiddenState = false;

    public static class HardwareComponent extends Component {
        private FastHardware hw;

        private double smoothedCpu = 0.0;
        private double smoothedTemp = 0.0;
        private double smoothedRam = 0.0;

        private double[] cpuHistory = new double[1000];
        private double[] tempHistory = new double[1000];
        private double[] ramHistory = new double[1000];
        private int historyHead = 0;
        private long lastUpdate = 0;
        private boolean isFirstSample = true;

        public HardwareComponent(int x, int y, int width, int height, FastHardware hw) {
            super(x, y, width, height);
            this.hw = hw;
        }

        private String padRight(String s, int n) {
            if (s.length() >= n) return s.substring(0, n);
            return s + " ".repeat(n - s.length());
        }

        private void drawGraph(FastTerminalScene canvas, int startRow, int height, String label, double[] history, double maxVal, int graphColor) {
            int w = getWidth();
            if (w <= 0) return;

            char[] blocks = {' ', ' ', '▂', '▃', '▄', '▅', '▆', '▇', '█'};
            
            // Draw a subtle translucent dark background panel for the graph area
            for (int r = 0; r < height; r++) {
                for (int c = 0; c < w; c++) {
                    canvas.writeCellAlpha(getX() + c, getY() + startRow + r, ' ', -1, 0x000000, 0.0, 0.4);
                }
            }

            for (int col = 0; col < w; col++) {
                // Read from history right-to-left
                int idx = (historyHead - (w - 1 - col)) % cpuHistory.length;
                if (idx < 0) idx += cpuHistory.length;
                
                double val = history[idx];
                double pct = Math.max(0.0, Math.min(1.0, val / maxVal));
                int totalLevels = (int) Math.round(pct * (height * 8));
                
                for (int r = 0; r < height; r++) {
                    int levelsForThisRow = totalLevels - ((height - 1 - r) * 8);
                    char c = ' ';
                    if (levelsForThisRow >= 8) c = '█';
                    else if (levelsForThisRow > 0) c = blocks[levelsForThisRow];
                    
                    if (c != ' ') {
                        canvas.writeCellAlpha(getX() + col, getY() + startRow + r, c, graphColor, -1, 1.0, 0.0);
                    }
                }
            }
            
            // Draw overlay text (bold black, fully opaque text on transparent background)
            String paddedText = padRight(" " + label, w);
            canvas.writeStringAlpha(getX(), getY() + startRow, paddedText, 0x000000, -1, 1.0, 0.0);
        }

        @Override
        public void render(FastTerminalScene canvas) {
            if (!isVisible()) return;
            
            HardwareSnapshot snap = hw.getSnapshot();
            long now = System.currentTimeMillis();

            // Sample into history buffer at ~10 Hz (every 100ms) for a smooth 1-cell-per-100ms scroll speed
            if (now - lastUpdate > 100) {
                double rawCpu = snap.cpuUsagePercent();
                double rawTemp = snap.cpuTemperatureCelsius();
                double rawRam = 1.0 - ((double)snap.freeRamBytes() / snap.totalRamBytes());

                if (isFirstSample) {
                    smoothedCpu = rawCpu;
                    smoothedTemp = rawTemp;
                    smoothedRam = rawRam;
                    isFirstSample = false;
                } else {
                    // EMA Smoothing filter
                    smoothedCpu = (rawCpu * 0.1) + (smoothedCpu * 0.9);
                    smoothedTemp = (rawTemp * 0.05) + (smoothedTemp * 0.95); // Temp changes slowly
                    smoothedRam = (rawRam * 0.2) + (smoothedRam * 0.8);
                }

                historyHead = (historyHead + 1) % cpuHistory.length;
                cpuHistory[historyHead] = smoothedCpu;
                tempHistory[historyHead] = smoothedTemp;
                ramHistory[historyHead] = smoothedRam;

                lastUpdate = now;
            }

            int sectionHeight = 5; // Height of each graph
            
            // 1. CPU Usage Graph
            int cpuColor = smoothedCpu > 80.0 ? 0xEF4444 : 0xFFFFFF; // Red spike, otherwise Windows 11 Blue/Cyan
            String cpuLabel = String.format("CPU Usage: %.1f %%", smoothedCpu);
            drawGraph(canvas, 0, sectionHeight, cpuLabel, cpuHistory, 100.0, cpuColor);

            // 2. CPU Temp Graph
            int tempColor = smoothedTemp > 85.0 ? 0xEF4444 : (smoothedTemp > 65.0 ? 0xF59E0B : 0x10B981); // Red/Amber/Emerald
            String tempLabel = String.format("CPU Temp: %.1f °C", smoothedTemp);
            drawGraph(canvas, sectionHeight, sectionHeight, tempLabel, tempHistory, 100.0, tempColor);
            
            // 3. RAM Usage Graph
            double totalGB = snap.totalRamBytes() / (1024.0 * 1024 * 1024);
            double usedGB = totalGB * smoothedRam;
            String ramLabel = String.format("RAM Usage: %.1f GB / %.1f GB", usedGB, totalGB);
            drawGraph(canvas, sectionHeight * 2, sectionHeight, ramLabel, ramHistory, 1.0, 0x8B5CF6); // Purple
        }
    }

    public static void main(String[] args) {
        System.out.println("Initializing UI Mouse Visualizer...");

        // Initialize FastKeyboard JNI listener
        final FastKeyboard keyboard = new FastKeyboardImpl();

        // Load background image
        String[] filenames = { "Image 1.png", "cyberpunk_city.png", "synthwave_sunset.png", "space_nebula.png" };
        for (String filename : filenames) {
            try {
                java.io.File imgFile = new java.io.File(filename);
                if (!imgFile.exists()) {
                    imgFile = new java.io.File("examples/Demo/" + filename);
                }
                if (!imgFile.exists()) {
                    imgFile = new java.io.File("../" + filename);
                }
                if (imgFile.exists()) {
                    backgroundImage = javax.imageio.ImageIO.read(imgFile);
                    break;
                }
            } catch (Throwable ignored) {}
        }

        // 1. Get console font and client window coordinates
        int[] winInfo = null;
        try {
            winInfo = FastTerminal.getConsoleWindowInfo();
            if (winInfo != null && winInfo.length >= 8) {
                clientX = winInfo[2];
                clientY = winInfo[3];
                fontW = winInfo[4];
                fontH = winInfo[5];
                clientWidth = winInfo[6];
                clientHeight = winInfo[7];
            }
        } catch (Throwable t) {
            System.err.println("[WARN] Could not retrieve Win32 console metrics: " + t.getMessage());
        }

        // 2. Configure screen alternate buffer and hide standard cursor
        System.out.print(FastANSI.ALT_BUFFER_ON + FastANSI.CURSOR_HIDE);

        int cols = 100;
        int rows = 30;
        try {
            int[] size = FastTerminal.getTerminalSize();
            if (size != null && size[0] > 0 && size[1] > 0) {
                cols = size[0];
                rows = size[1];
            }
        } catch (Throwable ignored) {}
        currentCols = cols;
        currentRows = rows;

        FastTerminalRenderer renderer = new FastTerminalRenderer(cols, rows);
        FastTerminalScene canvas = new FastTerminalScene(0, 0, cols, rows);
        renderer.addScene(canvas);

        // Create the beautiful draggable panel centered dynamically
        // Background color is 0x27272A (Charcoal zinc gray), Border/Header is 0xFFFFFF (Brilliant white)
        int dashW = 56;
        int dashH = 16; // 15 rows for 3 graphs + 1 row for titlebar
        int dashX = Math.max(0, (cols - dashW) / 2);
        int dashY = Math.max(0, (rows - dashH) / 2);
        
        Panel dashboard = new Panel(dashX, dashY, dashW, dashH, PANEL_BG_COLOR);
        dashboard.setBorderStyle(Panel.BorderStyle.NONE); // Remove side and bottom borders
        dashboard.setBorderFg(PANEL_BORDER_COLOR);
        dashboard.setHasHeaderBar(true);       // KEEP titlebar
        dashboard.setHasResizeButton(false);   // Remove the resize icon
        dashboard.setHeaderBg(PANEL_HEADER_BG);
        dashboard.setHeaderFg(PANEL_HEADER_FG);
        dashboard.setBeosStyle(true);
        dashboard.setTitle("FastHardware Live Monitor");
        dashboard.setBodyAlpha(0.80);
        dashboard.setShadowFg(PANEL_SHADOW_FG);
        dashboard.setShadowBg(PANEL_SHADOW_BG);
        dashboard.setShadowAlpha(PANEL_SHADOW_ALPHA);

        FastHardware hw = FastHardware.create();
        // Place filling the entire panel width, starting just below the title bar (row 1)
        HardwareComponent hc = new HardwareComponent(0, 1, dashW, dashH - 1, hw);
        dashboard.add(hc);

        // Initialize and start FastKeyboard listener
        keyboard.startListening((deviceHandle, vKey, makeCode, isPressed, isE0, timestamp, keyChar) -> {
            if (isPressed) {
                // Focus gate check: keystrokes only apply if terminal is active window!
                if (!FastTerminal.isTerminalFocused()) {
                    return;
                }
                if (vKey == 0x1B) { // ESC key to exit
                    System.exit(0);
                }
            }
        });

        // 3. Register native mouse callbacks using SGR-1006 ANSI Tracking
        AnsiMouse mouse = AnsiMouse.open(new FastMouseListener() {
            @Override
            public void onMouseMove(long deviceHandle, int deltaX, int deltaY, int absX, int absY) {
                // absX and absY are ALREADY the correct cellX and cellY reported by the terminal!
                int cellX = absX;
                int cellY = absY;
                
                // Safe boundary clipping to keep crosshair inside valid grid
                if (cellX < 0) cellX = 0;
                if (cellX >= currentCols) cellX = currentCols - 1;
                if (cellY < 0) cellY = 0;
                if (cellY >= currentRows) cellY = currentRows - 1;
                
                mouseCellX = cellX;
                mouseCellY = cellY;

                if (isResizing) {
                    int newW = cellX - dashboard.getX() + 1;
                    int newH = cellY - dashboard.getY() + 1;
                    
                    // Enforce reasonable minimum dimensions (15 columns, 5 rows)
                    if (newW < 15) newW = 15;
                    if (newH < 5) newH = 5;
                    
                    // Clip width/height to make sure window stays within viewport boundaries
                    if (dashboard.getX() + newW > currentCols) {
                        newW = currentCols - dashboard.getX();
                    }
                    if (dashboard.getY() + newH > currentRows) {
                        newH = currentRows - dashboard.getY();
                    }
                    
                    dashboard.setWidth(newW);
                    dashboard.setHeight(newH);
                } else if (isDragging) {
                    int newX = cellX - dragOffsetX;
                    int newY = cellY - dragOffsetY;
                    // Safe clipping to viewport bounds
                    if (newX < 0) newX = 0;
                    if (newX + dashboard.getWidth() > currentCols) newX = currentCols - dashboard.getWidth();
                    if (newY < 0) newY = 0;
                    if (newY + dashboard.getHeight() > currentRows) newY = currentRows - dashboard.getHeight();
                    dashboard.setX(newX);
                    dashboard.setY(newY);
                } else {
                    dashboard.handleMouseMove(cellX, cellY);
                }
            }

            @Override
            public void onMouseButton(long deviceHandle, int buttonId, boolean isPressed) {
                if (buttonId == 0) { // Left Button
                    isLeftPressed = isPressed;
                    if (isPressed) {
                        clickCount++;
                        int dx = dashboard.getX();
                        int dy = dashboard.getY();
                        int dw = dashboard.getWidth();
                        int dh = dashboard.getHeight();
                        
                        if (dashboard.isMinimized()) {
                            if (dashboard.isIconHit(mouseCellX, mouseCellY, 2, currentRows - 2)) {
                                dashboard.toggleMinimize();
                            }
                        } else {
                            // Check if click was on the bottom-right resize handle
                            if (dashboard.isResizeClick(mouseCellX, mouseCellY)) {
                                isResizing = true;
                            } else if (mouseCellX >= dx && mouseCellX < dx + dw && mouseCellY == dy) {
                            // Header row: check buttons first, then fall through to drag
                            if (dashboard.isCloseClick(mouseCellX, mouseCellY)) {
                                System.exit(0);
                            } else if (dashboard.isMinimizeClick(mouseCellX, mouseCellY)) {
                                // Minimize on press, track for release behavior
                                if (!dashboard.isMinimized()) {
                                    dashboard.toggleMinimize();
                                }
                                isMinimizePressed = true;
                            } else {
                                isDragging = true;
                                dragOffsetX = mouseCellX - dx;
                                dragOffsetY = mouseCellY - dy;
                            }
                            }
                        }
                    } else {
                        isDragging = false;
                        isResizing = false;
                        // Handle minimize button release
                        if (isMinimizePressed) {
                            if (dashboard.isMinimizeClick(mouseCellX, mouseCellY)) {
                                // Released inside button - restore
                                if (dashboard.isMinimized()) {
                                    dashboard.toggleMinimize();
                                }
                            }
                            // Released outside button - stay minimized (do nothing)
                            isMinimizePressed = false;
                        }
                    }
                } else if (buttonId == 1) {
                    isRightPressed = isPressed;
                    if (isPressed) clickCount++;
                }

                if (!isDragging) {
                    dashboard.handleMouseClick(mouseCellX, mouseCellY, isPressed);
                }
            }

            @Override
            public void onMouseWheel(long deviceHandle, int delta) {}
        });

        // Safe cleanup shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.print(FastANSI.ALT_BUFFER_OFF + FastANSI.CURSOR_SHOW + FastANSI.RESET);
            try {
                FastTerminal.setSystemCursorVisible(true);
            } catch (Throwable ignored) {}
            try {
                keyboard.stopListening();
            } catch (Throwable ignored) {}
            try {
                mouse.close();
            } catch (Throwable ignored) {}
        }));

        long frameCount = 0;

        while (true) {
            long startTime = System.currentTimeMillis();

            // Update cursor visibility based on terminal focus and mouse hover
            boolean shouldHide = false;
            try {
                shouldHide = FastTerminal.isTerminalFocused() && FastTerminal.isMouseOverTerminal();
            } catch (Throwable ignored) {}
            if (shouldHide != lastCursorHiddenState) {
                try {
                    FastTerminal.setSystemCursorVisible(!shouldHide);
                    lastCursorHiddenState = shouldHide;
                } catch (Throwable ignored) {}
            }

            // Handle terminal resizing
            int[] currentSize = FastTerminal.getWindowSize(cols, rows);
            if (renderer.resize(currentSize[0], currentSize[1])) {
                cols = currentSize[0];
                rows = currentSize[1];
                currentCols = cols;
                currentRows = rows;
                canvas.resize(cols, rows);
                
                // Dynamically reposition dashboard container to stay perfectly centered
                int newX = Math.max(0, (cols - dashboard.getWidth()) / 2);
                int newY = Math.max(0, (rows - dashboard.getHeight()) / 2);
                dashboard.setX(newX);
                dashboard.setY(newY);
            }

            // Update real-time console metrics from active window
            try {
                int[] currentWinInfo = FastTerminal.getConsoleWindowInfo();
                if (currentWinInfo != null && currentWinInfo.length >= 8) {
                    clientX = currentWinInfo[2];
                    clientY = currentWinInfo[3];
                    fontW = currentWinInfo[4];
                    fontH = currentWinInfo[5];
                    clientWidth = currentWinInfo[6];
                    clientHeight = currentWinInfo[7];
                }
            } catch (Throwable ignored) {}

            canvas.clear();

            // 1. Render beautiful high-resolution background image (or fallback gradient)
            if (backgroundImage != null) {
                int imgW = backgroundImage.getWidth();
                int imgH = backgroundImage.getHeight();
                
                double fwVal = fontW > 0 ? fontW : 8.0;
                double fhVal = fontH > 0 ? fontH : 16.0;
                
                double viewportW = cols * fwVal;
                double viewportH = rows * fhVal;
                
                double rImg = (double) imgW / imgH;
                double rTerm = viewportW / viewportH;
                
                if (rImg > rTerm) {
                    // Image is wider than terminal. Match height, crop sides.
                    double scaleX = fwVal * imgH / viewportH;
                    double offsetX = (imgW - cols * scaleX) / 2.0;
                    
                    for (int y = 0; y < rows; y++) {
                        double imgYTopFloat = (2 * y + 0.5) * imgH / (2.0 * rows);
                        double imgYBotFloat = (2 * y + 1.5) * imgH / (2.0 * rows);
                        
                        int imgYTop = Math.max(0, Math.min(imgH - 1, (int) imgYTopFloat));
                        int imgYBot = Math.max(0, Math.min(imgH - 1, (int) imgYBotFloat));
                        
                        for (int x = 0; x < cols; x++) {
                            int imgX = Math.max(0, Math.min(imgW - 1, (int) ((x + 0.5) * scaleX + offsetX)));
                            int colorTop = backgroundImage.getRGB(imgX, imgYTop) & 0xFFFFFF;
                            int colorBot = backgroundImage.getRGB(imgX, imgYBot) & 0xFFFFFF;
                            canvas.writeCell(x, y, '▄', colorBot, colorTop);
                        }
                    }
                } else {
                    // Image is taller than terminal. Match width, crop top/bottom.
                    double scaleY = (fhVal / 2.0) * imgW / viewportW;
                    double offsetY = (imgH - (2.0 * rows) * scaleY) / 2.0;
                    
                    for (int y = 0; y < rows; y++) {
                        int imgYTop = Math.max(0, Math.min(imgH - 1, (int) ((2 * y + 0.5) * scaleY + offsetY)));
                        int imgYBot = Math.max(0, Math.min(imgH - 1, (int) ((2 * y + 1.5) * scaleY + offsetY)));
                        
                        for (int x = 0; x < cols; x++) {
                            int imgX = Math.max(0, Math.min(imgW - 1, (int) ((x + 0.5) * imgW / cols)));
                            int colorTop = backgroundImage.getRGB(imgX, imgYTop) & 0xFFFFFF;
                            int colorBot = backgroundImage.getRGB(imgX, imgYBot) & 0xFFFFFF;
                            canvas.writeCell(x, y, '▄', colorBot, colorTop);
                        }
                    }
                }
            } else {
                // Procedural fallback gradient grid if file loading fails
                for (int y = 0; y < rows; y++) {
                    for (int x = 0; x < cols; x++) {
                        int r = (int) (40 + 30 * Math.sin(x * 0.05 + y * 0.1));
                        int g = (int) (20 + 15 * Math.cos(y * 0.08));
                        int b = (int) (80 + 40 * Math.sin(y * 0.05));
                        int colorTop = ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);

                        int r2 = (int) (40 + 30 * Math.sin(x * 0.05 + (2 * y + 1) * 0.05));
                        int g2 = (int) (20 + 15 * Math.cos((2 * y + 1) * 0.04));
                        int b2 = (int) (80 + 40 * Math.sin((2 * y + 1) * 0.025));
                        int colorBot = ((r2 & 0xFF) << 16) | ((g2 & 0xFF) << 8) | (b2 & 0xFF);

                        canvas.writeCell(x, y, '▄', colorBot, colorTop);
                    }
                }
            }

            // 2. Render the interactive dashboard panel
            dashboard.render(canvas);
            if (dashboard.isMinimized()) {
                dashboard.renderDesktopIcon(canvas, 2, rows - 2);
            }

            int mx = mouseCellX;
            int my = mouseCellY;

            // 5. Draw beautiful custom glowing ANSI Mouse Cursor on top of everything (single block)
            if (mx >= 0 && mx < cols && my >= 0 && my < rows) {
                int cursorFg = 0xFFFFFF; // Crisp white cursor body
                int cursorBg = isLeftPressed ? 0xEF4444 : isRightPressed ? 0x3B82F6 : 0x10B981; // Glow matching click state (Red/Blue/Green)
                
                // Draw main pointer cell with 40% transparent background alpha blending
                canvas.writeCellAlpha(mx, my, '↖', cursorFg, cursorBg, 1.0, 0.4);
            }

            renderer.render();

            frameCount++;
            long elapsed = System.currentTimeMillis() - startTime;
            long sleepTime = (1000 / 120) - elapsed; // Render at butter-smooth 120 FPS
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException ignored) {}
            }
        }
    }
}
