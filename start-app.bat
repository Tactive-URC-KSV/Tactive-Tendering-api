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

echo Starting Spring Boot application...
java -jar "%JAR_PATH%" --spring.config.additional-location=file:"%CONFIG_PATH%" > "%LOG_FILE%" 2>&1

echo Data loading process completed. Logs at %LOG_FILE%.
exit /b 0
