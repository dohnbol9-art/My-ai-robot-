package com.example.automation

import android.content.Context
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.security.SecurityManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

data class DomScrapeResult(
    val title: String,
    val priceText: String,
    val imageUrl: String,
    val description: String,
    val rawHtmlSnippet: String
)

class BrowserAutomationEngine(
    private val context: Context,
    private val securityManager: SecurityManager
) {
    private val _currentUrl = MutableStateFlow("https://www.google.com")
    val currentUrl: StateFlow<String> = _currentUrl.asStateFlow()

    private val _pageTitle = MutableStateFlow("Google")
    val pageTitle: StateFlow<String> = _pageTitle.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _lastScrapedData = MutableStateFlow<DomScrapeResult?>(null)
    val lastScrapedData: StateFlow<DomScrapeResult?> = _lastScrapedData.asStateFlow()

    private val _automationLog = MutableStateFlow<List<String>>(emptyList())
    val automationLog: StateFlow<List<String>> = _automationLog.asStateFlow()

    var webViewInstance: WebView? = null
        private set

    fun attachWebView(webView: WebView) {
        this.webViewInstance = webView
        setupWebView(webView)
    }

    private fun setupWebView(webView: WebView) {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadsImagesAutomatically = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            userAgentString = "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36 MyAIRobot/1.0"
        }

        webView.addJavascriptInterface(object {
            @JavascriptInterface
            fun onDomExtracted(jsonString: String) {
                try {
                    val obj = JSONObject(jsonString)
                    val result = DomScrapeResult(
                        title = obj.optString("title", ""),
                        priceText = obj.optString("price", ""),
                        imageUrl = obj.optString("image", ""),
                        description = obj.optString("desc", ""),
                        rawHtmlSnippet = obj.optString("snippet", "")
                    )
                    _lastScrapedData.value = result
                    logEvent("DOM Extraction: Found title: ${result.title.take(30)}...")
                } catch (e: Exception) {
                    logEvent("DOM Parse error: ${e.message}")
                }
            }

            @JavascriptInterface
            fun onSecurityChallengeDetected(challengeType: String) {
                logEvent("⚠️ SECURITY GATE DETECTED: $challengeType. Pausing automation.")
            }
        }, "RobotBridge")

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                _isLoading.value = true
                url?.let {
                    _currentUrl.value = it
                    logEvent("Navigating to: $it")
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                _isLoading.value = false
                _pageTitle.value = view?.title ?: ""
                url?.let {
                    _currentUrl.value = it
                    logEvent("Page loaded: ${_pageTitle.value}")
                }
            }
        }
    }

    fun navigateTo(url: String): Boolean {
        if (!securityManager.checkUrlAuthorized(url)) {
            logEvent("❌ BLOCKED: URL $url is not authorized in Security Settings.")
            return false
        }
        val target = if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "https://$url"
        } else url

        _currentUrl.value = target
        webViewInstance?.loadUrl(target)
        return true
    }

    fun executeSearch(query: String) {
        val searchUrl = "https://www.google.com/search?q=" + java.net.URLEncoder.encode(query, "UTF-8")
        navigateTo(searchUrl)
    }

    fun extractPageProductDom() {
        val jsScript = """
            (function() {
                var title = document.querySelector('h1, .product-title, .goods-name, [data-spm="title"]')?.innerText || document.title;
                var price = document.querySelector('.price, [data-testid="price"], .product-price, .goods-price')?.innerText || "$0.00";
                var img = document.querySelector('meta[property="og:image"]')?.content || document.querySelector('.main-image, img')?.src || "";
                var desc = document.querySelector('meta[name="description"]')?.content || "";
                var captcha = document.body.innerText.toLowerCase().includes("verify you are human") || document.body.innerText.toLowerCase().includes("security challenge");
                if (captcha) {
                    window.RobotBridge.onSecurityChallengeDetected("Captcha/Human verification");
                }
                var data = {
                    title: title.trim(),
                    price: price.trim(),
                    image: img,
                    desc: desc.trim(),
                    snippet: document.title
                };
                window.RobotBridge.onDomExtracted(JSON.stringify(data));
            })();
        """.trimIndent()
        webViewInstance?.evaluateJavascript(jsScript, null)
    }

    fun fillInputAndSubmit(selector: String, value: String) {
        val escapedValue = value.replace("'", "\\'")
        val js = """
            (function() {
                var el = document.querySelector('$selector');
                if (el) {
                    el.value = '$escapedValue';
                    el.dispatchEvent(new Event('input', { bubbles: true }));
                    el.dispatchEvent(new Event('change', { bubbles: true }));
                    var form = el.closest('form');
                    if (form) form.submit();
                }
            })();
        """.trimIndent()
        webViewInstance?.evaluateJavascript(js, null)
    }

    fun getSessionCookiesForDomain(domain: String): String {
        return CookieManager.getInstance().getCookie(domain) ?: ""
    }

    fun logEvent(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        val item = "[$timestamp] $message"
        _automationLog.value = (_automationLog.value + item).takeLast(100)
    }
}
