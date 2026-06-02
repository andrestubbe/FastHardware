# FastHardware Roadmap

## v0.1.0 (Current)
*   [x] Establish standard native multi-module repository layout.
*   [x] Native C++ JNI bridge using PDH for CPU telemetry.
*   [x] Win32 GlobalMemoryStatusEx implementation for RAM.
*   [x] WMI COM implementations for ACPI CPU and GPU temperature sensors.
*   [x] Live Terminal Demo showcasing real-time hardware tracking.

## v0.2.0 (Planned)
*   [ ] Add NVAPI (NVIDIA) and ADL (AMD) explicit support for highly accurate GPU telemetry.
*   [ ] Add Disk I/O activity polling via PDH (`\PhysicalDisk(_Total)\% Disk Time`).

## v0.3.0 (Planned)
*   [ ] Asynchronous hardware polling thread (Background Poller) with zero-GC buffers.
*   [ ] Health-Check integration hooks directly into `FastAgent`.
