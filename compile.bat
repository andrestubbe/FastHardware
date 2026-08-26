@echo off
chcp 65001 > nul
cd /d "%~dp0"

echo ========================================
echo  Compiling FastHardware Native Library
echo ========================================

set "VSWHERE=%ProgramFiles(x86)%\Microsoft Visual Studio\Installer\vswhere.exe"
if exist "%VSWHERE%" (
    for /f "usebackq tokens=*" %%i in (`"%VSWHERE%" -latest -products * -requires Microsoft.VisualStudio.Component.VC.Tools.x86.x64 -property installationPath`) do (
        set "VS_PATH=%%i"
    )
)

if not defined VS_PATH (
    echo ERROR: Visual Studio not found!
    pause
    exit /b 1
)

if not defined JAVA_HOME (
    if exist "C:\Program Files\Java\jdk-26.0.2.1" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-26.0.2.1"
    ) else if exist "C:\Program Files\Java\jdk-25" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-25"
    ) else if exist "C:\Program Files\Java\jdk-17" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-17"
    )
)

echo Visual Studio: %VS_PATH%
echo JAVA_HOME: %JAVA_HOME%

call "%VS_PATH%\VC\Auxiliary\Build\vcvars64.bat"

if not exist build mkdir build
if not exist "src\main\resources\native" mkdir "src\main\resources\native"

cl.exe /O2 /W3 /MD /EHsc /LD ^
   /I "%JAVA_HOME%\include" ^
   /I "%JAVA_HOME%\include\win32" ^
   /I "native\include" ^
   /Fo:build\ ^
   /Fe:build\fasthardware.dll ^
   native\src\*.cpp ^
   pdh.lib user32.lib gdi32.lib advapi32.lib wbemuuid.lib ole32.lib oleaut32.lib ^
   /link /DLL /MACHINE:X64

if %ERRORLEVEL% equ 0 (
    copy /Y build\fasthardware.dll src\main\resources\native\fasthardware.dll
    echo [SUCCESS] fasthardware.dll compiled and copied to src\main\resources\native\
) else (
    echo [ERROR] Compilation failed!
    pause
    exit /b 1
)
