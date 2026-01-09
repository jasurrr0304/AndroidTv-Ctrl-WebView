# SmartTV WebView Browser

Una aplicación Android optimizada para Smart TVs de bajos recursos que permite navegar por múltiples páginas web mediante WebView con soporte completo para control remoto.

## 📱 Características

- ✅ **Android 13 (API 33)** - Compatible con las últimas versiones
- ✅ **Optimizado para Smart TV** - Interfaz diseñada para televisores
- ✅ **Bajos recursos** - Configuraciones optimizadas para dispositivos con recursos limitados
- ✅ **WebView completo** - Navegación web completa con JavaScript
- ✅ **Lista de URLs** - Gestión de múltiples páginas web
- ✅ **Agregar URLs personalizadas** - Ingreso manual de nuevas direcciones
- ✅ **Control remoto** - Navegación completa con D-Pad
- ✅ **Modo landscape** - Optimizado para pantallas horizontales

## 🚀 Requisitos

- **Android Studio** Arctic Fox o superior
- **JDK 11** o superior
- **Gradle 8.0** o superior
- **Dispositivo Smart TV** con Android 5.0 (API 21) o superior

## 📦 Instalación

### 1. Clonar o descargar el proyecto

```bash
cd /Users/ces4rnm/Documents/Archivos/Projects/SmartTV-WebView
```

### 2. Abrir en Android Studio

1. Abre Android Studio
2. Selecciona "Open an existing project"
3. Navega a la carpeta del proyecto
4. Espera a que Gradle sincronice las dependencias

### 3. Compilar el proyecto

```bash
./gradlew assembleDebug
```

### 4. Instalar en tu Smart TV

#### Opción A: Conectar por USB
```bash
./gradlew installDebug
```

#### Opción B: Conectar por ADB WiFi
```bash
# Primero conecta tu TV por USB y ejecuta:
adb tcpip 5555

# Luego conecta por WiFi (reemplaza con la IP de tu TV):
adb connect 192.168.1.XXX:5555

# Instala la app:
./gradlew installDebug
```

## 🎮 Uso

### Navegación con Control Remoto

- **← (Izquierda)**: Abrir panel de URLs
- **→ (Derecha)**: Cerrar panel de URLs
- **↑↓ (Arriba/Abajo)**: Navegar por la lista de URLs
- **OK/Enter**: Seleccionar URL o confirmar acción
- **Back**: Retroceder en el navegador o salir

### Agregar una Nueva URL

1. Presiona **← (Izquierda)** para abrir el panel lateral
2. Usa el control remoto para navegar al campo de texto
3. Ingresa la URL (automáticamente agrega "https://" si es necesario)
4. Presiona "Agregar URL"
5. La página se cargará automáticamente

### URLs Predefinidas

La aplicación viene con estas URLs por defecto:
- Google
- YouTube
- Wikipedia
- Netflix
- Prime Video

## 🔧 Optimizaciones para Bajos Recursos

El proyecto incluye varias optimizaciones:

### En build.gradle
```gradle
minifyEnabled true          // Reduce tamaño del APK
shrinkResources true        // Elimina recursos no usados
```

### En WebView
```kotlin
blockNetworkImage = false   // Carga eficiente de imágenes
cacheMode = LOAD_DEFAULT    // Usa caché para mejor rendimiento
setAppCacheEnabled(true)    // Cache de aplicación activo
```

### En AndroidManifest.xml
```xml
android:hardwareAccelerated="true"  // Aceleración por hardware
android:largeHeap="true"            // Más memoria disponible
```

## 📁 Estructura del Proyecto

```
SmartTV-WebView/
├── app/
│   ├── build.gradle                    # Configuración del módulo
│   ├── proguard-rules.pro             # Reglas de ofuscación
│   └── src/main/
│       ├── AndroidManifest.xml        # Manifest de la app
│       ├── java/com/smarttv/webview/
│       │   ├── MainActivity.kt        # Activity principal
│       │   └── UrlAdapter.kt          # Adaptador RecyclerView
│       └── res/
│           ├── layout/
│           │   ├── activity_main.xml  # Layout principal
│           │   └── item_url.xml       # Item de lista
│           ├── drawable/              # Recursos visuales
│           └── values/
│               └── strings.xml        # Strings de la app
├── build.gradle                       # Gradle del proyecto
├── settings.gradle                    # Configuración de módulos
└── gradle.properties                  # Propiedades de Gradle
```

## 🎨 Personalización

### Cambiar URLs Predefinidas

Edita el archivo [MainActivity.kt](app/src/main/java/com/smarttv/webview/MainActivity.kt#L84-L91):

```kotlin
private fun loadDefaultUrls() {
    urlList.addAll(listOf(
        "https://www.tusitio.com",
        "https://www.otrasitio.com"
    ))
}
```

### Cambiar Tema de Colores

Edita los archivos drawable en [res/drawable/](app/src/main/res/drawable/):
- `button_bg.xml` - Color de botones
- `item_bg.xml` - Fondo de items de lista
- `edittext_bg.xml` - Campo de texto

### Ajustar Configuración de WebView

Modifica la función `setupWebView()` en [MainActivity.kt](app/src/main/java/com/smarttv/webview/MainActivity.kt#L76-L123)

## 🐛 Solución de Problemas

### La app no se instala en la TV
1. Verifica que la depuración USB esté habilitada en la TV
2. Asegúrate de que ADB reconoce el dispositivo: `adb devices`
3. Intenta reiniciar ADB: `adb kill-server && adb start-server`

### Las páginas web no cargan
1. Verifica la conexión a internet de la TV
2. Comprueba que los permisos de internet estén configurados
3. Revisa los logs: `adb logcat | grep WebView`

### La navegación con control remoto no funciona
1. Asegúrate de que tu TV tiene soporte Leanback
2. Verifica que la app se inicie desde el launcher de TV
3. Comprueba que los elementos UI tengan `focusable="true"`

## 📊 Rendimiento

### Tamaño del APK
- **Debug**: ~2-3 MB
- **Release**: ~1-2 MB (con ProGuard)

### Memoria RAM requerida
- **Mínimo**: 512 MB
- **Recomendado**: 1 GB

### Versiones Android soportadas
- **Mínimo**: Android 5.0 (API 21)
- **Target**: Android 13 (API 33)

## 🔐 Permisos

La aplicación requiere:
- `INTERNET` - Para cargar páginas web
- `ACCESS_NETWORK_STATE` - Para verificar conectividad

## 📝 Licencia

Este proyecto es de código abierto y está disponible bajo la licencia MIT.

## 🤝 Contribuciones

Las contribuciones son bienvenidas. Para cambios importantes:

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📧 Soporte

Para reportar problemas o sugerencias, abre un issue en el repositorio.

## ✅ Checklist de Desarrollo

- [x] Configuración básica del proyecto
- [x] WebView funcional
- [x] Lista de URLs
- [x] Agregar URLs personalizadas
- [x] Navegación con control remoto
- [x] Optimizaciones de rendimiento
- [x] Soporte para Smart TV
- [ ] Gestión de favoritos persistente
- [ ] Historial de navegación
- [ ] Modo incógnito
- [ ] Configuración de zoom

---

**Desarrollado para Smart TVs de bajos recursos** 📺
