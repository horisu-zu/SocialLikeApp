package com.example.loginapp

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.backendless.Backendless
import com.backendless.BackendlessUser
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.google.android.material.button.MaterialButton
import java.util.regex.Pattern

class RegistrationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        var user = BackendlessUser()

        val registrationButton: MaterialButton = findViewById(R.id.registrationButton)
        val emailEditText: EditText = findViewById(R.id.editTextRegistrationEmail)
        val passwordEditText: EditText = findViewById(R.id.editTextRegistrationPassword)
        val nameEditText: EditText = findViewById(R.id.editTextRegistrationName)
        val ageEditText: EditText = findViewById(R.id.editTextRegistrationAge)
        val genderEditText: EditText = findViewById(R.id.editTextRegistrationGender)
        val countryEditText: EditText = findViewById(R.id.editTextRegistrationCountry)

        registrationButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val age = ageEditText.text.toString()

            user.email = email
            user.password = password
            user.setProperty("name", nameEditText.text.toString())
            user.setProperty("age", age)
            user.setProperty("gender", genderEditText.text.toString())
            user.setProperty("country", countryEditText.text.toString())

            if(isValidEmail(email) && isValidPassword(password) && isValidAge(age)) {
                Backendless.UserService.register(user, object : AsyncCallback<BackendlessUser> {
                    override fun handleResponse(response: BackendlessUser?) {
                        Toast.makeText(this@RegistrationActivity,
                            "Реєстрація проведена успішно", Toast.LENGTH_SHORT).show()
                    }

                    override fun handleFault(fault: BackendlessFault?) {
                        val errorMessage = fault?.message ?: "Помилка реєстрації"
                        Log.e("RegistrationActivity", "Backendless error: " +
                                "${fault?.code}, ${fault?.detail}")
                        Toast.makeText(this@RegistrationActivity, errorMessage,
                            Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
               Toast.makeText(this, "Невірний формат електронної пошти",
                   Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex =
            "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})"
        val pattern = Pattern.compile(emailRegex)
        return pattern.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        val passwordRegex = "^(?=.*[0-9]).{8,}\$"
        val pattern = Pattern.compile(passwordRegex)
        return pattern.matcher(password).matches()
    }

    private fun isValidAge(age: String): Boolean {
        val ageInt = age.toIntOrNull()
        return ageInt != null && ageInt >= 5
    }
}
