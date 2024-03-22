package com.example.loginapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.backendless.Backendless
import com.backendless.BackendlessUser
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault

class PasswordConfigurationActivity : AppCompatActivity() {

    private lateinit var editOld: EditText
    private lateinit var editNew: EditText
    private lateinit var editConfirmation: EditText
    private lateinit var applyCard: CardView

    private lateinit var user: BackendlessUser
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_configuration)
        user = Backendless.UserService.CurrentUser() ?: BackendlessUser()

        editOld = findViewById(R.id.baseNicknameEdit)
        editNew = findViewById(R.id.newPasswordEdit)
        editConfirmation = findViewById(R.id.confirmationPasswordEdit)
        applyCard = findViewById(R.id.saveCard)

        applyCard.setOnClickListener {
            val newPassword = editNew.text.toString()
            val baseNickname = editOld.text.toString()
            val confirmationPassword = editConfirmation.text.toString()

            if(oldCheck(baseNickname)) {
                if(confirmationCheck(newPassword, confirmationPassword)) {
                    user.password = newPassword

                    Backendless.UserService.update(user, object: AsyncCallback<BackendlessUser> {
                        override fun handleResponse(response: BackendlessUser?) {
                            Toast.makeText(this@PasswordConfigurationActivity,
                                "Оновлення паролю завершено успішно", Toast.LENGTH_SHORT).show()

                            val intent = Intent(this@PasswordConfigurationActivity,
                                SettingsActivity::class.java)
                            startActivity(intent)
                            finish()
                        }

                        override fun handleFault(fault: BackendlessFault?) {
                            Toast.makeText(this@PasswordConfigurationActivity,
                                "Помилка оновлення паролю", Toast.LENGTH_SHORT).show()
                        }
                    })
                } else{
                    Toast.makeText(this, "Помилка підтвердження паролю",
                        Toast.LENGTH_SHORT).show()
                }
            } else{
                Toast.makeText(this, "Введено неправильний пароль",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun oldCheck(baseNickname: String): Boolean {
        val userBaseNickname = user.getProperty("baseNickname")

        return baseNickname == userBaseNickname
    }

    private fun confirmationCheck(newPassword: String, confirmationPassword: String): Boolean {
        return newPassword.equals(confirmationPassword)
    }
}