@echo off
setlocal

REM -------------------------------------------------------------
REM Tactive Backend Startup Script (Spring Boot + Java OCR)
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

if not defined OCR_TESSDATA_PATH if exist "%APP_DIR%\tessdata\eng.traineddata" SET "OCR_TESSDATA_PATH=%APP_DIR%\tessdata"
:: Create logs folder if missing
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

echo Starting Tactive Backend Services from %APP_DIR%...

:: 1. Start Spring Boot in Background
echo Starting Spring Boot application on port 2087...
SET "EXTRA_CONFIG="
if exist "%CONFIG_PATH%" SET "EXTRA_CONFIG=--spring.config.additional-location=file:%CONFIG_PATH%"
start "Tactive-SpringBoot" /B cmd /c ""java" -jar "%JAR_PATH%" --server.port=2087 %EXTRA_CONFIG% >> "%SPRING_LOG%" 2>&1"

echo.
echo =========================================================
echo Spring Boot started in the background!
echo   Spring Boot Log: %SPRING_LOG%
echo =========================================================

exit /b 0
