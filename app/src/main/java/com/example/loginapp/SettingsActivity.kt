package com.example.loginapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.backendless.Backendless
import com.backendless.BackendlessUser
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault

class SettingsActivity : AppCompatActivity() {

    private lateinit var logoutCard: LinearLayout
    private lateinit var deleteCard: LinearLayout
    private lateinit var configurationCard: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        logoutCard = findViewById(R.id.logoutCard)
        deleteCard = findViewById(R.id.deleteCard)
        configurationCard = findViewById(R.id.configurationProfileCard)

        logoutCard.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Підтвердження логауту")
                .setMessage("Ви впевнені, що хочете вийти з аккаунту?")
                .setPositiveButton("Так") { dialog, which ->
                    Backendless.UserService.logout(object : AsyncCallback<Void?> {
                        override fun handleResponse(response: Void?) {
                            val intent = Intent(this@SettingsActivity,
                                MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }

                        override fun handleFault(fault: BackendlessFault?) {
                            Toast.makeText(applicationContext, "Неможливо вийти з аккаунту",
                                Toast.LENGTH_SHORT).show()
                        }
                    })
                }
                .setNegativeButton("Ні") { dialog, _ -> dialog.dismiss() }
                .show()
        }

        deleteCard.setOnClickListener {
        }

        configurationCard.setOnClickListener {
            val intent = Intent(this@SettingsActivity,
                ConfigurationActivity::class.java)
            startActivity(intent)
        }
    }
}
