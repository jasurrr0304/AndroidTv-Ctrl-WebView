#!/bin/bash

# Script para compilar e instalar la aplicación en Smart TV

echo "🔨 Compilando aplicación..."
./gradlew assembleDebug

if [ $? -eq 0 ]; then
    echo "✅ Compilación exitosa"
    
    echo "📱 Detectando dispositivos..."
    adb devices
    
    echo "📦 Instalando en dispositivo..."
    ./gradlew installDebug
    
    if [ $? -eq 0 ]; then
        echo "✅ Instalación completa"
        echo "🚀 Iniciando aplicación..."
        adb shell am start -n com.smarttv.webview/.MainActivity
    else
        echo "❌ Error en la instalación"
    fi
else
    echo "❌ Error en la compilación"
fi
