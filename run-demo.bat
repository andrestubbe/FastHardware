@echo off
echo ===========================================
echo FastHardware Demo (Native PDH)
echo ===========================================

cd examples\Demo
call mvn clean compile exec:java
cd ..\..
pause
