# FastHardware 0.1.1 — Native Hardware Telemetry API for Java

[![Status](https://img.shields.io/badge/status-0.1.1-brightgreen.svg)](https://github.com/andrestubbe/FastHardware/releases/tag/0.1.1)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-ready-green.svg)](https://jitpack.io/#andrestubbe/FastHardware)

**⚡ Zero-overhead native hardware telemetry for Java. Monitor CPU usage, CPU temperature, RAM, and GPU temperature directly via Win32 PDH and WMI — no JMX, no process spawning, no bloat.**

**FastHardware** bypasses the JVM's heavy `OperatingSystemMXBean` and shell-based `wmic` calls entirely. By binding directly to Win32 PDH counters and WMI COM objects via JNI, it delivers accurate, low-latency hardware telemetry at native speed.

[![FastHardware Showcase](docs/screenshot.png)](https://www.youtube.com/watch?v=BZsqQl7WqWk)

---

## Table of Contents
- [Features](#features)
- [Quick Start](#quick-start)
- [API Quick Reference](#api-quick-reference)
- [Performance Benchmarks](#performance-benchmarks)
- [Examples & Demos](#examples--demos)
- [Installation](#installation)
- [Documentation](#documentation)
- [Platform Support](#platform-support)
- [Related Projects](#related-projects)
- [License](#license)

---

## Features

- **📊 Real-Time Telemetry** — CPU usage %, per-core CPU usage, CPU temperature, physical RAM, GPU temperature.
- **⚡ Native Win32 Speed** — PDH counters (`\\Processor(_Total)\\% Processor Time`) registered once, polled in microseconds. RAM via `GlobalMemoryStatusEx` (direct kernel table read). Temperature via WMI `MSAcpi_ThermalZoneTemperature` in `ROOT\WMI`.
- **🧊 Zero Overhead** — All JNI calls use primitives (`jlong`, `jdouble`, `jdoubleArray`). No heap allocation per query.
- **📦 Atomic Snapshot** — `getSnapshot()` returns a frozen `HardwareSnapshot` record with all fields captured in a single native round-trip.
- **🔌 FastCore Auto-Load** — `fasthardware.dll` is embedded in the JAR. `FastCore` extracts and loads it at runtime — no manual DLL management.

---

## Quick Start

```java
// 1. Create a hardware monitor instance (initializes PDH + WMI once)
FastHardware hw = FastHardware.create();

// First PDH sample needs ~1s interval to compute CPU rate
Thread.sleep(1100);

// 2. Get an atomic snapshot of all telemetry
HardwareSnapshot snap = hw.getSnapshot();

System.out.printf("CPU:      %.1f%%%n",   snap.cpuUsagePercent());
System.out.printf("CPU Temp: %.1f°C%n",   snap.cpuTemperatureCelsius());
System.out.printf("RAM:      %d MB free / %d MB total%n",
    snap.freeRamBytes()  / 1024 / 1024,
    snap.totalRamBytes() / 1024 / 1024);
System.out.printf("GPU Temp: %.1f°C%n",   snap.gpuTemperatureCelsius());
```

> [!IMPORTANT]
> PDH CPU counters require **two collection intervals** to compute a rate. Call `hw.getSnapshot()` once, wait ~1 second, then read real values. FastHardware handles this automatically after the first poll.

---

## API Quick Reference

### `FastHardware` (Interface)

| Method | Returns | Description |
|--------|---------|-------------|
| `FastHardware.create()` | `FastHardware` | Initializes the native library and returns a monitor instance. |
| `getSnapshot()` | `HardwareSnapshot` | Atomic read of all telemetry fields in one native call. |
| `getGlobalCpuUsage()` | `double` | CPU usage 0.0–100.0 via PDH `\\Processor(_Total)\\% Processor Time`. |
| `getPerCoreCpuUsage()` | `double[]` | Per-logical-core CPU usage array via PDH. |
| `getTotalMemoryBytes()` | `long` | Total physical RAM via `GlobalMemoryStatusEx`. |
| `getFreeMemoryBytes()` | `long` | Free physical RAM via `GlobalMemoryStatusEx`. |
| `getCpuTemperatureCelsius()` | `double` | CPU package temperature via WMI `MSAcpi_ThermalZoneTemperature` in `ROOT\WMI`. |
| `getGpuTemperatureCelsius()` | `double` | GPU temperature via WMI (discrete GPUs; `0.0` on Intel integrated). |

### `HardwareSnapshot` (Record)

```java
record HardwareSnapshot(
    double   cpuUsagePercent,
    double[] perCoreCpuUsage,
    long     usedRamBytes,
    long     totalRamBytes,
    double   cpuTemperatureCelsius,
    double   gpuTemperatureCelsius
) {
    long freeRamBytes(); // helper: totalRamBytes - usedRamBytes
}
```

---

## Performance Benchmarks

FastHardware native Win32 JNI vs standard Java `OperatingSystemMXBean` / `Runtime`:

| Metric | Java JMX / Runtime | FastHardware Native | Advantage |
|--------|--------------------|---------------------|-----------|
| Full telemetry snapshot | 3× separate MXBean calls | **1× atomic JNI call** | **3× fewer round-trips** |
| CPU usage query | `getSystemLoadAverage()` (1-min rolling) | **PDH instantaneous** | **Real-time vs delayed** |
| Per-core CPU usage | ❌ No API | **`double[]` per logical core** | **FastHardware exclusive** |
| Free RAM (OS-level) | `Runtime.freeMemory()` (JVM heap only) | **`GlobalMemoryStatusEx` (physical)** | **System-wide accuracy** |
| CPU temperature | ❌ No API | **WMI `ROOT\WMI` ACPI sensor** | **FastHardware exclusive** |
| GPU temperature | ❌ No API | **WMI discrete GPU sensor** | **FastHardware exclusive** |

*Run `run-benchmark.bat` for live JMH throughput numbers on your machine.*

---

## Examples & Demos

| Case | File | Launcher | Description |
|------|------|----------|-------------|
| **Live Terminal Dashboard** | [Demo.java](examples/Demo/src/main/java/fasthardware/Demo.java) | `run-demo.bat` | ANSI terminal monitor — CPU%, CPU°C, RAM, GPU°C as live bars + scrolling sparklines. Pure FastHardware, no extra deps. |
| **JMH Benchmark Suite** | [Benchmark.java](examples/Benchmark/src/main/java/fasthardware/benchmark/Benchmark.java) | `run-benchmark.bat` | 7-group JMH throughput suite comparing FastHardware native vs Java JMX/Runtime. |

---

## Installation

### Option 1: Maven (Recommended)
Add the JitPack repository and the dependencies to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <!-- FastHardware Library -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastHardware</artifactId>
        <version>0.1.1</version>
    </dependency>

    <!-- FastCore (Required Native Loader) -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastCore</artifactId>
        <version>0.1.0</version>
    </dependency>
</dependencies>
```

### Option 2: Gradle (via JitPack)
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:FastHardware:0.1.1'
    implementation 'com.github.andrestubbe:FastCore:0.1.0'
}
```

### Option 3: Direct Download (No Build Tool)
1. 📦 **[FastHardware-0.1.1.jar](https://github.com/andrestubbe/FastHardware/releases/download/0.1.1/FastHardware-0.1.1.jar)** — The Core Library
2. ⚙️ **[FastCore-0.1.0.jar](https://github.com/andrestubbe/FastCore/releases/download/0.1.0/fastcore-0.1.0.jar)** — The Mandatory Native Loader

> [!IMPORTANT]
> Both JARs must be on your classpath. FastCore extracts `fasthardware.dll` to `%USERPROFILE%\.fastcore\native\` at runtime.

---

## Documentation

| File | Description |
|------|-------------|
| [ARCHITECTURE.md](docs/ARCHITECTURE.md) | Win32 PDH, WMI COM, and JNI boundary details. |
| [REFERENCE.md](docs/REFERENCE.md) | Full API specification and JNI contracts. |
| [COMPILE.md](docs/COMPILE.md) | Build guide for the native DLL from source. |
| [PHILOSOPHY.md](docs/PHILOSOPHY.md) | Why native-first telemetry matters in Java. |
| [CHANGELOG.md](docs/CHANGELOG.md) | Version history. |
| [ROADMAP.md](docs/ROADMAP.md) | Future development milestones. |

---

## Platform Support

| Platform | Status |
|----------|--------|
| Windows 10 / 11 (x64) | ✅ Fully Supported |
| Linux | 🔜 Planned |
| macOS | 🔜 Planned |

---

## Related Projects

- [FastCore](https://github.com/andrestubbe/FastCore) — Native JNI DLL loader for the FastJava ecosystem
- [FastDWM](https://github.com/andrestubbe/FastDWM) — Windows DWM bridge (VSync, title bar theming, WinMM timers)
- [FastGPU](https://github.com/andrestubbe/FastGPU) — Vulkan compute kernel dispatch for Java
- [FastDisplay](https://github.com/andrestubbe/FastDisplay) — Native display refresh rate and resolution detection
- [FastExecution](https://github.com/andrestubbe/FastExecution) — Sub-millisecond precision named loop and delay scheduler

---

**Part of the FastJava Ecosystem** — *Making the JVM faster. Small package. Maximum speed. Zero bloat.* 🚀