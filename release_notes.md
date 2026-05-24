# FastHardware v0.1.0 — Initial Release 🚀

## 🎉 Version 0.1.0: Native Hardware Telemetry
**Release Date:** 2026-05-24  
**Tag:** `v0.1.0`

---

## ✨ Features

### 📊 Real-Time Metrics
- **CPU (PDH):** Zero-latency access to global and per-core processor times.
- **RAM (Win32):** Direct physical memory status via `GlobalMemoryStatusEx`.
- **Sensors (WMI):** Access to ACPI thermal zone temperatures for CPU and GPU monitoring.

### 🚀 Zero-Allocation JNI
- The Java-side interface guarantees zero-GC overhead during continuous high-frequency hardware polls, bridging native data types seamlessly.

---

## 📦 Installation (JitPack)

### Maven
```xml
<dependencies>
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>fasthardware</artifactId>
        <version>v0.1.0</version>
    </dependency>

    <!-- FastCore (Required Native Loader) -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>fastcore</artifactId>
        <version>v0.1.0</version>
    </dependency>
</dependencies>
```

---

## 🔧 Technical Details
- **Native DLL:** `fasthardware.dll` (bundled inside JAR).
- **Architecture:** Windows JNI (PDH, WMI).
- **Build System:** Standardized Maven pipeline via GitHub Actions CMake CI.

---

## 🙏 Credits
- **FastCore:** Unified JNI loading engine.

**Part of the FastJava Ecosystem** — *Making the JVM faster.*
