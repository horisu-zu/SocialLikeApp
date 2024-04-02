package com.example.loginapp

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.backendless.Backendless
import com.backendless.BackendlessUser
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.files.BackendlessFile
import com.backendless.files.FileInfo
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Picasso
import java.io.File
import java.io.FileOutputStream

class ConfigurationActivity : AppCompatActivity() {
    private lateinit var user: BackendlessUser
    private lateinit var avatarPath: String
    private val REQUEST_SELECT_IMAGE_FROM_DEVICE = 1939

    private lateinit var editName: EditText
    private lateinit var editNickname: EditText
    private lateinit var editAvatar: FloatingActionButton
    private lateinit var saveButton: CardView
    private lateinit var passwordCard: CardView

    private lateinit var previewAvatar: ImageView
    private lateinit var previewName: TextView
    private lateinit var previewNickname: TextView
    private lateinit var previewSubscriptionsCount: TextView
    private lateinit var previewSubscribersCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuration)
        user = Backendless.UserService.CurrentUser() ?: BackendlessUser()

        editName = findViewById(R.id.editName)
        editNickname = findViewById(R.id.editNickname)
        editAvatar = findViewById(R.id.fabUploadImage)
        saveButton = findViewById(R.id.saveCard)
        passwordCard = findViewById(R.id.passwordCard)

        previewAvatar = findViewById(R.id.avatarImageView)
        previewName = findViewById(R.id.nameTextView)
        previewNickname = findViewById(R.id.nicknameTextView)
        previewSubscribersCount = findViewById(R.id.subscribersCount)
        previewSubscriptionsCount = findViewById(R.id.subscriptionsCount)

        getUserInfo()

        editAvatar.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(this@ConfigurationActivity)
            dialogBuilder.setTitle("Виберіть джерело:")
            dialogBuilder.setItems(arrayOf("Локальний пристрій", "Сервер")) { _, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(Intent.ACTION_GET_CONTENT)
                        intent.type = "image/*"
                        startActivityForResult(intent, REQUEST_SELECT_IMAGE_FROM_DEVICE)
                    }
                    1 -> {
                        showServerImageListDialog()
                    }
                }
            }
            dialogBuilder.show()
        }

        passwordCard.setOnClickListener {
            val intent = Intent(this@ConfigurationActivity,
                PasswordConfigurationActivity::class.java)
            startActivity(intent)
        }

        saveButton.setOnClickListener {
            val nickname = previewNickname.text.toString().substringAfterLast("@")
            user.setProperty("name", previewName.text.toString())
            user.setProperty("nickname", nickname)
            if (::avatarPath.isInitialized) {
                user.setProperty("avatarPath", avatarPath)
            } else {
                Toast.makeText(this, "No new avatar selected", Toast.LENGTH_SHORT).show()
            }

            Backendless.UserService.update(user, object: AsyncCallback<BackendlessUser> {
                override fun handleResponse(response: BackendlessUser?) {
                    Toast.makeText(this@ConfigurationActivity, "Дані успішно оновлено",
                        Toast.LENGTH_SHORT).show()

                    val intent = Intent(this@ConfigurationActivity,
                        HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                override fun handleFault(fault: BackendlessFault?) {
                    Toast.makeText(this@ConfigurationActivity,
                        "Помилка при оновленні даних", Toast.LENGTH_SHORT).show()
                }

            })
        }

        editName.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val getName = editName.text.toString()
                updateProfilePreviewName(getName)
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })

        editNickname.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val getNickname = editNickname.text.toString()
                updateProfilePreviewNickname(getNickname)
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
    }

    @SuppressLint("SetTextI18n")
    private fun getUserInfo() {
        previewName.text = user.getProperty("name").toString()
        previewNickname.text = "@${user.getProperty("nickname")}"
        previewSubscribersCount.text = user.getProperty("subscribersCount").toString()
        previewSubscriptionsCount.text = user.getProperty("subscriptionsCount").toString()

        val oldAvatarPath = user.getProperty("avatarPath").toString()
        Log.e("AVATAR", oldAvatarPath)
        if (oldAvatarPath.isNotEmpty()) {
            Picasso.get().load(oldAvatarPath).into(previewAvatar)
        } else {
            previewAvatar.setImageResource(R.drawable.placeholder_image)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateProfilePreviewName(name: String) {
        previewName.text = name
    }

    @SuppressLint("SetTextI18n")
    private fun updateProfilePreviewNickname(nickname: String) {
        previewNickname.text = "@$nickname"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_SELECT_IMAGE_FROM_DEVICE -> {
                    data?.data?.let { uri ->
                        val file = getFileFromUri(uri)
                        if (file != null) {
                            val remotePath = "users/${user.getProperty("baseNickname")}" +
                                    "/profile_pictures"
                            uploadImage(file, remotePath)
                        } else {
                            Log.e("TAG", "File is null")
                        }
                    }
                }
            }
        }
    }

    private fun getFileFromUri(uri: Uri): File? {
        val inputStream = contentResolver.openInputStream(uri)
        val fileName = getFileNameFromUri(uri)
        val outputFile = File(cacheDir, fileName)
        inputStream?.use { input ->
            FileOutputStream(outputFile).use { output ->
                input.copyTo(output)
            }
        }
        return outputFile
    }

    private fun getFileNameFromUri(uri: Uri): String {
        var fileName = ""
        val cursor = contentResolver.query(uri, null, null, null,   null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    fileName = it.getString(displayNameIndex)
                }
            }
        }
        return fileName
    }

    private fun uploadImage(file: File, remotePath: String) {
        Backendless.Files.upload(file, remotePath, true, null,
            object : AsyncCallback<BackendlessFile> {
                override fun handleResponse(response: BackendlessFile?) {
                    response?.fileURL?.let { url ->
                        Log.e("URL", url)
                        avatarPath = url
                        loadAndSetImage(url)
                    }
                    Toast.makeText(this@ConfigurationActivity,
                        "Перевірте прев'ю :)", Toast.LENGTH_SHORT).show()
                }

                override fun handleFault(fault: BackendlessFault?) {
                    Toast.makeText(this@ConfigurationActivity, "Помилка завантаження файлу",
                        Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showServerImageListDialog() {
        val folderPath = "users/${user.getProperty("baseNickname")}/profile_pictures"

        Backendless.Files.listing(folderPath, "*", false,
            object : AsyncCallback<List<FileInfo>> {
                override fun handleResponse(response: List<FileInfo>?) {
                    val imageNames = mutableListOf<String>()
                    response?.forEach { fileInfo ->
                        val extension = fileInfo.name.substringAfterLast(".")
                        if (isImageFile(extension)) {
                            imageNames.add(fileInfo.name)
                        }
                    }

                    val items = imageNames.toTypedArray()

                    AlertDialog.Builder(this@ConfigurationActivity)
                        .setTitle("Оберіть зображення")
                        .setItems(items) { _, which ->
                            val selectedFileName = items[which]
                            val selectedImageUrl = response?.firstOrNull { fileInfo ->
                                fileInfo.name == selectedFileName }!!.publicUrl

                            Log.e("SELECTED URL", selectedImageUrl)

                            avatarPath = selectedImageUrl
                            loadAndSetImage(selectedImageUrl.toString())
                        }
                        .setNegativeButton("Відмінити", null)
                        .show()
                }

                override fun handleFault(fault: BackendlessFault?) {
                    Toast.makeText(this@ConfigurationActivity,
                        "Помилка при отриманні списку зображень: " +
                                "${fault?.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun loadAndSetImage(imageUrl: String) {
        Picasso.get().load(imageUrl).into(previewAvatar)
    }

    private fun isImageFile(extension: String): Boolean {
        return extension.equals("jpg", true) ||
                extension.equals("jpeg", true) ||
                extension.equals("png", true) ||
                extension.equals("gif", true) ||
                extension.equals("bmp", true)
    }
}