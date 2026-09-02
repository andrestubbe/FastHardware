@echo off
setlocal
chcp 65001 > nul
cd /d "%~dp0"

echo ===================================================
echo   FastHardware Terminal Demo
echo   Real-Time CPU / Temp / RAM / GPU Monitor
echo ===================================================
echo.

echo [1/2] Building Demo...
cd examples\Demo
call mvn compile dependency:copy-dependencies -U -DincludeScope=runtime -DskipTests -q
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Build failed!
    cd ..\..
    pause
    exit /b %ERRORLEVEL%
)

echo [2/2] Launching Terminal Demo...  ^(Ctrl+C to exit^)
echo.
java --enable-native-access=ALL-UNNAMED -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -cp "target/classes;target/dependency/*" fasthardware.Demo

cd ..\..
pause
