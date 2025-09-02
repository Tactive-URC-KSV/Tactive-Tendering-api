@echo off
echo Terminating all java.exe processes...

taskkill /F /IM java.exe >nul 2>&1

IF %ERRORLEVEL% EQU 0 (
    echo All java.exe processes terminated successfully.
) ELSE (
    echo No java.exe processes found or an error occurred.
)
