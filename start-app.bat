@echo off
REM -----------------------------
REM Spring Boot application startup
REM -----------------------------

REM Path to your JAR
SET JAR_PATH=E:\APP\Application\TactiveKSV\tactive.jar

REM Path to Spring config folder (if any)
SET CONFIG_PATH=E:\APP\Application\TactiveKSV\config\application-uat.properties

REM Path to log file
SET LOG_FILE=E:\APP\Application\TactiveKSV\logs\app.log

echo Checking if port 8080 is in use...

FOR /F "tokens=5" %%P IN ('netstat -ano ^| findstr :8080') DO (
    echo Port 8080 is in use by PID %%P. Stopping it...
    taskkill /PID %%P /F
)

echo Starting Spring Boot application...
java -jar "%JAR_PATH%" --spring.config.additional-location=file:"%CONFIG_PATH%" > "%LOG_FILE%" 2>&1

echo Spring Boot app started successfully. Logs at %LOG_FILE%.
pause
