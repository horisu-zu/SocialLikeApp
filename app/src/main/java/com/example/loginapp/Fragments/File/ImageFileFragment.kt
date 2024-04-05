package com.example.loginapp.Fragments.File

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.example.loginapp.R
import com.squareup.picasso.Picasso

class ImageFileFragment : DialogFragment() {

    private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_image_file, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getString("imageUri")?.let { uriString ->
            imageUri = Uri.parse(uriString)
            Log.e("URI", imageUri.toString())

            val imageView = view.findViewById<ImageView>(R.id.imageView)

            Picasso.get()
                .load(imageUri)
                .into(imageView)
        }

        view.setOnClickListener {
            dismiss()
        }
    }
}
