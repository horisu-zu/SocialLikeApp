package com.example.loginapp

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.files.BackendlessFile
import com.backendless.files.FileInfo
import com.backendless.persistence.DataQueryBuilder
import com.example.loginapp.Adapters.FileAdapter
import com.example.loginapp.Listeners.FolderFileClickListener
import com.example.loginapp.Models.Defaults
import com.example.loginapp.Models.FolderFile
import com.example.loginapp.Models.User
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class FileActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fileAdapter: FileAdapter
    private lateinit var addButton: FloatingActionButton
    private lateinit var cameraButton: FloatingActionButton
    private lateinit var imageView: ImageView
    private val PICK_FILE_REQUEST = 1648
    private val REQUEST_IMAGE_CAPTURE = 1914
    //private var filePath: String = ""
    private val userList: MutableList<User> = mutableListOf()
    private var currentFolderPath: String = ""
    private val BASE_URL = "https://backendlessappcontent.com/${Defaults.applicationId}" +
            "/${Defaults.apiKey}/files/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file)

        recyclerView = findViewById(R.id.fileRecycler)
        addButton = findViewById(R.id.addButton)
        cameraButton = findViewById(R.id.cameraButton)
        imageView = findViewById<ImageView>(R.id.imageView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        fileAdapter = FileAdapter(emptyList(), fileClickListener)
        recyclerView.adapter = fileAdapter

        val folderPath = intent.getStringExtra("folderPath")
        getUsers()

        if (!folderPath.isNullOrEmpty()) {
            loadFiles(folderPath)
            currentFolderPath = folderPath
            Log.e("CURRENT FOLDER PATH", currentFolderPath)
        }

        addButton.setOnClickListener {
            openFilePicker()
        }

        imageView.setOnClickListener {
            if(imageView.isVisible) {
                imageView.visibility = View.GONE
            }
        }

        cameraButton.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            if (takePictureIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            } else {
                Toast.makeText(this, "Немає додатку для створення фото",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private var fileClickListener = object : FolderFileClickListener {
        override fun onClick(file: FolderFile) {
            openFile(file)
        }

        override fun onLongClick(cardView: CardView?, file: FolderFile) {
            showPopupMenu(cardView, file)
        }
    }

    private fun showPopupMenu(cardView: CardView?, fileFolder: FolderFile?) {
        val popupMenu = PopupMenu(this, cardView)
        popupMenu.menuInflater.inflate(R.menu.file_popup, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_download -> {
                    downloadFile(fileFolder!!)
                    true
                }
                R.id.action_access -> {
                    showAccessDialog(userList, fileFolder!!)
                    true
                }
                R.id.action_rename -> {
                    showRenameDialog(fileFolder!!.fileName)
                    true
                }
                R.id.action_delete -> {
                    deleteSelectedFile(fileFolder!!.fileName)
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun downloadFile(fileFolder: FolderFile) {
        val url = fileFolder.filePath
        val fileName = fileFolder.fileName
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadsDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)

        try {
            if (downloadsDir != null) {
                if (downloadsDir.canWrite() && downloadsDir.canRead()) {
                    Log.d("DownloadsDirectory", "Directory is writable and readable")
                } else {
                    Log.e("DownloadsDirectory", "Directory is not writable or readable")
                }
            } else {
                Log.e("DownloadsDirectory", "Downloads directory is null")
                return
            }

            Log.e("FILE PATH: ", url)
            val downloadUri = Uri.parse(url)
            Log.e("DOWNLOAD_URI", downloadUri.toString())
            val request = DownloadManager.Request(downloadUri)
                .setTitle(fileName)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                    fileName)
                .setNotificationVisibility(DownloadManager.Request
                    .VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setVisibleInDownloadsUi(true)

            downloadManager.enqueue(request)
        } catch (e: Exception) {
            Log.e("DownloadError", "Error downloading file: ${e.message}")
        }
    }


    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.setType("*/*")
        startActivityForResult(intent, PICK_FILE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null &&
            data.data != null) {
            val uri = data.data
            val filePath = uri?.let { getPathFromUri(this, it) }
            filePath?.let {
                uploadFile(it)
            }
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap

            uploadPhoto(imageBitmap)

            loadFiles(currentFolderPath)
        }
    }

    private fun uploadPhoto(bitmap: Bitmap) {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(Date())
        val fileName = "photo_$timeStamp.png"

        Backendless.Files.Android.upload(
            bitmap,
            Bitmap.CompressFormat.PNG,
            100,
            fileName,
            currentFolderPath,
            object : AsyncCallback<BackendlessFile> {
                override fun handleResponse(response: BackendlessFile?) {
                }

                override fun handleFault(fault: BackendlessFault?) {
                    Log.e("UploadPhoto", "Failed to upload pgoto: ${fault?.message}")
                }
            }
        )
    }

    private fun getFileFromUri(context: Context, uri: Uri): File? {
        val contentResolver = context.contentResolver
        val inputStream: InputStream?
        try {
            inputStream = contentResolver.openInputStream(uri)
            inputStream?.let {
                val fileName = getFileName(context, uri)
                val filesDir = context.filesDir
                val file = File(filesDir, fileName)
                FileOutputStream(file).use { outputStream ->
                    val buffer = ByteArray(4 * 1024)
                    var read: Int
                    while (inputStream.read(buffer).also { read = it } != -1) {
                        outputStream.write(buffer, 0, read)
                    }
                    outputStream.flush()
                }
                Log.e("FILE", file.absolutePath)
                return file
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    @SuppressLint("Range")
    private fun getFileName(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null,
                null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex("_display_name"))
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.lastPathSegment
        }
        return result ?: "file"
    }

    private fun getPathFromUri(context: Context, uri: Uri): String? {
        return getFileFromUri(context, uri)?.absolutePath
    }

    private fun uploadFile(filePath: String) {
        Backendless.Files.upload(File(filePath), currentFolderPath,
            object : AsyncCallback<BackendlessFile> {
                override fun handleResponse(uploadedFile: BackendlessFile?) {
                    Log.d("FileActivity", "File uploaded successfully")
                    loadFiles(currentFolderPath)
                }

                override fun handleFault(fault: BackendlessFault?) {
                    Log.e("FileActivity", "Failed to upload file: ${fault?.message}")
                }
            })
    }

    private fun loadFiles(folderPath: String) {
        Backendless.Files.listing(folderPath, "*",
            false, object : AsyncCallback<List<FileInfo?>?> {
                override fun handleResponse(response: List<FileInfo?>?) {
                    response?.let { fileInfoList ->
                        val files = fileInfoList.mapNotNull { fileInfo ->
                            val fileName = fileInfo?.name ?: ""
                            val fileType = getFileType(fileName)
                            val filePath = fileInfo!!.publicUrl
                            Log.d(
                                "FileActivity", "File name: $fileName, " +
                                        "File type: $fileType, File: $filePath"
                            )
                            FolderFile(fileName, fileType, filePath)
                        }
                        fileAdapter.updateData(files)
                    }
                }

                override fun handleFault(fault: BackendlessFault?) {
                    Log.e("FileActivity", "Failed to load files: ${fault?.message}")
                }
            })
    }

    private fun showAccessDialog(userList: List<User>, fileFolder: FolderFile) {
        val userNames = userList.map { it.baseNickname }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Select User")
            .setItems(userNames) { dialog, which ->
                val selectedUser = userList[which]
                grantAccessToFile(selectedUser, fileFolder)
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    private fun getUsers() {
        val currentObjectId = Backendless.UserService.CurrentUser().objectId
        val whereClause = "objectId != '$currentObjectId'"

        val queryBuilder = DataQueryBuilder.create().setWhereClause(whereClause)

        Backendless.Data.of("Users").find(queryBuilder,
            object : AsyncCallback<List<Map<*, *>?>?> {
                override fun handleResponse(foundUsers: List<Map<*, *>?>?) {
                    foundUsers?.forEach { userData ->
                        val user = User(
                            email = userData!!["email"].toString(),
                            password = userData["password"].toString(),
                            name = userData["name"].toString(),
                            nickname = userData["nickname"].toString(),
                            baseNickname = userData["baseNickname"].toString(),
                            age = userData["age"].toString().toInt(),
                            gender = userData["gender"].toString(),
                            country = userData["country"].toString(),
                            subscribersCount = userData["subscribersCount"].toString().toInt(),
                            subscriptionsCount = userData["subscriptionsCount"].toString().toInt(),
                            avatarPath = userData["avatarPath"].toString()
                        )
                        userList.add(user)
                        Log.e("USER", user.baseNickname)
                    }
                }

                override fun handleFault(fault: BackendlessFault) {
                    Log.e("Error", fault.message)
                }
        })
    }

    private fun grantAccessToFile(user: User, fileFolder: FolderFile) {
        val fileName = fileFolder.fileName
        val sharedFileName = "${fileName.substringBeforeLast('.')}.txt"
        val sharedFilePath = "users/${user.baseNickname}/shared with me/$sharedFileName"

        val fileUrl = "${BASE_URL}${currentFolderPath}/$fileName"

        Backendless.Files.saveFile(
            sharedFilePath,
            fileUrl.toByteArray(),
            true,
            object : AsyncCallback<String> {
                override fun handleResponse(response: String?) {
                    Log.d("FileActivity", "File shared successfully")
                }

                override fun handleFault(fault: BackendlessFault?) {
                    Log.e("FileActivity", "Failed to share file: ${fault?.message}")
                }
            }
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showRenameDialog(fileName: String) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        val input = EditText(this)
        val fileNameWithoutExtension = fileName.substringBeforeLast('.')
        input.setText(fileNameWithoutExtension)

        alertDialogBuilder.apply {
            setTitle("Перейменувати файл")
            setView(input)
            setPositiveButton("OK") { _, _ ->
                val newName = input.text.toString()
                renameFile(fileName, newName, currentFolderPath)
            }
            setNegativeButton("Відмінити") { _, _ ->
            }
        }.create().show()
    }

    private fun renameFile(oldFileName: String, newFileName: String, folderPath: String) {
        val extension = oldFileName.substringAfterLast('.', "")
        Log.e("EXTENSION", extension)
        val newFileNameWithExtension = "$newFileName.$extension"

        Backendless.Files.renameFile("$folderPath/$oldFileName", newFileNameWithExtension,
            object : AsyncCallback<String> {
                override fun handleResponse(response: String?) {
                    Log.d("FileActivity", "File renamed successfully")
                    loadFiles(folderPath)
                }

                override fun handleFault(fault: BackendlessFault?) {
                    Log.e("FileActivity", "Failed to rename file: ${fault?.message}")
                }
            })
    }

    private fun deleteSelectedFile(fileName: String) {
        Backendless.Files.remove("$currentFolderPath/$fileName",
            object : AsyncCallback<Int?> {
                override fun handleResponse(response: Int?) {
                    Log.d("FileActivity", "File deleted successfully")
                    loadFiles(currentFolderPath)
                }

                override fun handleFault(fault: BackendlessFault?) {
                    Log.e("FileActivity", "Failed to delete file: ${fault?.message}")
                }
            })
    }

    private fun openFile(file: FolderFile) {
        val filePath = file.filePath

        if (currentFolderPath.contains("shared with me")) {
            readFile(filePath,
                onSuccess = { fileContent ->
                    Log.i(TAG, "File content: $fileContent")
                    if (isImageFile(fileContent)) {
                        displayImage(Uri.parse(fileContent))
                    } else {
                        openFileExternal(Uri.parse(fileContent))
                    }
                },
                onFailure = { errorMessage ->
                    Log.e(TAG, errorMessage)
                }
            )
        } else {
            val intent = Intent(applicationContext, WebViewActivity::class.java)
            intent.putExtra("fileUrl", filePath)
            startActivity(intent)
        }
    }

    private fun isImageFile(filePath: String): Boolean {
        val imageExtensions = arrayOf("jpg", "jpeg", "png", "gif")
        val fileExtension = getFileExtension(filePath)
        return imageExtensions.contains(fileExtension)
    }

    private fun getFileExtension(filePath: String): String {
        return filePath.substring(filePath.lastIndexOf(".") + 1)
    }

    private fun displayImage(imageUri: Uri) {
        Picasso.get().load(imageUri)
            .into(imageView, object : Callback {
                override fun onSuccess() {
                    imageView.visibility = View.VISIBLE
                }

                override fun onError(e: Exception?) {
                    Log.e(TAG, "Error loading image: $e")
                }
            })
    }

    private fun openFileExternal(fileUri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = fileUri
        startActivity(intent)
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun readFile(fileUrl: String, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(fileUrl)
                .build()

            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val fileContent = response.body()!!.string()
                    withContext(Dispatchers.Main) {
                        onSuccess(fileContent)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onFailure("Server returned non-successful response")
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    onFailure("Error reading file: ${e.message}")
                }
            }
        }
    }

    private fun getFileType(fileName: String): String {
        val extension = fileName.substringAfterLast('.', "")
        return when (extension.toLowerCase(Locale.getDefault())) {
            "pdf", "txt" -> "Document"
            "mp4", "mkv" -> "Video"
            "jpg", "jpeg", "png", "gif" -> "Image"
            "mp3", "wav" -> "Audio"
            else -> "Unknown"
        }
    }
}
