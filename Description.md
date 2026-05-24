# FastHardware

## 1. Vision & Kernidee
**FastHardware** ist das native Modul zur Erfassung globaler System- und Hardware-Telemetrie. 

Während `FastProcess` den Ressourcenverbrauch von *einzelnen* Programmen überwacht, schaut FastHardware auf das "große Ganze". Es liefert dem KI-Agenten den Gesundheitszustand und die absolute Auslastung der physischen Maschine in Echtzeit.

**Warum Memory und CPU zusammen?**
Beides gehört unabdingbar zusammen, wenn man die Leistungsfähigkeit des Systems bewerten will. Die Windows-Performance-APIs (PDH) liefern diese Daten extrem effizient im gleichen Aufwasch. Eine Trennung in `FastCPU` und `FastRAM` würde unnötigen JNI-Overhead erzeugen.

**Warum braucht ein KI-Agent/Bot FastHardware?**
- **Load Balancing:** Bevor der Agent eine rechenintensive Aufgabe startet (z.B. ein lokales LLM laden oder Bilder mit FastImage filtern), prüft er: *Ist noch genug RAM frei? Ist die CPU gerade bei 100%?*
- **Thermal Throttling & Health:** Wenn das Notebook zu heiß wird (Temperatur-Sensoren), kann der Agent seine Framerate (bei FastGhostMouse) drosseln oder Hintergrundaufgaben pausieren, um den Lüfter leise zu halten.
- **Disk I/O:** Ein Check, ob die Festplatte gerade blockiert ist (z.B. durch ein Windows-Update im Hintergrund), verhindert, dass RAG-Suchen unendlich lange hängen.

## 2. Java High-Level API

```java
public interface FastHardware {
    static FastHardware open() { return new FastHardwareImpl(); }

    // Gibt einen Snapshot aller globalen Hardware-Metriken (Zero-Allocation optimiert)
    HardwareSnapshot getSnapshot();

    // Detaillierte Einzelabfragen
    float getGlobalCpuUsage();
    long getFreeMemoryBytes();
    long getTotalMemoryBytes();
    
    // Sensoren
    float getCpuTemperature();
    float getGpuTemperature();
}

public record HardwareSnapshot(
    float cpuUsagePercent,
    long usedRamBytes,
    long totalRamBytes,
    float diskIoActivity,
    float cpuTemperature
) {}
```

## 3. C++ JNI Backend (Win32 PDH & WMI)
Um den Hardware-Zustand extrem performant und ohne ständige WMI-Queries (die oft langsam sind) auszulesen, setzt das Backend auf PDH.

1. **PDH (Performance Data Helper):** Die Windows PDH-API bietet Echtzeit-Zugriff auf Performance-Counter (z.B. `\Processor(_Total)\% Processor Time`). Diese Counter werden einmal registriert und dann im Millisekunden-Takt ohne Overhead abgefragt.
2. **GlobalMemoryStatusEx:** Für sofortige, nanosekunden-schnelle RAM-Abfragen.
3. **WMI / Ring0-Treiber:** Temperaturen auszulesen ist unter Windows traditionell schwer. Hier wird selektiv WMI genutzt (z.B. `MSAcpi_ThermalZoneTemperature`) oder auf verbreitete APIs von GPU-Treibern (NVAPI für NVIDIA-Temperaturen) zurückgegriffen.

## 4. Agent-Kit (KI-Integration)
Ein Agent nutzt FastHardware als "Gesundheits-Check" vor wichtigen Aktionen.

**JSON Schema Beispiel (Health Check):**
```json
{
  "action": "check_system_health"
}
```
**Antwort:**
```json
{
  "status": "HEALTHY",
  "hardware": {
    "cpu_load_percent": 15.2,
    "ram_free_gb": 12.4,
    "thermal_throttling": false
  }
}
```
