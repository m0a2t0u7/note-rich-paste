package com.latte.nrp

import android.app.Activity
import android.app.AlertDialog
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
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import org.json.JSONArray

class MainActivity : Activity() {
    private lateinit var web: WebView
    private lateinit var countView: TextView
    private var fileCb: ValueCallback<Array<Uri>>? = null
    private val script by lazy { assets.open("nrp.js").bufferedReader().readText() }
    private val stock = mutableListOf<String>()
    private val maxItems = 30

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadStock()
        val d = resources.displayMetrics.density
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        // 上部バー：noteの下部ツールバーや本文と重ならない位置に置く
        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(Color.parseColor("#111111"))
            setPadding((12 * d).toInt(), 0, (8 * d).toInt(), 0)
        }
        countView = TextView(this).apply { setTextColor(Color.parseColor("#DDDDDD")); textSize = 13f }
        bar.addView(countView, LinearLayout.LayoutParams(0, -2, 1f))
        val btn = Button(this).apply {
            text = "流し込む"
            setTextColor(Color.parseColor("#111111"))
            textSize = 14f
            isAllCaps = false
            isFocusable = false
            isFocusableInTouchMode = false
            background = GradientDrawable().apply { setColor(Color.WHITE); cornerRadius = 18 * d }
            setPadding((16 * d).toInt(), 0, (16 * d).toInt(), 0)
            setOnClickListener { onPourTap() }
        }
        bar.addView(btn, LinearLayout.LayoutParams(-2, (36 * d).toInt()))
        root.addView(bar, LinearLayout.LayoutParams(-1, (48 * d).toInt()))

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
        root.addView(web, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
        updateCount()

        if (savedInstanceState != null) web.restoreState(savedInstanceState)
        else web.loadUrl("https://note.com/notes/new")
    }

    // アプリが前面に来たときだけクリップボードを読める（Androidの制限）
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) captureClipboard()
    }

    private fun readClipboard(): String {
        val cm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        return cm.primaryClip?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.coerceToText(this)?.toString() ?: ""
    }

    private fun captureClipboard() {
        val t = readClipboard()
        if (t.isBlank() || stock.lastOrNull() == t) return
        stock.remove(t)
        stock.add(t)
        while (stock.size > maxItems) stock.removeAt(0)
        saveStock(); updateCount()
    }

    private fun onPourTap() {
        captureClipboard()
        if (stock.isEmpty()) { Toast.makeText(this, "箱が空です。Markdownをコピーしてからアプリに戻ってください。", Toast.LENGTH_LONG).show(); return }
        if (stock.size == 1) { pour(listOf(0)); return }
        val labels = stock.map { preview(it) }.toTypedArray()
        val checked = BooleanArray(stock.size) { true }
        AlertDialog.Builder(this)
            .setTitle("流し込む項目（上から順に入ります）")
            .setMultiChoiceItems(labels, checked) { _, i, v -> checked[i] = v }
            .setPositiveButton("選んだ順に流し込む") { _, _ -> pour(checked.indices.filter { checked[it] }) }
            .setNeutralButton("箱を空にする") { _, _ -> stock.clear(); saveStock(); updateCount() }
            .setNegativeButton("閉じる", null)
            .show()
    }

    private fun pour(idx: List<Int>) {
        if (idx.isEmpty()) return
        val items = idx.map { stock[it] }
        val text = items.joinToString("\n\n")
        val arg = JSONArray().put(text).toString()
        web.evaluateJavascript(script + ";window.nrpInsert($arg[0])") { r ->
            val res = r?.trim('"') ?: ""
            if (res.startsWith("OK:")) {
                stock.removeAll(items.toSet()); saveStock(); updateCount()
                Toast.makeText(this, "${items.size}件を" + res.removePrefix("OK:"), Toast.LENGTH_LONG).show()
            } else Toast.makeText(this, res.ifEmpty { "結果を取得できませんでした。" }, Toast.LENGTH_LONG).show()
        }
    }

    private fun preview(t: String): String {
        val lines = t.lines().map { it.trim() }.filter { it.isNotEmpty() }.take(2).joinToString(" / ")
        return if (lines.length > 60) lines.take(60) + "…" else lines
    }

    private fun updateCount() { countView.text = "箱：${stock.size}件" }

    private fun loadStock() {
        val raw = getSharedPreferences("nrp", MODE_PRIVATE).getString("stock", "[]") ?: "[]"
        val a = try { JSONArray(raw) } catch (e: Exception) { JSONArray() }
        for (i in 0 until a.length()) stock.add(a.getString(i))
    }

    private fun saveStock() {
        val a = JSONArray(); stock.forEach { a.put(it) }
        getSharedPreferences("nrp", MODE_PRIVATE).edit().putString("stock", a.toString()).apply()
    }

    override fun onActivityResult(req: Int, res: Int, data: Intent?) {
        if (req == 1) { fileCb?.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(res, data)); fileCb = null }
        else super.onActivityResult(req, res, data)
    }

    override fun onSaveInstanceState(out: Bundle) { super.onSaveInstanceState(out); web.saveState(out) }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() { if (web.canGoBack()) web.goBack() else super.onBackPressed() }
}
