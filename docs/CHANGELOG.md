# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [0.1.1] - 2026-09-02

### Added
- Terminal ANSI dashboard demo (`run-demo.bat`) — live CPU%, CPU°C, RAM, GPU°C bars + scrolling sparklines. Pure FastHardware, no extra dependencies.
- JMH benchmark suite expanded to 7 groups: FastHardware native vs Java JMX / Runtime across CPU%, per-core CPU, free RAM, total RAM, CPU temp, GPU temp, and full snapshot.
- `sign-natives.bat`, `sign.ps1`, `sign-debug.ps1` — self-signed Authenticode DLL signing scripts (port from FastVulkan).

### Fixed
- **WMI temperature bug**: `initWMI()` previously connected only to `ROOT\CIMV2`, but `MSAcpi_ThermalZoneTemperature` lives in `ROOT\WMI`. A separate `IWbemServices*` connection is now opened for `ROOT\WMI`, making the ACPI thermal zone query reach the correct namespace.
- **Hardcoded fallback removed**: CPU temperature fallback was `45.0` (misleading constant). Fallback is now `0.0` (honest — sensor not available).
- **FastCore version**: Benchmark `pom.xml` fixed from `main-SNAPSHOT` to `0.1.0` (stable release).
- **PDH warm-up**: Demo now performs a silent 1.1s warm-up poll at startup so CPU% shows real data from the first frame.

### Known Limitations
- **CPU temperature on Intel integrated platforms**: `MSAcpi_ThermalZoneTemperature` (WMI `ROOT\WMI`) returns a static BIOS-reported value on many Intel Tiger Lake / Alder Lake / Raptor Lake laptops. The BIOS ACPI thermal zone only updates at critical thresholds, not continuously. This is a firmware limitation, not a FastHardware bug. Discrete GPU systems (NVIDIA/AMD) and desktop motherboards typically provide live readings.
- **GPU temperature on Intel Iris Xe**: Intel integrated graphics expose no WMI thermal zone. `getGpuTemperatureCelsius()` returns `0.0` on Intel-only systems.

## [0.1.0] - 2026-06-07

### Added
- Initial release via JitPack.
- Native C++ JNI bridge using PDH for CPU usage telemetry.
- Win32 `GlobalMemoryStatusEx` implementation for physical RAM.
- WMI COM implementation for ACPI CPU and GPU temperature sensors.
- `FastHardware.create()` factory — initializes PDH + WMI once, amortized over all subsequent calls.
- `HardwareSnapshot` Java 17 record — atomic read of all telemetry.
- Live terminal demo showcasing real-time hardware tracking.