@echo off
setlocal
chcp 65001 > nul
cd /d "%~dp0"
set "MAVEN_OPTS=--enable-native-access=ALL-UNNAMED --sun-misc-unsafe-memory-access=allow -Dorg.slf4j.simpleLogger.defaultLogLevel=warn"

echo ===================================================
echo   FastHardware JMH Benchmark Suite
echo   Native Win32/WMI vs Standard Java JMX/Runtime
echo ===================================================
echo.

echo [1/2] Building FastHardware locally and installing to .m2...
call mvn -q clean install -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] FastHardware build failed!
    pause
    exit /b %ERRORLEVEL%
)

echo [2/2] Packaging JMH uber-jar...
cd examples\Benchmark
call mvn -q clean package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] JMH benchmark packaging failed!
    cd ..\..
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo ===================================================
echo   Running JMH Benchmarks (Throughput: ops/ms)
echo   Warmup: 3x 1s   Measurement: 5x 1s   Fork: 1
echo ===================================================
echo.

java --enable-native-access=ALL-UNNAMED --sun-misc-unsafe-memory-access=allow -jar target\benchmarks.jar -f 1 -wi 3 -i 5 -tu ms -bm thrpt 2>&1

cd ..\..
echo.
echo ===================================================
echo   Done. Higher ops/ms = better throughput.
echo ===================================================
pause
