package com.example.loginapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView

class WebViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        val fileUrl = intent.getStringExtra("fileUrl")
        val webView: WebView = findViewById(R.id.webView)
        webView.loadUrl(fileUrl!!)
    }
}