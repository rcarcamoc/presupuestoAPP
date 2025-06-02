@echo off
setlocal enabledelayedexpansion

echo 🔍 Verificando y limpiando Gradle Daemons...
gradlew --stop

echo 🚀 Iniciando compilación...
gradlew clean && gradlew assembleDebug --parallel --build-cache

if !errorlevel! equ 0 (
    echo ✅ Compilación completada exitosamente
) else (
    echo ❌ La compilación falló con código de error: !errorlevel!
)