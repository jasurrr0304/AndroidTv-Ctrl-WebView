package com.smarttv.webview

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var urlEditText: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var urlAdapter: UrlAdapter
    private lateinit var sidePanel: View
    private lateinit var cursor: View
    private val urlList = mutableListOf<String>()
    
    // Handler para auto-ocultar controles
    private val hideControlsHandler = Handler(Looper.getMainLooper())
    private val hideControlsRunnable = Runnable { hideControls() }
    private val AUTO_HIDE_DELAY = 3000L // 3 segundos
    
    // Variables para el cursor virtual
    private var cursorX = 0f
    private var cursorY = 0f
    private val CURSOR_SPEED = 20f // Velocidad de movimiento del cursor
    private var cursorMode = true // Modo cursor activado por defecto
    private var userAgentMode = 0 // 0=TV, 1=Móvil, 2=Escritorio
    private val EDGE_SCROLL_THRESHOLD = 100f // Distancia desde el borde para iniciar scroll automático
    private val EDGE_SCROLL_SPEED = 15 // Velocidad de scroll automático
    
    // Variables para detectar doble clic
    private var lastClickTime = 0L
    private val DOUBLE_CLICK_TIME_DELTA = 500L // 500ms para detectar doble clic

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar vistas
        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)
        urlEditText = findViewById(R.id.urlEditText)
        recyclerView = findViewById(R.id.urlRecyclerView)
        sidePanel = findViewById(R.id.sidePanel)
        cursor = findViewById(R.id.cursor)

        val btnAddUrl: Button = findViewById(R.id.btnAddUrl)
        val btnReload: Button = findViewById(R.id.btnReload)
        val btnToggleCursor: Button = findViewById(R.id.btnToggleCursor)
        val btnToggleUserAgent: Button = findViewById(R.id.btnToggleUserAgent)
        val btnResetZoom: Button = findViewById(R.id.btnResetZoom)
        
        // Inicializar posición del cursor en el centro (cursorX/Y es el punto central)
        webView.post {
            cursorX = webView.width / 2f
            cursorY = webView.height / 2f
            updateCursorPosition()
        }

        // Cargar URLs predefinidas
        loadDefaultUrls()

        // Configurar WebView optimizado para bajos recursos
        setupWebView()

        // Configurar RecyclerView
        setupRecyclerView()

        // Botón para agregar URL
        btnAddUrl.setOnClickListener {
            addUrl()
        }

        // Enter en el campo de texto
        urlEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addUrl()
                true
            } else {
                false
            }
        }

        // Botón para recargar página
        btnReload.setOnClickListener {
            webView.reload()
            Toast.makeText(this, "Recargando página...", Toast.LENGTH_SHORT).show()
        }

        // Botón para activar/desactivar modo cursor
        btnToggleCursor.setOnClickListener {
            cursorMode = !cursorMode
            cursor.isVisible = cursorMode
            
            // Activar/desactivar foco del WebView según el modo
            if (cursorMode) {
                webView.isFocusable = false
                webView.isFocusableInTouchMode = false
            } else {
                webView.isFocusable = true
                webView.isFocusableInTouchMode = true
                webView.requestFocus()
                // Reinyectar el estilo de focus para modo sin cursor
                injectFocusStyle(webView)
            }
            
            val message = if (cursorMode) "Modo Cursor: ACTIVADO" else "Modo Cursor: DESACTIVADO"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            btnToggleCursor.text = if (cursorMode) "Cursor ON" else "Cursor OFF"
        }
        
        // Botón para cambiar user agent (TV -> Móvil -> Escritorio -> TV)
        btnToggleUserAgent.setOnClickListener {
            userAgentMode = (userAgentMode + 1) % 3
            updateUserAgent()
            webView.reload()
            val modeName = when (userAgentMode) {
                0 -> "TV"
                1 -> "Móvil"
                2 -> "Escritorio"
                else -> "TV"
            }
            Toast.makeText(this, "Modo: $modeName", Toast.LENGTH_SHORT).show()
            btnToggleUserAgent.text = modeName
        }
        
        // Botón para resetear zoom al 100%
        btnResetZoom.setOnClickListener {
            // Scroll hacia arriba
            webView.scrollTo(0, 0)
            // Resetear zoom
            webView.setInitialScale(100)
            // Inyectar JavaScript para centrar y resetear viewport
            webView.evaluateJavascript("""
                (function() {
                    window.scrollTo(0, 0);
                    document.body.scrollTop = 0;
                    document.documentElement.scrollTop = 0;
                })();
            """.trimIndent(), null)
            webView.reload()
            Toast.makeText(this, "Zoom: 100% - Top", Toast.LENGTH_SHORT).show()
        }

        // Cargar primera URL si existe
        if (urlList.isNotEmpty()) {
            loadUrl(urlList[0])
        }
        
        // Iniciar el temporizador para ocultar controles
        scheduleHideControls()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            
            // Optimizaciones para bajos recursos
            blockNetworkImage = false
            loadsImagesAutomatically = true
            
            // Cache para mejorar rendimiento
            cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
            
            // Desactivar características pesadas
            javaScriptCanOpenWindowsAutomatically = false
            setGeolocationEnabled(false)
            
            // Bloquear popups y ventanas emergentes
            setSupportMultipleWindows(false)
            
            // Zoom
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            
            // Multimedia optimizado
            mediaPlaybackRequiresUserGesture = false
            
            // Configurar viewport
            useWideViewPort = true
            loadWithOverviewMode = true
        }
        
        // Configurar User-Agent inicial
        updateUserAgent()
        
        // Desactivar foco del WebView para que el cursor funcione
        webView.isFocusable = false
        webView.isFocusableInTouchMode = false

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                progressBar.isVisible = true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                progressBar.isGone = true
                
                // Resetear zoom al 100%
                view?.setInitialScale(100)
                
                // Inyectar CSS para bloquear anuncios comunes y overlays
                val adBlockCss = """
                    javascript:(function() {
                        var css = 'iframe[src*="ads"], iframe[src*="doubleclick"], ' +
                                  'div[id*="ad-"], div[class*="ad-"], div[id*="ads"], ' +
                                  'div[class*="ads"], div[id*="banner"], ' +
                                  'div[class*="banner"], div[id*="popup"], ' +
                                  'div[class*="popup"], ' +
                                  'div[class*="overlay"], div[id*="overlay"], ' +
                                  'div[class*="modal"], div[id*="modal"], ' +
                                  'div[class*="backdrop"], div[id*="backdrop"] { display: none !important; }';
                        var style = document.createElement('style');
                        style.type = 'text/css';
                        style.appendChild(document.createTextNode(css));
                        document.head.appendChild(style);
                    })();
                """.trimIndent()
                view?.loadUrl(adBlockCss)
                
                // Inyectar CSS para resaltar elementos con focus (modo sin cursor)
                injectFocusStyle(view)
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                if (request?.isForMainFrame == true) {
                    Toast.makeText(this@MainActivity, "Error: ${error?.description}", Toast.LENGTH_SHORT).show()
                }
            }
            
            // Bloquear URLs de anuncios conocidas
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString()
                if (url != null) {
                    // Bloquear dominios de anuncios
                    val adDomains = listOf(
                        "doubleclick.net", "googleadservices.com", "googlesyndication.com",
                        "advertising.com", "adbrite.com", "adnxs.com", "adsystem.com",
                        "ads.youtube.com", "ads.facebook.com", "ads.twitter.com",
                        "popads.net", "popcash.net", "popunder", "popup"
                    )
                    
                    if (adDomains.any { url.contains(it, ignoreCase = true) }) {
                        return true // Bloquear la URL
                    }
                    
                    // Manejar URL schemes personalizados (meli://, intent://, market://, etc.)
                    val uri = Uri.parse(url)
                    val scheme = uri.scheme?.lowercase()
                    
                    // Lista de schemes que deben abrirse con apps externas
                    val externalSchemes = listOf(
                        "meli", "intent", "market", "tel", "mailto", "sms", "geo",
                        "whatsapp", "fb", "twitter", "instagram", "youtube", "spotify",
                        "maps", "play", "app", "scheme"
                    )
                    
                    if (scheme != null && scheme !in listOf("http", "https", "file", "about", "javascript")) {
                        // Intentar abrir con una app externa
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            return true
                        } catch (e: ActivityNotFoundException) {
                            // No hay app instalada para este scheme
                            Toast.makeText(
                                this@MainActivity, 
                                "No hay aplicación instalada para abrir este enlace", 
                                Toast.LENGTH_SHORT
                            ).show()
                            return true
                        } catch (e: Exception) {
                            // Error general
                            Toast.makeText(
                                this@MainActivity, 
                                "Error al abrir el enlace", 
                                Toast.LENGTH_SHORT
                            ).show()
                            return true
                        }
                    }
                }
                return false
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressBar.progress = newProgress
            }
        }
    }
    
    private fun updateUserAgent() {
        webView.settings.userAgentString = when (userAgentMode) {
            0 -> "Mozilla/5.0 (Linux; Android 13; Smart TV Build/TP1A.220624.014) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            1 -> "Mozilla/5.0 (Linux; Android 13; SM-S918B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
            2 -> "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            else -> "Mozilla/5.0 (Linux; Android 13; Smart TV) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
        }
    }
    
    private fun injectFocusStyle(view: WebView?) {
        val focusCss = """
            javascript:(function() {
                var focusStyleId = 'tv-focus-style';
                var existingStyle = document.getElementById(focusStyleId);
                if (existingStyle) {
                    existingStyle.remove();
                }
                
                var css = '*:focus { ' +
                    'outline: 5px solid #00ff00 !important; ' +
                    'outline-offset: 3px !important; ' +
                    'box-shadow: 0 0 20px rgba(0, 255, 0, 0.8) !important; ' +
                    'background-color: rgba(0, 255, 0, 0.1) !important; ' +
                    'border-radius: 4px !important; ' +
                    'z-index: 999999 !important; ' +
                '} ' +
                'a:focus, button:focus, input:focus, select:focus, textarea:focus { ' +
                    'outline: 5px solid #00ff00 !important; ' +
                    'outline-offset: 3px !important; ' +
                    'box-shadow: 0 0 25px rgba(0, 255, 0, 1) !important; ' +
                    'background-color: rgba(0, 255, 0, 0.15) !important; ' +
                '}';
                
                var style = document.createElement('style');
                style.id = focusStyleId;
                style.type = 'text/css';
                style.appendChild(document.createTextNode(css));
                document.head.appendChild(style);
            })();
        """.trimIndent()
        view?.loadUrl(focusCss)
    }

    private fun setupRecyclerView() {
        urlAdapter = UrlAdapter(urlList) { url ->
            loadUrl(url)
            toggleSidePanel()
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = urlAdapter
    }

    private fun loadDefaultUrls() {
        urlList.addAll(listOf(
            "https://www.youtube.com/results?search_query=peliculas+gratis",
            "https://www.youtube.com/results?search_query=peliculas+dominio+publico",
            "https://doramasflix.co",
            "https://ww3.gnulahd.nu",
            "https://www.rojadirectaenvivo.pl",
            "https://www.hackstore2.com",
            "https://pluto.tv/latam",
            "https://watch.plex.tv/on-demand/category/en-espanol",
            "https://www.vix.com/es",
            "https://www.nuestra.tv",
            "https://doramasflix.io",
            "https://canela.tv",
            "https://radio.garden",
            "https://artvee.com",
            "https://play.mercadolibre.com.mx/",
            "https://skribbl.io",
            "https://www.chess.com/play/online",
            "https://es.y8.com"
        ))
    }

    private fun addUrl() {
        val url = urlEditText.text.toString().trim()
        if (url.isNotEmpty()) {
            var finalUrl = url
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                finalUrl = "https://$url"
            }
            
            if (!urlList.contains(finalUrl)) {
                urlList.add(finalUrl)
                urlAdapter.notifyItemInserted(urlList.size - 1)
                Toast.makeText(this, "URL agregada", Toast.LENGTH_SHORT).show()
            }
            
            urlEditText.text.clear()
            loadUrl(finalUrl)
        } else {
            Toast.makeText(this, "Ingrese una URL válida", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUrl(url: String) {
        // Resetear zoom antes de cargar
        webView.setInitialScale(100)
        webView.loadUrl(url)
    }

    private fun toggleSidePanel() {
        if (sidePanel.isVisible) {
            sidePanel.isGone = true
            
            // Recalcular posición del cursor al cerrar el sidebar
            if (cursorMode) {
                webView.post {
                    // Re-centrar el cursor en el WebView visible
                    cursorX = webView.width / 2f
                    cursorY = webView.height / 2f
                    updateCursorPosition()
                    cursor.isVisible = true
                }
            }
            
            webView.requestFocus()
            scheduleHideControls()
        } else {
            sidePanel.isVisible = true
            cursor.isGone = true
            showControls()
            // Dar foco al RecyclerView para navegación fácil con control remoto
            recyclerView.postDelayed({
                recyclerView.requestFocus()
                // Scroll al primer elemento si hay URLs
                if (urlList.isNotEmpty()) {
                    recyclerView.scrollToPosition(0)
                }
            }, 100)
        }
    }
    
    private fun showControls() {
        scheduleHideControls()
    }
    
    private fun hideControls() {
        // Ya no hay controles que ocultar
    }
    
    private fun scheduleHideControls() {
        // Cancelar cualquier ocultado pendiente
        hideControlsHandler.removeCallbacks(hideControlsRunnable)
        // Programar nuevo ocultado
        hideControlsHandler.postDelayed(hideControlsRunnable, AUTO_HIDE_DELAY)
    }
    
    private fun updateCursorPosition() {
        // Centrar el cursor en la posición calculada
        cursor.x = cursorX - cursor.width / 2
        cursor.y = cursorY - cursor.height / 2
    }
    
    private fun moveCursor(dx: Float, dy: Float) {
        // cursorX/Y representan el centro del cursor, así que los límites consideran el radio
        val halfWidth = cursor.width / 2f
        val halfHeight = cursor.height / 2f
        cursorX = (cursorX + dx).coerceIn(halfWidth, webView.width.toFloat() - halfWidth)
        cursorY = (cursorY + dy).coerceIn(halfHeight, webView.height.toFloat() - halfHeight)
        updateCursorPosition()
        
        // Resaltar elemento bajo el cursor
        highlightElementUnderCursor()
        
        // Auto-scroll cuando el cursor está cerca de los bordes
        val scrollAmount = EDGE_SCROLL_SPEED
        
        // Scroll hacia arriba si está cerca del borde superior
        if (cursorY - halfHeight < EDGE_SCROLL_THRESHOLD) {
            webView.scrollBy(0, -scrollAmount)
        }
        
        // Scroll hacia abajo si está cerca del borde inferior
        if (cursorY + halfHeight > webView.height - EDGE_SCROLL_THRESHOLD) {
            webView.scrollBy(0, scrollAmount)
        }
        
        // Scroll hacia la izquierda si está cerca del borde izquierdo
        if (cursorX - halfWidth < EDGE_SCROLL_THRESHOLD) {
            webView.scrollBy(-scrollAmount, 0)
        }
        
        // Scroll hacia la derecha si está cerca del borde derecho
        if (cursorX + halfWidth > webView.width - EDGE_SCROLL_THRESHOLD) {
            webView.scrollBy(scrollAmount, 0)
        }
    }
    
    private fun highlightElementUnderCursor() {
        // Obtener la posición del WebView en la pantalla
        val webViewLocation = IntArray(2)
        webView.getLocationOnScreen(webViewLocation)
        
        // Obtener la posición del cursor en la pantalla
        val cursorLocation = IntArray(2)
        cursor.getLocationOnScreen(cursorLocation)
        
        // Calcular coordenadas relativas al WebView
        val webViewX = (cursorLocation[0] - webViewLocation[0] + cursor.width / 2).toFloat()
        val webViewY = (cursorLocation[1] - webViewLocation[1] + cursor.height / 2).toFloat()
        
        // Inyectar JavaScript para resaltar el elemento
        val js = """
            (function() {
                var x = $webViewX;
                var y = $webViewY;
                
                // Remover resaltado anterior
                var previousHighlight = document.querySelector('.cursor-hover-highlight');
                if (previousHighlight) {
                    previousHighlight.classList.remove('cursor-hover-highlight');
                }
                
                // Obtener elemento actual bajo el cursor
                var element = document.elementFromPoint(x, y);
                
                if (element) {
                    // Agregar clase de resaltado
                    element.classList.add('cursor-hover-highlight');
                    
                    // Asegurar que el estilo existe
                    if (!document.getElementById('cursor-hover-style')) {
                        var style = document.createElement('style');
                        style.id = 'cursor-hover-style';
                        style.textContent = '.cursor-hover-highlight { ' +
                            'outline: 3px solid #00ff00 !important; ' +
                            'outline-offset: 2px !important; ' +
                            'box-shadow: 0 0 15px rgba(0, 255, 0, 0.6) !important; ' +
                            'background-color: rgba(0, 255, 0, 0.05) !important; ' +
                            'transition: all 0.1s ease !important; ' +
                        '}';
                        document.head.appendChild(style);
                    }
                }
            })();
        """.trimIndent()
        
        webView.evaluateJavascript(js, null)
    }
    
    private fun tryFullscreenVideo() {
        // Obtener la posición del WebView en la pantalla
        val webViewLocation = IntArray(2)
        webView.getLocationOnScreen(webViewLocation)
        
        // Obtener la posición del cursor en la pantalla
        val cursorLocation = IntArray(2)
        cursor.getLocationOnScreen(cursorLocation)
        
        // Calcular coordenadas relativas al WebView
        val webViewX = (cursorLocation[0] - webViewLocation[0] + cursor.width / 2).toFloat()
        val webViewY = (cursorLocation[1] - webViewLocation[1] + cursor.height / 2).toFloat()
        
        // JavaScript avanzado para detectar y poner en fullscreen diferentes tipos de videos
        val js = """
            (function() {
                var x = $webViewX;
                var y = $webViewY;
                
                console.log('Buscando video en posición:', x, y);
                
                // Obtener elemento en la posición del cursor
                var element = document.elementFromPoint(x, y);
                if (!element) {
                    return 'No se encontró elemento en la posición';
                }
                
                console.log('Elemento encontrado:', element.tagName, element.className);
                
                // Función para intentar fullscreen con múltiples APIs
                function requestFullscreen(elem) {
                    if (!elem) return false;
                    
                    try {
                        if (elem.requestFullscreen) {
                            elem.requestFullscreen();
                            return true;
                        } else if (elem.mozRequestFullScreen) {
                            elem.mozRequestFullScreen();
                            return true;
                        } else if (elem.webkitRequestFullscreen) {
                            elem.webkitRequestFullscreen();
                            return true;
                        } else if (elem.msRequestFullscreen) {
                            elem.msRequestFullscreen();
                            return true;
                        } else if (elem.webkitEnterFullscreen) {
                            // Para iOS
                            elem.webkitEnterFullscreen();
                            return true;
                        }
                    } catch(e) {
                        console.log('Error al intentar fullscreen:', e);
                    }
                    return false;
                }
                
                // 1. Buscar elemento VIDEO más cercano (HTML5 video)
                var videoElement = element;
                var depth = 0;
                while (videoElement && depth < 10) {
                    if (videoElement.tagName === 'VIDEO') {
                        console.log('Video HTML5 encontrado');
                        
                        // Intentar reproducir si está pausado
                        if (videoElement.paused) {
                            videoElement.play();
                        }
                        
                        // Intentar fullscreen en el video
                        if (requestFullscreen(videoElement)) {
                            return 'Fullscreen activado en VIDEO HTML5';
                        }
                        
                        // Si no funciona, intentar en el contenedor padre
                        if (videoElement.parentElement && requestFullscreen(videoElement.parentElement)) {
                            return 'Fullscreen activado en contenedor de VIDEO';
                        }
                    }
                    videoElement = videoElement.parentElement;
                    depth++;
                }
                
                // 2. Buscar IFRAME de YouTube o reproductores embebidos
                var iframeElement = element;
                depth = 0;
                while (iframeElement && depth < 10) {
                    if (iframeElement.tagName === 'IFRAME') {
                        console.log('IFRAME encontrado:', iframeElement.src);
                        
                        // Detectar YouTube
                        if (iframeElement.src && (iframeElement.src.includes('youtube.com') || iframeElement.src.includes('youtu.be'))) {
                            console.log('YouTube detectado');
                            
                            // Intentar fullscreen en el iframe
                            if (requestFullscreen(iframeElement)) {
                                return 'Fullscreen activado en YouTube IFRAME';
                            }
                            
                            // Intentar buscar botón de fullscreen dentro del iframe (limitado por CORS)
                            try {
                                var iframeDoc = iframeElement.contentDocument || iframeElement.contentWindow.document;
                                var fullscreenBtn = iframeDoc.querySelector('.ytp-fullscreen-button');
                                if (fullscreenBtn) {
                                    fullscreenBtn.click();
                                    return 'Click en botón fullscreen de YouTube';
                                }
                            } catch(e) {
                                console.log('No se puede acceder al contenido del iframe (CORS)');
                            }
                        }
                        
                        // Otros reproductores embebidos (Vimeo, Dailymotion, etc.)
                        if (iframeElement.src && 
                            (iframeElement.src.includes('vimeo.com') || 
                             iframeElement.src.includes('dailymotion.com') ||
                             iframeElement.src.includes('player') ||
                             iframeElement.src.includes('embed'))) {
                            console.log('Reproductor embebido detectado');
                            if (requestFullscreen(iframeElement)) {
                                return 'Fullscreen activado en reproductor embebido';
                            }
                        }
                    }
                    iframeElement = iframeElement.parentElement;
                    depth++;
                }
                
                // 3. Buscar reproductores HTML personalizados (buscar contenedores comunes)
                var playerElement = element;
                depth = 0;
                var playerClasses = ['player', 'video-player', 'movie-player', 'jwplayer', 'vjs-', 'plyr', 'flowplayer', 'video-js'];
                
                while (playerElement && depth < 10) {
                    var className = (playerElement.className || '').toLowerCase();
                    var id = (playerElement.id || '').toLowerCase();
                    
                    // Verificar si es un contenedor de reproductor
                    var isPlayer = playerClasses.some(function(cls) {
                        return className.includes(cls) || id.includes(cls);
                    });
                    
                    if (isPlayer) {
                        console.log('Reproductor personalizado encontrado:', playerElement.className || playerElement.id);
                        
                        // Buscar video dentro del reproductor
                        var video = playerElement.querySelector('video');
                        if (video) {
                            if (video.paused) {
                                video.play();
                            }
                            if (requestFullscreen(video)) {
                                return 'Fullscreen activado en video de reproductor personalizado';
                            }
                        }
                        
                        // Intentar fullscreen en el contenedor del reproductor
                        if (requestFullscreen(playerElement)) {
                            return 'Fullscreen activado en reproductor personalizado';
                        }
                        
                        // Buscar botón de fullscreen dentro del reproductor
                        var fullscreenSelectors = [
                            '.vjs-fullscreen-control',
                            '.plyr__control--fullscreen',
                            '[data-plyr="fullscreen"]',
                            '.jw-icon-fullscreen',
                            'button[title*="fullscreen" i]',
                            'button[aria-label*="fullscreen" i]',
                            '.fullscreen-button',
                            '[class*="fullscreen"]'
                        ];
                        
                        for (var i = 0; i < fullscreenSelectors.length; i++) {
                            var btn = playerElement.querySelector(fullscreenSelectors[i]);
                            if (btn) {
                                console.log('Botón fullscreen encontrado:', fullscreenSelectors[i]);
                                btn.click();
                                return 'Click en botón fullscreen de reproductor';
                            }
                        }
                    }
                    
                    playerElement = playerElement.parentElement;
                    depth++;
                }
                
                // 4. Buscar TODOS los videos en la página como último recurso
                var allVideos = document.querySelectorAll('video');
                if (allVideos.length > 0) {
                    console.log('Videos encontrados en la página:', allVideos.length);
                    
                    // Intentar con el primer video visible
                    for (var i = 0; i < allVideos.length; i++) {
                        var vid = allVideos[i];
                        var rect = vid.getBoundingClientRect();
                        
                        // Verificar si el video está visible
                        if (rect.width > 0 && rect.height > 0 && 
                            rect.top < window.innerHeight && rect.bottom > 0) {
                            console.log('Video visible encontrado');
                            
                            if (vid.paused) {
                                vid.play();
                            }
                            
                            if (requestFullscreen(vid)) {
                                return 'Fullscreen activado en primer video visible';
                            }
                        }
                    }
                }
                
                // 5. Buscar TODOS los iframes como último recurso
                var allIframes = document.querySelectorAll('iframe');
                if (allIframes.length > 0) {
                    console.log('Iframes encontrados en la página:', allIframes.length);
                    
                    for (var i = 0; i < allIframes.length; i++) {
                        var iframe = allIframes[i];
                        var rect = iframe.getBoundingClientRect();
                        
                        // Verificar si el iframe está visible y es de tamaño razonable
                        if (rect.width > 200 && rect.height > 200 && 
                            rect.top < window.innerHeight && rect.bottom > 0) {
                            console.log('Iframe visible encontrado');
                            
                            if (requestFullscreen(iframe)) {
                                return 'Fullscreen activado en iframe visible';
                            }
                        }
                    }
                }
                
                return 'No se encontró ningún video o reproductor';
            })();
        """.trimIndent()
        
        // Feedback visual de doble clic
        cursor.animate().scaleX(1.5f).scaleY(1.5f).setDuration(150).withEndAction {
            cursor.animate().scaleX(1f).scaleY(1f).setDuration(150).start()
        }
        
        webView.evaluateJavascript(js) { result ->
            // Mostrar mensaje del resultado
            val message = result?.replace("\"", "") ?: "Sin respuesta"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
    
    @SuppressLint("SetJavaScriptEnabled")
    private fun simulateClick() {
        // Detectar doble clic
        val currentTime = System.currentTimeMillis()
        val isDoubleClick = (currentTime - lastClickTime) < DOUBLE_CLICK_TIME_DELTA
        lastClickTime = currentTime
        
        // Si es doble clic, intentar poner video en fullscreen
        if (isDoubleClick) {
            tryFullscreenVideo()
            return
        }
        
        // Si es clic simple, proceder con la lógica normal
        // Obtener la posición del WebView en la pantalla
        val webViewLocation = IntArray(2)
        webView.getLocationOnScreen(webViewLocation)
        
        // Obtener la posición del cursor en la pantalla
        val cursorLocation = IntArray(2)
        cursor.getLocationOnScreen(cursorLocation)
        
        // Calcular coordenadas relativas al WebView
        val webViewX = (cursorLocation[0] - webViewLocation[0] + cursor.width / 2).toFloat()
        val webViewY = (cursorLocation[1] - webViewLocation[1] + cursor.height / 2).toFloat()
        
        // Inyectar JavaScript mejorado para simular click
        val js = """
            (function() {
                var x = $webViewX;
                var y = $webViewY;
                
                // Obtener elemento en la posición
                var element = document.elementFromPoint(x, y);
                
                if (element) {
                    // Hacer scroll al elemento
                    element.scrollIntoView({behavior: 'smooth', block: 'center'});
                    
                    // Disparar múltiples eventos para máxima compatibilidad
                    ['mousedown', 'mouseup', 'click'].forEach(function(eventType) {
                        var evt = new MouseEvent(eventType, {
                            view: window,
                            bubbles: true,
                            cancelable: true,
                            clientX: x,
                            clientY: y,
                            button: 0
                        });
                        element.dispatchEvent(evt);
                    });
                    
                    // Disparar evento touch para sitios móviles
                    if (typeof TouchEvent !== 'undefined') {
                        var touch = new Touch({
                            identifier: Date.now(),
                            target: element,
                            clientX: x,
                            clientY: y,
                            radiusX: 2.5,
                            radiusY: 2.5,
                            rotationAngle: 0,
                            force: 0.5
                        });
                        
                        ['touchstart', 'touchend'].forEach(function(eventType) {
                            var touchEvent = new TouchEvent(eventType, {
                                bubbles: true,
                                cancelable: true,
                                touches: [touch],
                                targetTouches: [touch],
                                changedTouches: [touch]
                            });
                            element.dispatchEvent(touchEvent);
                        });
                    }
                    
                    // Si es un link, navegarlo directamente
                    if (element.tagName === 'A' && element.href) {
                        window.location.href = element.href;
                        return 'Navigating to: ' + element.href;
                    }
                    
                    // Si es un botón o input, hacer click nativo
                    if (element.tagName === 'BUTTON' || element.tagName === 'INPUT' || element.tagName === 'SELECT') {
                        element.click();
                        element.focus();
                    }
                    
                    return 'Clicked: ' + element.tagName + ' - ' + (element.className || element.id || 'no-class');
                }
                return 'No element found at position';
            })();
        """.trimIndent()
        
        // Feedback visual inmediato
        cursor.animate().scaleX(1.3f).scaleY(1.3f).setDuration(100).withEndAction {
            cursor.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
        }
        
        webView.evaluateJavascript(js) { result ->
            // Log del resultado para debugging
            if (result != null && !result.contains("No element")) {
                // Click exitoso
            }
        }
    }

    override fun onBackPressed() {
        when {
            sidePanel.isVisible -> toggleSidePanel()
            webView.canGoBack() -> webView.goBack()
            else -> {
                AlertDialog.Builder(this)
                    .setTitle("Salir")
                    .setMessage("¿Desea salir de la aplicación?")
                    .setPositiveButton("Sí") { _, _ -> finish() }
                    .setNegativeButton("No", null)
                    .show()
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Mostrar controles cuando hay interacción
        showControls()
        
        // Si el sidebar está abierto, manejar navegación del menú
        if (sidePanel.isVisible) {
            when (keyCode) {
                KeyEvent.KEYCODE_MENU -> {
                    toggleSidePanel()
                    return true
                }
                KeyEvent.KEYCODE_BACK -> {
                    toggleSidePanel()
                    return true
                }
                else -> return super.onKeyDown(keyCode, event)
            }
        }
        
        // Si el sidebar está cerrado y el modo cursor está activado
        if (sidePanel.isGone && cursorMode) {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> {
                    moveCursor(0f, -CURSOR_SPEED)
                    return true
                }
                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    moveCursor(0f, CURSOR_SPEED)
                    return true
                }
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    moveCursor(-CURSOR_SPEED, 0f)
                    return true
                }
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    moveCursor(CURSOR_SPEED, 0f)
                    return true
                }
                KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                    simulateClick()
                    return true
                }
                KeyEvent.KEYCODE_MENU -> {
                    toggleSidePanel()
                    return true
                }
                // Scroll vertical con botones de canal
                KeyEvent.KEYCODE_CHANNEL_UP, KeyEvent.KEYCODE_PAGE_UP -> {
                    webView.scrollBy(0, -100)
                    return true
                }
                KeyEvent.KEYCODE_CHANNEL_DOWN, KeyEvent.KEYCODE_PAGE_DOWN -> {
                    webView.scrollBy(0, 100)
                    return true
                }
            }
        } else if (sidePanel.isGone && !cursorMode) {
            // Modo navegación normal del WebView
            when (keyCode) {
                KeyEvent.KEYCODE_MENU -> {
                    toggleSidePanel()
                    return true
                }
                // Scroll de página con botones de canal o D-Pad
                KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_CHANNEL_UP, KeyEvent.KEYCODE_PAGE_UP -> {
                    webView.scrollBy(0, -100)
                    return true
                }
                KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_CHANNEL_DOWN, KeyEvent.KEYCODE_PAGE_DOWN -> {
                    webView.scrollBy(0, 100)
                    return true
                }
            }
        }
        
        return super.onKeyDown(keyCode, event)
    }
    
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        // Mostrar controles al tocar la pantalla
        showControls()
        return super.onTouchEvent(event)
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    override fun onDestroy() {
        hideControlsHandler.removeCallbacks(hideControlsRunnable)
        webView.destroy()
        super.onDestroy()
    }
}
