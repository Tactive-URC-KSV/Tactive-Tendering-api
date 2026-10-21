@echo off
setlocal
echo =========================================================
echo Stopping Tactive Backend Services (Spring Boot + FastAPI)
echo =========================================================

:: 1. Stop FastAPI OCR Service (Port 8000)
echo Stopping FastAPI OCR service on port 8000...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8000 ^| findstr LISTENING') do (
    echo Terminating PID %%a on port 8000...
    taskkill /F /PID %%a >nul 2>&1
)

:: 2. Stop Spring Boot Application (Port 8080 & Java)
echo Stopping Spring Boot application on port 8080...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080 ^| findstr LISTENING') do (
    echo Terminating PID %%a on port 8080...
    taskkill /F /PID %%a >nul 2>&1
)

:: Fallback process kill
taskkill /F /IM java.exe /T >nul 2>&1
taskkill /F /IM javaw.exe /T >nul 2>&1

echo.
echo All backend services stopped successfully.
pause
