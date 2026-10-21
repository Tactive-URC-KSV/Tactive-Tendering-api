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
echo [1/3] Building Spring Boot application...
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

:: 3. Package FastAPI (OCR Service)
echo [2/3] Packaging FastAPI OCR Service...
mkdir "%DIST_DIR%\ocr-service"
xcopy "%SCRIPT_DIR%ocr-service" "%DIST_DIR%\ocr-service" /E /I /Q /Y /EXCLUDE:%SCRIPT_DIR%package-exclude.txt
echo [OK] FastAPI OCR service staged to dist\ocr-service.

:: 4. Copy Startup & Shutdown Scripts
echo [3/3] Copying deployment scripts...
copy /y "%SCRIPT_DIR%start-app.bat" "%DIST_DIR%\" >nul
copy /y "%SCRIPT_DIR%stop-app.bat" "%DIST_DIR%\" >nul

echo ========================================================
echo   Build Completed Successfully!
echo   Deployable files are staged in: %DIST_DIR%
echo ========================================================
exit /b 0
