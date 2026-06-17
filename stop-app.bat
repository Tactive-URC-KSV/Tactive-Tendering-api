@echo off
echo Stopping all Java processes...

:: Kill java.exe
taskkill /F /IM java.exe /T >nul 2>&1

:: Kill javaw.exe
taskkill /F /IM javaw.exe /T >nul 2>&1

:: Optional: kill by port (example 8080)
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080') do (
    taskkill /F /PID %%a >nul 2>&1
)

echo Done.
pause
