@echo off
REM -----------------------------
REM Spring Boot application startup
REM -----------------------------

REM Path to your JAR
SET JAR_PATH=E:\APP\Application\TactiveKSV\tactive.jar

REM Path to Spring config folder (if any)
SET CONFIG_PATH=E:\APP\Application\TactiveKSV\config

REM Path to log file
SET LOG_FILE=E:\APP\Application\TactiveKSV\logs\app.log

REM Stop existing Java processes for this app (optional)
taskkill /F /IM java.exe >nul 2>&1

REM Start the Spring Boot JAR in the background
start "" java -jar "%JAR_PATH%" --spring.config.additional-location=file:"%CONFIG_PATH%" > "%LOG_FILE%" 2>&1

echo Spring Boot app started.
pause
