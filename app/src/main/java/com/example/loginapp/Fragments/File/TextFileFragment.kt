package com.example.loginapp.Fragments.File

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.DialogFragment
import com.example.loginapp.R

class TextFileFragment : DialogFragment() {

    private var pdfUrl: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_text_file, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pdfUrl = arguments?.getString("pdfUrl")

        displayPDF(pdfUrl)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun displayPDF(url: String?) {
        url?.let {
            val webView = view?.findViewById<WebView>(R.id.webView)
            webView?.settings?.javaScriptEnabled = true
            webView?.loadUrl("https://docs.google.com/gview?embedded=true&url=$url")
        }
    }
}
