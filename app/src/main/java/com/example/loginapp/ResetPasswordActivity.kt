package com.example.loginapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.google.android.material.button.MaterialButton

class ResetPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        val emailEditText: EditText = findViewById(R.id.editTextForgotPasswordEmail)
        val resetPasswordButton: MaterialButton = findViewById(R.id.resetPasswordButton)

        resetPasswordButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()

            if (email.isNotEmpty()) {
                Backendless.UserService.restorePassword(email, object : AsyncCallback<Void?> {
                    override fun handleResponse(response: Void?) {
                        Toast.makeText(applicationContext,
                            "Перевірте свою електронну пошту для відновлення паролю",
                            Toast.LENGTH_SHORT).show()
                    }

                    override fun handleFault(fault: BackendlessFault?) {
                        val errorMessage = fault?.message ?: "Помилка відновлення паролю"
                        Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                Toast.makeText(applicationContext, "Введіть адресу електронної пошти", Toast.LENGTH_SHORT).show()
            }
        }
    }
}