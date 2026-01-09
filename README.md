# SmartTV WebView Browser

Una aplicación Android optimizada para Smart TVs de bajos recursos que permite navegar por múltiples páginas web mediante WebView con soporte completo para control remoto.

## 📱 Características

- ✅ **Android 13 (API 33)** - Compatible con las últimas versiones
- ✅ **Optimizado para Smart TV** - Interfaz diseñada para televisores
- ✅ **Bajos recursos** - Configuraciones optimizadas para dispositivos con recursos limitados
- ✅ **WebView completo** - Navegación web completa con JavaScript
- ✅ **Modo Cursor Virtual** - Navegación con puntero controlado por D-Pad
- ✅ **Detección inteligente de videos** - Doble clic para pantalla completa
- ✅ **Múltiples reproductores soportados** - YouTube, HTML5, JW Player, Video.js, Plyr, Flowplayer
- ✅ **Scroll automático** - Al acercarse a los bordes con el cursor
- ✅ **User-Agent configurable** - Cambia entre TV, Móvil y Escritorio
- ✅ **Lista de URLs** - Gestión de múltiples páginas web
- ✅ **Agregar URLs personalizadas** - Ingreso manual de nuevas direcciones
- ✅ **Control remoto completo** - Navegación completa con D-Pad y tecla MENÚ
- ✅ **Bloqueador de anuncios** - Filtrado básico de ads y popups
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

## 🎮 Controles del Control Remoto

### 🔑 Tecla Principal

- **MENÚ/OPCIONES** ⚙️: Abre/Cierra el panel lateral de URLs y configuración

### 📍 Modo Cursor (Activado por defecto)

El modo cursor permite navegar con un puntero virtual en la pantalla:

#### Navegación Básica
- **↑↓←→ (D-Pad)**: Mueve el cursor en la pantalla
- **OK/ENTER (1 clic)**: Hace clic en el elemento bajo el cursor
- **OK/ENTER (2 clics rápidos)**: 🎬 **Detecta y pone videos en pantalla completa**
  - Detecta automáticamente videos HTML5
  - Compatible con YouTube embebido
  - Soporta reproductores: JW Player, Video.js, Plyr, Flowplayer
  - Funciona con Vimeo, Dailymotion y otros reproductores

#### Scroll Automático
- El cursor hace **scroll automático** al acercarse a los bordes de la pantalla
- Desplazamiento suave y continuo

#### Teclas de Scroll Manual
- **CANAL ↑ / PAGE UP**: Scroll hacia arriba
- **CANAL ↓ / PAGE DOWN**: Scroll hacia abajo

### 🎯 Modo Navegación Normal (Sin cursor)

Cuando desactivas el modo cursor desde el panel lateral:

- **↑↓ (Arriba/Abajo)**: Scroll de página
- **CANAL ↑↓ / PAGE UP/DOWN**: Scroll de página
- **OK/ENTER**: Navega por elementos enfocables (links, botones)
- Los elementos enfocados se resaltan con un borde verde brillante

### 📋 Panel Lateral (Menú)

Para acceder al panel lateral presiona **MENÚ/OPCIONES** ⚙️:

#### Botones Disponibles:
1. **Cursor ON/OFF**: Activa/desactiva el modo cursor
2. **TV/Móvil/Escritorio**: Cambia el User-Agent
   - TV: Identifica como Smart TV
   - Móvil: Identifica como smartphone Android
   - Escritorio: Identifica como navegador de PC
3. **Zoom 100%**: Resetea el zoom y vuelve al inicio de la página
4. **Recargar**: Recarga la página actual
5. **Agregar URL**: Campo para agregar nuevas URLs

#### Navegación en el Panel:
- **↑↓ (Arriba/Abajo)**: Navegar entre URLs guardadas
- **OK/ENTER**: Cargar la URL seleccionada
- **MENÚ/BACK**: Cerrar el panel y volver a la navegación

### 🎬 Función de Video en Pantalla Completa

La aplicación detecta inteligentemente diferentes tipos de videos:

1. **Videos HTML5** (`<video>` tag)
2. **YouTube** (iframes embebidos)
3. **Reproductores populares**:
   - JW Player
   - Video.js
   - Plyr
   - Flowplayer
   - Y otros reproductores personalizados

**Cómo usar:**
1. Mueve el cursor sobre el video
2. Presiona **OK/ENTER dos veces rápidamente** (doble clic)
3. El video entrará automáticamente en pantalla completa

### ⬅️ Navegación y Salida

- **BACK**: 
  - Si el panel está abierto: Cierra el panel
  - Si hay historial: Retrocede en el navegador
  - Si no hay historial: Muestra diálogo de salida

### 🎨 Indicadores Visuales

- **Cursor verde**: Tu posición actual en modo cursor
- **Borde verde**: Elemento bajo el cursor o con foco
- **Animación del cursor**: 
  - Pequeño bounce: Clic simple
  - Bounce grande: Doble clic (intento de fullscreen)
- **Mensajes Toast**: Confirman acciones realizadas

### URLs Predefinidas

La aplicación viene con estas URLs por defecto:
- Doramasflix
- Gnula HD
- Roja Directa (deportes)
- HackStore
- Pluto TV
- Plex
- Canela TV
- ChatGPT

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
- [x] Modo cursor virtual con D-Pad
- [x] Detección inteligente de videos
- [x] Pantalla completa con doble clic
- [x] Scroll automático en bordes
- [x] User-Agent configurable (TV/Móvil/Escritorio)
- [x] Bloqueador básico de anuncios
- [x] Resaltado de elementos enfocados
- [ ] Gestión de favoritos persistente
- [ ] Historial de navegación
- [ ] Modo incógnito
- [ ] Descarga de archivos

---

**Desarrollado para Smart TVs de bajos recursos** 📺
