#!/usr/bin/env bash

# Asegurar que estamos en el directorio del proyecto
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Verificar cuántos daemons están activos
ACTIVE_DAEMONS=$(./gradlew --status | grep "IDLE" | wc -l)

echo "🔍 Verificando Gradle Daemons..."
if [ "$ACTIVE_DAEMONS" -gt 1 ]; then
    echo "Hay $ACTIVE_DAEMONS daemons activos. Deteniendo daemons extras..."
    ./gradlew --stop
fi

echo "🚀 Iniciando compilación..."
./gradlew clean && ./gradlew :app:assembleDebug --parallel --build-cache

BUILD_RESULT=$?
if [ $BUILD_RESULT -eq 0 ]; then
    echo "✅ Compilación completada exitosamente"
    
    echo "📱 Instalando APK en el dispositivo..."
    # Verificar si hay dispositivos conectados
    DEVICES=$(adb devices | grep -v "List" | grep "device" | wc -l)
    if [ "$DEVICES" -gt 0 ]; then
        ./gradlew :app:installDebug
        INSTALL_RESULT=$?
        if [ $INSTALL_RESULT -eq 0 ]; then
            echo "✅ APK instalada exitosamente"
            echo "🚀 Lanzando aplicación..."
            adb shell am start -n com.aranthalion.presupuesto/.MainActivity
        else
            echo "❌ Error al instalar la APK"
        fi
    else
        echo "❌ No se encontraron dispositivos Android conectados"
    fi
    
    echo "🧹 Limpiando archivos temporales..."
    rm -rf build/
    rm -rf app/build/
    echo "✨ Limpieza completada"
else
    echo "❌ La compilación falló con código de error: $BUILD_RESULT"
fi

exit $BUILD_RESULT 