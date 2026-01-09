# Changelog

## [1.1.0] - 2026-01-08

### Agregado
- Auto-ocultado del botón de menú y barra superior después de 3 segundos de inactividad
- Los controles se muestran automáticamente cuando el usuario interactúa con el control remoto
- Los controles también se muestran al tocar la pantalla (para dispositivos táctiles)

### Mejorado
- La barra superior ahora se agrupa en un contenedor `topBar` para facilitar su ocultado
- El panel lateral mantiene visible la barra superior mientras está abierto
- Navegación más inmersiva con ocultado automático de controles

### Técnico
- Agregado `Handler` con `Looper.getMainLooper()` para manejar el temporizador
- Implementado `showControls()` para mostrar botón y barra
- Implementado `hideControls()` para ocultar botón y barra
- Implementado `scheduleHideControls()` para programar el ocultado con delay
- Override de `onTouchEvent()` para detectar interacción táctil
- Limpieza del handler en `onDestroy()` para evitar memory leaks
