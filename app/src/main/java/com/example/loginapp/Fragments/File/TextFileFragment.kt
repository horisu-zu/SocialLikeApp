package com.example.loginapp.Fragments.File

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.loginapp.R

class TextFileFragment : DialogFragment() {

    private var pdfUrl: String? = null
    //private lateinit var pdfView: PDFView

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
        /*pdfView = view.findViewById(R.id.pdfView)

        displayPDF(pdfUrl)*/
    }

    /*private fun displayPDF(url: String?) {
        url?.let {
            pdfView.fromUri(Uri.parse(url))
                .defaultPage(0)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .load()
        }
    }*/
}
