@echo off
echo Terminating all java.exe and javaw.exe processes...

echo Current Java Processes:
tasklist | findstr java

taskkill /F /T /IM java.exe
taskkill /F /T /IM javaw.exe

IF %ERRORLEVEL% EQU 0 (
    echo Java processes terminated successfully.
) ELSE (
    echo No java processes found or an error occurred during termination.
)
