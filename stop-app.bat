@echo off
setlocal
echo =========================================================
echo Stopping Tactive Backend Services (Spring Boot + FastAPI)
echo =========================================================

:: 1. Stop FastAPI OCR Service (Port 2088)
echo Stopping FastAPI OCR service on port 2088...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :2088 ^| findstr LISTENING') do (
    echo Terminating PID %%a on port 2088...
    taskkill /F /PID %%a >nul 2>&1
)

:: 2. Stop Spring Boot Application (Port 2087 & Java)
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
