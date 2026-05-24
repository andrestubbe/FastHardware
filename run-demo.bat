@echo off
echo ===========================================
echo FastHardware Demo (Native PDH)
echo ===========================================

cd examples\Demo
call mvn -q clean compile exec:java
cd ..\..
pause
