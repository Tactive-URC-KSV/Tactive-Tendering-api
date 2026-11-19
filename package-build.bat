@echo off
setlocal enabledelayedexpansion

echo ========================================================
echo   Building and Packaging Tactive Backend for Deployment
echo ========================================================

set "SCRIPT_DIR=%~dp0"
set "DIST_DIR=%SCRIPT_DIR%dist"

:: 1. Clean previous dist folder
if exist "%DIST_DIR%" rmdir /s /q "%DIST_DIR%"
mkdir "%DIST_DIR%"
mkdir "%DIST_DIR%\logs"

:: 2. Build Spring Boot JAR
echo [1/2] Building Spring Boot application...
cd /d "%SCRIPT_DIR%tactive-tendering"
call mvnw.cmd clean package -DskipTests
if errorlevel 1 (
    echo [ERROR] Spring Boot Maven build failed!
    exit /b 1
)

:: Copy JAR to dist
for %%f in ("%SCRIPT_DIR%tactive-tendering\target\*.jar") do (
    copy /y "%%f" "%DIST_DIR%\tactive.jar" >nul
)
echo [OK] Spring Boot JAR built and copied to dist\tactive.jar.

:: 3. Package OCR language data when it is installed beside the source
if exist "%SCRIPT_DIR%tessdata\eng.traineddata" (
    echo Packaging English Tesseract model...
    xcopy "%SCRIPT_DIR%tessdata" "%DIST_DIR%\tessdata" /E /I /Q /Y
) else (
    echo [INFO] Using the bundled English Tesseract model. An external tessdata folder is optional.
)

:: 4. Copy Startup & Shutdown Scripts
echo [2/2] Copying deployment scripts...
copy /y "%SCRIPT_DIR%start-app.bat" "%DIST_DIR%\" >nul
copy /y "%SCRIPT_DIR%stop-app.bat" "%DIST_DIR%\" >nul

echo ========================================================
echo   Build Completed Successfully!
echo   Deployable files are staged in: %DIST_DIR%
echo ========================================================
exit /b 0
