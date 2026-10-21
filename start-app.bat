@echo off
setlocal

REM -------------------------------------------------------------
REM Tactive Backend Startup Script (Spring Boot + FastAPI OCR)
REM -------------------------------------------------------------

:: Determine Root Directory
SET "APP_DIR=%~dp0"
IF "%APP_DIR:~-1%"=="\" SET "APP_DIR=%APP_DIR:~0,-1%"
IF NOT EXIST "%APP_DIR%\tactive.jar" (
    IF EXIST "E:\APP\Application\TactiveKSV\tactive.jar" (
        SET "APP_DIR=E:\APP\Application\TactiveKSV"
    )
)

SET "JAR_PATH=%APP_DIR%\tactive.jar"
SET "CONFIG_PATH=%APP_DIR%\config\application-uat.properties"
SET "LOG_DIR=%APP_DIR%\logs"
SET "SPRING_LOG=%LOG_DIR%\app.log"
SET "OCR_LOG=%LOG_DIR%\ocr.log"
SET "OCR_DIR=%APP_DIR%\ocr-service"

:: Create logs folder if missing
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

:: Locate Python for FastAPI
SET "PYTHON_EXE=python"
if exist "%OCR_DIR%\venv\Scripts\python.exe" (
    SET "PYTHON_EXE=%OCR_DIR%\venv\Scripts\python.exe"
) else if exist "%APP_DIR%\venv\Scripts\python.exe" (
    SET "PYTHON_EXE=%APP_DIR%\venv\Scripts\python.exe"
)

echo Starting Tactive Backend Services from %APP_DIR%...

:: 1. Start Spring Boot in Background
echo Starting Spring Boot application (Port 2087)...
if exist "%CONFIG_PATH%" (
    start "Tactive-SpringBoot" /B cmd /c "java -jar "%JAR_PATH%" --server.port=2087 --ocr.service.url=http://localhost:2088 --spring.config.additional-location=file:"%CONFIG_PATH%" >> "%SPRING_LOG%" 2>&1"
) else (
    start "Tactive-SpringBoot" /B cmd /c "java -jar "%JAR_PATH%" --server.port=2087 --ocr.service.url=http://localhost:2088 >> "%SPRING_LOG%" 2>&1"
)

:: 2. Start FastAPI OCR Service in Background
if exist "%OCR_DIR%\app.py" (
    echo Starting FastAPI OCR Service (Port 2088)...
    cd /d "%OCR_DIR%"
    start "Tactive-FastAPI" /B cmd /c ""%PYTHON_EXE%" -m uvicorn app:app --host 0.0.0.0 --port 2088 >> "%OCR_LOG%" 2>&1"
    cd /d "%APP_DIR%"
) else (
    echo [WARNING] OCR service not found at %OCR_DIR%. Skipping FastAPI startup.
)

echo.
echo =========================================================
echo Both services started in the background!
echo   Spring Boot Log: %SPRING_LOG%
echo   FastAPI OCR Log: %OCR_LOG%
echo =========================================================

exit /b 0
