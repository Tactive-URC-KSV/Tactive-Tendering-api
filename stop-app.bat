@echo off
REM -----------------------------
REM Stop Spring Boot application
REM -----------------------------

SET JAR_NAME=tactive.jar

echo Stopping Spring Boot application...

for /f "skip=1 tokens=2 delims=," %%a in ('wmic process where "CommandLine like '%%tactive.jar%%'" get ProcessId /format:csv') do (
    if not "%%a"=="" (
        echo Found process PID: %%a
        taskkill /F /PID %%a
        goto :done
    )
)

echo No running process found for %JAR_NAME%.

:done
echo Application stopped.
pause
