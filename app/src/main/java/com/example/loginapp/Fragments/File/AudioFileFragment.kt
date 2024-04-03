package com.example.loginapp.Fragments.File

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.loginapp.R
import java.util.concurrent.TimeUnit

class AudioFileFragment : Fragment() {

    private var mediaPlayer: MediaPlayer? = null
    private var audioFilePath: String? = null
    private var totalTime: Long = 0

    private lateinit var imageView: ImageView
    private lateinit var titleView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var buttonRewind: CardView
    private lateinit var buttonPlayPause: Button
    private lateinit var buttonForward: CardView
    private lateinit var currentTimeView: TextView
    private lateinit var timeView: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_audio_file, container, false)
        imageView = view.findViewById(R.id.imageView)
        titleView = view.findViewById(R.id.titleView)
        progressBar = view.findViewById(R.id.progressBar)
        buttonRewind = view.findViewById(R.id.rewindCard)
        buttonPlayPause = view.findViewById(R.id.buttonPlayPause)
        buttonForward = view.findViewById(R.id.forwardCard)
        currentTimeView = view.findViewById(R.id.currentTimeView)
        timeView = view.findViewById(R.id.timeView)

        return view
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        audioFilePath = arguments?.getString("audioUri", "audioUri")
        titleView.text = arguments?.getString("audioTitle", "someTitle")

        mediaPlayer = MediaPlayer().apply {
            setDataSource(audioFilePath)
            prepare()
            start()
            totalTime = duration.toLong()
        }

        view.setOnTouchListener { _, _ ->
            return@setOnTouchListener true
        }

        buttonPlayPause.setOnClickListener {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                buttonPlayPause.text = "Play"
            } else {
                mediaPlayer?.start()
                buttonPlayPause.text = "Pause"
            }
        }

        progressBar.max = totalTime.toInt()

        progressBar.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val newPosition = (event.x / progressBar.width * progressBar.max).toInt()
                mediaPlayer?.seekTo(newPosition)
            }
            true
        }

        Thread {
            while (mediaPlayer != null && mediaPlayer!!.isPlaying) {
                val currentPosition = mediaPlayer!!.currentPosition
                activity?.runOnUiThread {
                    progressBar.progress = currentPosition
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(currentPosition.toLong())
                    val seconds = TimeUnit.MILLISECONDS.toSeconds(currentPosition.toLong()) -
                            TimeUnit.MINUTES.toSeconds(minutes)
                    currentTimeView.text = String.format("%02d:%02d", minutes, seconds)
                }
                Thread.sleep(500)
            }
        }.start()

        val minutesTotal = TimeUnit.MILLISECONDS.toMinutes(totalTime)
        val secondsTotal = TimeUnit.MILLISECONDS.toSeconds(totalTime) -
                TimeUnit.MINUTES.toSeconds(minutesTotal)
        timeView.text = String.format("%02d:%02d", minutesTotal, secondsTotal)

        buttonRewind.setOnClickListener {
            mediaPlayer?.seekTo(mediaPlayer?.currentPosition?.minus(TIME) ?: 0)
        }

        buttonForward.setOnClickListener {
            mediaPlayer?.seekTo(mediaPlayer?.currentPosition?.plus(TIME) ?: 0)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    companion object {
        private const val TIME = 10000 // 10 sec
    }
}