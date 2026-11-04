@echo off
setlocal
echo =========================================================
echo Stopping Tactive Backend Service (Spring Boot)
echo =========================================================

:: Stop Spring Boot application on port 2087
echo Stopping Spring Boot application on port 2087...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :2087 ^| findstr LISTENING') do (
    echo Terminating PID %%a on port 2087...
    taskkill /F /PID %%a >nul 2>&1
)

:: Fallback process kill
taskkill /F /IM java.exe /T >nul 2>&1
taskkill /F /IM javaw.exe /T >nul 2>&1

echo.
echo All backend services stopped successfully.
pause
