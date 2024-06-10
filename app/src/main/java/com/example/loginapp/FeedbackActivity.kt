package com.example.loginapp

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.messaging.MessageStatus
import com.example.loginapp.Models.Defaults

class FeedbackActivity : AppCompatActivity() {

    private lateinit var feedbackEditText: EditText
    private lateinit var errorRadioButton: RadioButton
    private lateinit var suggestionRadioButton: RadioButton
    private lateinit var sendFeedbackButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        feedbackEditText = findViewById(R.id.feedbackEditText)
        errorRadioButton = findViewById(R.id.errorRadioButton)
        suggestionRadioButton = findViewById(R.id.suggestionRadioButton)
        sendFeedbackButton = findViewById(R.id.sendFeedbackButton)

        sendFeedbackButton.setOnClickListener {
            sendFeedback()
        }
    }

    private fun sendFeedback() {
        val feedbackText = feedbackEditText.text.toString()
        val feedbackType = if (errorRadioButton.isChecked) {
            "Помилка"
        } else {
            "Порада"
        }

        Backendless.Messaging.sendTextEmail(feedbackType, feedbackText, Defaults.devEmail,
            object : AsyncCallback<MessageStatus> {
            override fun handleResponse(response: MessageStatus?) {
                Toast.makeText(this@FeedbackActivity, "Ваш фідбек успішно надіслано",
                    Toast.LENGTH_LONG).show()
            }

            override fun handleFault(fault: BackendlessFault) {
                Log.e("SENDING ERROR", fault.toString())
            }
        })
    }

    /*private fun sendFeedback() {
        val feedbackText = feedbackEditText.text.toString()
        val feedbackType = if (errorRadioButton.isChecked) {
            "Помилка"
        } else {
            "Порада"
        }

        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.data = Uri.parse("mailto:")
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(Defaults.devEmail))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, feedbackType)
        emailIntent.putExtra(Intent.EXTRA_TEXT, feedbackText)

        try {
            startActivity(Intent.createChooser(emailIntent, "Виберіть email-клієнт"))
        } catch (e: Exception) {
            Log.e("Email Error", "Помилка при відправленні листа: ${e.message}")
        }
    }*/
}
