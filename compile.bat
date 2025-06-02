@echo off
setlocal enabledelayedexpansion

:: Configuración básica
set "SCRIPT_DIR=%~dp0"
set "APK_PATH=%SCRIPT_DIR%app\build\outputs\apk\debug\app-debug.apk"
set "APK_DEST=%SCRIPT_DIR%presupuesto-debug.apk"

echo [DEBUG] ===== INICIO COMPILACIÓN =====
echo [DEBUG] Directorio actual: %CD%
echo [DEBUG] Configuración:
echo [DEBUG] - SCRIPT_DIR: %SCRIPT_DIR%
echo [DEBUG] - APK_PATH: %APK_PATH%
echo [DEBUG] - APK_DEST: %APK_DEST%

:: Verificar que estamos en el directorio correcto
if not exist "gradlew.bat" (
    echo [ERROR] No se encontró gradlew.bat. Asegúrese de estar en el directorio del proyecto.
    exit /b 1
)

:: Limpiar y compilar
echo [INFO] Limpiando proyecto...
call gradlew clean
if !errorlevel! neq 0 (
    echo [ERROR] Error al limpiar el proyecto
    exit /b 1
)

echo [INFO] Compilando APK...
call gradlew :app:assembleDebug
if !errorlevel! neq 0 (
    echo [ERROR] Error al compilar el proyecto
    exit /b 1
)

:: Copiar APK a la raíz
if exist "%APK_PATH%" (
    echo [INFO] Copiando APK a la raíz del proyecto...
    copy /Y "%APK_PATH%" "%APK_DEST%"
    if !errorlevel! neq 0 (
        echo [ERROR] Error al copiar el archivo APK
        exit /b 1
    )
    echo [INFO] APK copiada exitosamente a: %APK_DEST%
) else (
    echo [ERROR] No se encontró el archivo APK en: %APK_PATH%
    exit /b 1
)

:: Subir APK a GitHub
echo [INFO] Subiendo APK a GitHub...

:: Verificar si hay cambios en el APK
git diff --quiet "%APK_DEST%" 2>nul
if !errorlevel! equ 0 (
    echo [INFO] No hay cambios en el APK, omitiendo subida a GitHub
    goto :firebase_distribution
)

:: Agregar APK al staging
echo [INFO] Agregando APK al staging...
git add "%APK_DEST%"
if !errorlevel! neq 0 (
    echo [ERROR] Error al agregar el APK al staging
    exit /b 1
)

:: Crear commit con la fecha actual
for /f "tokens=2 delims==" %%a in ('wmic OS Get localdatetime /value') do set "dt=%%a"
set "VERSION=1.0.%dt:~0,8%%dt:~8,6%"
echo [INFO] Creando commit con versión !VERSION!...
git commit -m "Compilación !VERSION!"
if !errorlevel! neq 0 (
    echo [ERROR] Error al crear el commit
    exit /b 1
)

:: Subir cambios
echo [INFO] Subiendo cambios a GitHub...
git push origin main
if !errorlevel! neq 0 (
    echo [ERROR] Error al subir los cambios a GitHub
    exit /b 1
)

echo [INFO] APK subida exitosamente a GitHub

:firebase_distribution
:: Distribuir a Firebase
echo [INFO] Distribuyendo APK a Firebase App Distribution...
call gradlew appDistributionUploadDebug
if !errorlevel! neq 0 (
    echo [ERROR] Error al distribuir el APK a Firebase
    exit /b 1
)
echo [INFO] APK distribuida exitosamente a Firebase App Distribution

echo [DEBUG] ===== FIN COMPILACIÓN =====
echo [INFO] Proceso completado exitosamente

endlocal 