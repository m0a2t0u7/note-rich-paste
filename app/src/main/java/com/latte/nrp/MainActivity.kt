package com.latte.nrp

import android.app.Activity
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import org.json.JSONArray

class MainActivity : Activity() {
    private lateinit var web: WebView
    private var fileCb: ValueCallback<Array<Uri>>? = null
    private val script by lazy { assets.open("nrp.js").bufferedReader().readText() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = FrameLayout(this)
        web = WebView(this)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(web, true)
        web.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) { view.evaluateJavascript(script, null) }
        }
        web.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(v: WebView, cb: ValueCallback<Array<Uri>>, p: FileChooserParams): Boolean {
                fileCb?.onReceiveValue(null); fileCb = cb
                return try { startActivityForResult(p.createIntent(), 1); true } catch (e: Exception) { fileCb = null; false }
            }
        }
        root.addView(web, FrameLayout.LayoutParams(-1, -1))

        val d = resources.displayMetrics.density
        val btn = Button(this).apply {
            text = "流し込む"
            setTextColor(Color.WHITE)
            textSize = 15f
            isFocusable = false
            isFocusableInTouchMode = false
            background = GradientDrawable().apply { setColor(Color.parseColor("#1F8A62")); cornerRadius = 28 * d }
            elevation = 8 * d
            setPadding((20 * d).toInt(), 0, (20 * d).toInt(), 0)
            setOnClickListener { pour() }
        }
        root.addView(btn, FrameLayout.LayoutParams(-2, (56 * d).toInt(), Gravity.BOTTOM or Gravity.END).apply {
            setMargins(0, 0, (16 * d).toInt(), (24 * d).toInt())
        })
        setContentView(root)

        if (savedInstanceState != null) web.restoreState(savedInstanceState)
        else web.loadUrl("https://note.com/notes/new")
    }

    private fun pour() {
        val cm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val text = cm.primaryClip?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.coerceToText(this)?.toString() ?: ""
        val arg = JSONArray().put(text).toString()
        web.evaluateJavascript(script + ";window.nrpInsert($arg[0])") { r ->
            val msg = r?.trim('"')?.removePrefix("OK:") ?: "結果を取得できませんでした。"
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(req: Int, res: Int, data: Intent?) {
        if (req == 1) { fileCb?.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(res, data)); fileCb = null }
        else super.onActivityResult(req, res, data)
    }

    override fun onSaveInstanceState(out: Bundle) { super.onSaveInstanceState(out); web.saveState(out) }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() { if (web.canGoBack()) web.goBack() else super.onBackPressed() }
}
