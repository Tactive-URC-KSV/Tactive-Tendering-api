@echo off
REM -----------------------------
REM Stop Spring Boot application
REM -----------------------------

REM Path to your JAR (so we only stop this app, not all Java apps)
SET JAR_NAME=tactive.jar

echo Stopping Spring Boot application...

REM Find the PID of the process running this JAR
for /f "tokens=2" %%a in ('tasklist /FI "IMAGENAME eq java.exe" /v ^| findstr /i %JAR_NAME%') do (
    echo Found process PID: %%a
    taskkill /F /PID %%a
    goto :done
)

echo No running process found for %JAR_NAME%.

:done
echo Application stopped.
pause
