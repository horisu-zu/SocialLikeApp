package com.example.loginapp.Fragments.File

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.VideoView
import androidx.fragment.app.DialogFragment
import com.example.loginapp.R

class VideoFileFragment : DialogFragment() {
    private lateinit var videoView: VideoView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_video_file, container, false)
        videoView = view.findViewById(R.id.videoView)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val videoUriString = arguments?.getString("videoUri")
        videoUriString?.let { uriString ->
            val videoUri = Uri.parse(uriString)
            videoView.setVideoURI(videoUri)
            videoView.start()
        }
    }
}