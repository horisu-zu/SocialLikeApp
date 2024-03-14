package com.example.loginapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault

class SettingsActivity : AppCompatActivity() {

    private lateinit var logoutCard: LinearLayout
    private lateinit var logoutImageView: ImageView
    private lateinit var logoutTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        logoutCard = findViewById(R.id.logoutCard)
        logoutImageView = findViewById(R.id.logoutImageView)
        logoutTextView = findViewById(R.id.logoutTextView)

        logoutCard.setOnClickListener {
            Backendless.UserService.logout(object : AsyncCallback<Void?> {
                override fun handleResponse(response: Void?) {
                    val intent = Intent(this@SettingsActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                override fun handleFault(fault: BackendlessFault?) {
                }
            })
        }
    }
}
