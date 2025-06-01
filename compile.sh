#!/usr/bin/env bash

# Asegurar que estamos en el directorio del proyecto
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

echo "🔍 Verificando y limpiando Gradle Daemons..."
./gradlew --stop

echo "🚀 Iniciando compilación..."
./gradlew clean && ./gradlew assembleDebug --parallel --build-cache

BUILD_RESULT=$?
if [ $BUILD_RESULT -eq 0 ]; then
    echo "✅ Compilación completada exitosamente"
    
    echo "🧹 Limpiando Gradle después de la compilación..."
    ./gradlew --stop
    rm -rf .gradle/
    rm -rf build/
    rm -rf app/build/
    echo "✨ Limpieza post-compilación completada"
else
    echo "❌ La compilación falló con código de error: $BUILD_RESULT"
fi

exit $BUILD_RESULT 