package com.example.loginapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.files.BackendlessFile
import com.backendless.files.FileInfo
import com.example.loginapp.Adapters.FileAdapter
import com.example.loginapp.Listeners.FolderClickListener
import com.example.loginapp.Listeners.FolderFileClickListener
import com.example.loginapp.Models.Folder
import com.example.loginapp.Models.FolderFile
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.Locale

class FileActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fileAdapter: FileAdapter
    private lateinit var addButton: FloatingActionButton
    private val PICK_FILE_REQUEST = 1648
    //private var filePath: String = ""
    private var currentFolderPath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file)

        recyclerView = findViewById(R.id.fileRecycler)
        addButton = findViewById(R.id.addButton)
        recyclerView.layoutManager = LinearLayoutManager(this)
        fileAdapter = FileAdapter(emptyList(), fileClickListener)
        recyclerView.adapter = fileAdapter

        val folderPath = intent.getStringExtra("folderPath")

        if (!folderPath.isNullOrEmpty()) {
            loadFiles(folderPath)
            currentFolderPath = folderPath
        }

        addButton.setOnClickListener {
            openFilePicker()
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
                override fun handleResponse(response: BackendlessFile?) {
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
                            Log.d(
                                "FileActivity", "File name: $fileName, " +
                                        "File type: $fileType"
                            )
                            FolderFile(fileName, fileType)
                        }
                        fileAdapter.updateData(files)
                    }
                }

                override fun handleFault(fault: BackendlessFault?) {
                    Log.e("FileActivity", "Failed to load files: ${fault?.message}")
                }
            })
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

    private var fileClickListener = object : FolderFileClickListener {
        override fun onClick(file: FolderFile) {
            openFile(file)
        }

        override fun onLongClick(cardView: CardView?, file: FolderFile) {
        }
    }

    private fun openFile(file: FolderFile) {
        val fileUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider",
            File(filesDir, file.fileName))
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(fileUri, getMimeType(file.fileName))
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(intent)
    }

    private fun getMimeType(fileName: String): String {
        return when (fileName.substringAfterLast('.', "")) {
            "pdf" -> "application/pdf"
            "txt" -> "text/plain"
            "mp4", "mkv" -> "video/*"
            "jpg", "jpeg", "png" -> "image/*"
            "mp3", "wav" -> "audio/*"
            else -> "*/*"
        }
    }
}