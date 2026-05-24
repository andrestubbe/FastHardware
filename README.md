# FastHardware — Native Hardware Telemetry API for Java [v0.1.0]

**A zero-overhead native module for the FastJava ecosystem. Monitor CPU, GPU, RAM, and Disk health directly via native Windows APIs.**

[![Status](https://img.shields.io/badge/status-v0.1.0-brightgreen.svg)](https://github.com/andrestubbe/FastHardware/releases/tag/v0.1.0)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-ready-green.svg)](https://jitpack.io/#andrestubbe)

---

**FastHardware** provides direct access to system performance counters and hardware sensors. By using native Win32/WMI hooks, it delivers accurate telemetry without the performance hit of traditional Java system-querying methods.

## Table of Contents
- [Features](#features)
- [Installation](#installation)
- [License](#license)

## Features
- **📊 Real-Time Stats**: Monitor CPU usage, GPU temperature, and RAM availability.
- **⚡ Ultra-Fast Queries**: Low-latency hardware status updates.
- **📦 Zero Overhead**: Efficient native implementation bypassing heavy abstractions.
- **🚀 High Accuracy**: Real OS-level data for performance monitoring tools.

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

### Option 2: Gradle (via JitPack)
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:fasthardware:v0.1.0'
    implementation 'com.github.andrestubbe:fastcore:v0.1.0'
}
```

### Option 3: Direct Download (No Build Tool)
Download the latest JARs directly to add them to your classpath:

1. 📦 **[fasthardware-v0.1.0.jar](https://github.com/andrestubbe/FastHardware/releases/download/v0.1.0/fasthardware-v0.1.0.jar)** (The Core Library)
2. ⚙️ **[fastcore-v0.1.0.jar](https://github.com/andrestubbe/FastCore/releases/download/v0.1.0/fastcore-v0.1.0.jar)** (The Mandatory Native Loader)

> [!IMPORTANT]
> All JARs must be in your classpath for the native JNI calls to function correctly.


## Documentation
*   **[ARCHITECTURE.md](ARCHITECTURE.md)**: Details on PDH and WMI implementation.
*   **[REFERENCE.md](REFERENCE.md)**: Full API and JNI contracts.
*   **[ROADMAP.md](ROADMAP.md)**: Future development and milestones.

## License
MIT License — See [LICENSE](LICENSE) for details.

---
**Part of the FastJava Ecosystem** — *Making the JVM faster.*
