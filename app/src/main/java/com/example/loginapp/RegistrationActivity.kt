package com.example.loginapp

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import com.backendless.Backendless
import com.backendless.BackendlessUser
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.files.BackendlessFile
import com.google.android.material.button.MaterialButton
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
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
        val nicknameEditText : EditText = findViewById(R.id.editTextNickname)
        val ageEditText: EditText = findViewById(R.id.editTextRegistrationAge)
        val genderEditText: EditText = findViewById(R.id.editTextRegistrationGender)
        val countryEditText: EditText = findViewById(R.id.editTextRegistrationCountry)

        registrationButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val age = ageEditText.text.toString()
            val subscribersCount = 0
            val subscriptionsCount = 0
            val avatarPath = getDrawableImagePath(this@RegistrationActivity,
                R.drawable.placeholder_image)

            user.email = email
            user.password = password
            user.setProperty("name", nameEditText.text.toString())
            user.setProperty("nickname", nicknameEditText.text.toString())
            user.setProperty("age", age)
            user.setProperty("gender", genderEditText.text.toString())
            user.setProperty("country", countryEditText.text.toString())
            user.setProperty("subscribersCount", subscribersCount)
            user.setProperty("subscriptionsCount", subscriptionsCount)
            user.setProperty("avatarPath", avatarPath)

            if(isValidEmail(email) && isValidPassword(password) && isValidAge(age)) {
                Backendless.UserService.register(user, object : AsyncCallback<BackendlessUser> {
                    override fun handleResponse(response: BackendlessUser?) {
                        Toast.makeText(this@RegistrationActivity,
                            "Реєстрація проведена успішно", Toast.LENGTH_SHORT).show()

                        /*val currentUser = Backendless.UserService.CurrentUser()

                        val userFolder = "users/${currentUser.getProperty("nickname")}"

                        val avatarFolder = "users/${currentUser.getProperty("nickname")}/avatar"

                        currentUser.setProperty("avatarPath", avatarFolder)
                        setImageToFolder(currentUser)*/

                        /*Backendless.UserService.update(currentUser, object :
                                AsyncCallback<BackendlessUser> {
                            override fun handleResponse(updatedUser: BackendlessUser?) {
                            }

                            override fun handleFault(fault: BackendlessFault?) {
                            }
                        })*/

                        val intent = Intent(this@RegistrationActivity,
                            MainActivity::class.java)

                        startActivity(intent)
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
               Toast.makeText(this, "Невірний формат",
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

    private fun getDrawableImagePath(context: Context, @DrawableRes drawableResId: Int): String? {
        val bitmap = BitmapFactory.decodeResource(context.resources, drawableResId)
        val filesDir = context.filesDir
        val imageFile = File(filesDir, "placeholder_image.png")

        try {
            FileOutputStream(imageFile).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
            return imageFile.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    /*private fun setImageToFolder(user: BackendlessUser) {
        val placeholderImageBitmap: Bitmap = BitmapFactory.decodeResource(resources,
            R.drawable.placeholder_image)
        val byteArrayOutputStream = ByteArrayOutputStream()
        placeholderImageBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)

        val avatarFolder = user.getProperty("avatarFolder").toString()
        val fileName = "placeholder_image.png"

        Backendless.Files.Android.upload(placeholderImageBitmap,
            Bitmap.CompressFormat.PNG,
            100, fileName, avatarFolder, object : AsyncCallback<BackendlessFile> {
            override fun handleResponse(response: BackendlessFile) {
            }

            override fun handleFault(fault: BackendlessFault?) {
            }
        })
    }*/

}
