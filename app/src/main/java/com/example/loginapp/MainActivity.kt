package com.example.loginapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.backendless.Backendless
import com.backendless.BackendlessUser
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class MainActivity : AppCompatActivity() {

    private var isCheckBoxSelected : Boolean = false
    private val applicationId : String = "7FD7EA68-8D2D-9F4D-FF0A-5ADB25284600"
    private val apiKey : String = "CB86EDC0-E1F2-4A70-886B-32F00FDC755C"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_login)

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        if (Backendless.UserService.CurrentUser() != null) {
            val intent = Intent(this@MainActivity, HomeActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        Backendless.initApp(this, applicationId, apiKey)

        val loginEditText: EditText = findViewById(R.id.loginEdit)
        val passwordEditText: EditText = findViewById(R.id.passwordEdit)
        val loginButton: MaterialButton = findViewById(R.id.buttonLogin)
        val registrationButton: MaterialCardView = findViewById(R.id.noAccountCard)
        val forgotPasswordView: TextView = findViewById(R.id.forgotPasswordView)
        val checkBox : CheckBox = findViewById(R.id.checkBox)

        loginButton.setOnClickListener {
            val login = loginEditText.text.toString()
            val password = passwordEditText.text.toString()

            Backendless.UserService.login(login, password, object : AsyncCallback<BackendlessUser> {
                override fun handleResponse(response: BackendlessUser?) {
                    Toast.makeText(applicationContext, "Успішний вхід", Toast.LENGTH_SHORT).show()

                    sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()

                    sharedPreferences.edit().putString("userObjectId", response?.objectId).apply()

                    val intent = Intent(this@MainActivity, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                override fun handleFault(fault: BackendlessFault?) {
                    val errorMessage = fault?.message ?: "Помилка логіну"
                    Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_SHORT).show()
                    Log.e("Error: ", errorMessage)
                }
            })
        }

        registrationButton.setOnClickListener {
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
        }

        forgotPasswordView.setOnClickListener {
            val intent = Intent(this, ResetPasswordActivity::class.java)
            startActivity(intent)
        }

        checkBox.setOnCheckedChangeListener { _, isChecked ->
            isCheckBoxSelected = isChecked
            Log.d("MainActivity", "IsLoggedIn: $isChecked")
        }
    }

}
