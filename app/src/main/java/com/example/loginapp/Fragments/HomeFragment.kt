package com.example.loginapp.Fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.backendless.Backendless
import com.backendless.BackendlessUser
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.files.FileInfo
import com.example.loginapp.Adapters.FolderAdapter
import com.example.loginapp.FileActivity
import com.example.loginapp.Listeners.FolderClickListener
import com.example.loginapp.Models.Folder
import com.example.loginapp.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var folderAdapter: FolderAdapter
    private lateinit var addButton: FloatingActionButton
    private var foldersList: MutableList<Folder> = mutableListOf()
    private val currentUser: BackendlessUser = Backendless.UserService.CurrentUser()
    private val userNickname: String? = currentUser.getProperty("nickname") as? String

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        addButton = view.findViewById(R.id.addButton)
        recyclerView = view.findViewById(R.id.postRecyclerView)
        recyclerView.layoutManager = StaggeredGridLayoutManager(2,
            StaggeredGridLayoutManager.VERTICAL)
        folderAdapter = FolderAdapter(mutableListOf(), folderClickListener)
        recyclerView.adapter = folderAdapter

        loadFolders()

        addButton.setOnClickListener {
            showCreateFolderDialog()
            folderAdapter.notifyDataSetChanged()
        }

        return view
    }

    private var folderClickListener = object : FolderClickListener {
        override fun onClick(folder: Folder?) {
            folder?.let {
                val intent = Intent(context, FileActivity::class.java)
                intent.putExtra("folderPath", "users/$userNickname/${it.title}")
                startActivity(intent)
            }
        }

        override fun onLongClick(cardView: CardView?, folder: Folder?) {
            folder?.let {
                showPopupMenu(cardView, folder)
            }
        }
    }

    private fun showPopupMenu(cardView: CardView?, folder: Folder?) {
        val popupMenu = PopupMenu(requireContext(), cardView)
        popupMenu.menuInflater.inflate(R.menu.folder_popup, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_rename -> {
                    //showRenameDialog(folder!!.title)
                    true
                }
                R.id.action_delete -> {
                    deleteFolder(folder!!.title)
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showRenameDialog(folderName: String) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        val input = EditText(requireContext())
        input.setText(folderName)
        alertDialogBuilder.apply {
            setTitle("Перейменувати папку")
            setView(input)
            setPositiveButton("OK") { _, _ ->
                val newName = input.text.toString()
                renameFolder(folderName, newName)
            }
            setNegativeButton("Відмінити") { _, _ ->
            }
        }.create().show()
    }

    private fun loadFolders() {
        if (userNickname != null) {
            val foldersPath: String = "users/$userNickname"
            val foldersList: MutableList<Folder> = mutableListOf()

            Backendless.Files.listing(
                foldersPath,
                "*",
                false,
                object : AsyncCallback<List<FileInfo?>?> {
                    override fun handleResponse(response: List<FileInfo?>?) {
                        response?.let { fileInfoList ->
                            for (fileInfo in fileInfoList) {
                                fileInfo?.let {
                                    val folderName = it.name
                                    val folderPath = "$foldersPath/$folderName"

                                    Backendless.Files.listing(
                                        folderPath,
                                        "*",
                                        false,
                                        object : AsyncCallback<List<FileInfo?>?> {
                                            override fun handleResponse(subResponse:
                                                                        List<FileInfo?>?) {
                                                subResponse?.let { subFileInfoList ->
                                                    val fileCount = subFileInfoList.size
                                                    foldersList.add(Folder(folderName, fileCount))
                                                    Log.d("Folder: ", "$folderName - " +
                                                            "$fileCount")

                                                    folderAdapter.updateData(foldersList)
                                                }
                                            }

                                            override fun handleFault(fault: BackendlessFault?) {
                                                Log.e("Folder Error: ", "Error: " +
                                                        "${fault?.message}")
                                            }
                                        })
                                }
                            }
                        }
                    }

                    override fun handleFault(fault: BackendlessFault?) {
                        Log.e("Folder Error: ", "Error: ${fault?.message}")
                    }
                })
        }
    }

    private fun createFolder(folderName: String) {
        val userNickname = Backendless.UserService.CurrentUser().getProperty("nickname") as? String
        if (userNickname != null) {
            val path = "users/$userNickname/$folderName"

            Backendless.Files.exists(path, object : AsyncCallback<Boolean> {
                override fun handleResponse(exists: Boolean?) {
                    if (exists != null && exists) {
                        showReplaceDialog(folderName, path)
                    } else {
                        createNewFolder(folderName, path)
                    }
                }

                override fun handleFault(fault: BackendlessFault?) {
                    activity?.runOnUiThread {
                        Toast.makeText(
                            context,
                            "Помилка при перевірці існування папки: ${fault?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })
        }
    }

    private fun showReplaceDialog(folderName: String, path: String) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.apply {
            setTitle("Папка вже існує")
            setMessage("Папка \"$folderName\" вже існує. Бажаєте перезаписати?")
            setPositiveButton("Да") { _, _ ->
                createNewFolder(folderName, path)
            }
            setNegativeButton("Відмінити") { _, _ ->
            }
        }.create().show()
    }

    private fun createNewFolder(folderName: String, path: String) {
        Backendless.Files.saveFile(
            path,
            folderName,
            ByteArray(0),
            true,
            object : AsyncCallback<String?> {
                override fun handleResponse(response: String?) {
                    activity?.runOnUiThread {
                        Toast.makeText(
                            context,
                            "Створено нову папку: $folderName",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    removeFile(folderName, path)
                    loadFolders()
                }

                override fun handleFault(fault: BackendlessFault?) {
                    activity?.runOnUiThread {
                        Toast.makeText(
                            context,
                            "Помилка при створенні папки: ${fault?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        )
    }

    private fun removeFile(remoteName: String, remotePath: String) {
        Backendless.Files.remove(
            "$remotePath/$remoteName",
            object : AsyncCallback<Int?> {
                override fun handleResponse(response: Int?) {
                    Log.d("REMOVE FILE", "Виделення файлу успішно")
                }

                override fun handleFault(fault: BackendlessFault?) {
                    Log.e("REMOVE FILE", "Не вдалося видалити файл: ${fault?.message}")
                }
            }
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showCreateFolderDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Введіть назву папки")

        val input = EditText(requireContext())
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        input.layoutParams = layoutParams
        builder.setView(input)

        builder.setPositiveButton("Створити нову папку") { dialog, _ ->
            val folderName = input.text.toString()
            createFolder(folderName)
            folderAdapter.notifyDataSetChanged()
            dialog.dismiss()
        }

        builder.setNegativeButton("Відмінити") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun deleteFolder(folderName: String) {
        if (userNickname != null) {
            val path = "users/$userNickname/$folderName"

            Backendless.Files.remove(path, object : AsyncCallback<Int?> {
                override fun handleResponse(response: Int?) {
                    val position = foldersList.indexOfFirst { it.title == folderName }
                    if (position != -1) {
                        folderAdapter.removeFolder(position)
                    } else {
                        Log.e("Delete Folder", "Папку не знайдено")
                    }
                }

                override fun handleFault(fault: BackendlessFault?) {
                    Log.e("Delete Folder", "Помилка при видаленні: ${fault?.message}")
                }
            })

            loadFolders()
        }
    }

    private fun renameFolder(oldName: String, newName: String) {
        if (userNickname != null) {
            val oldPath = "users/$userNickname/$oldName"
            val newPath = "users/$userNickname/$newName"

            Backendless.Files.renameFile(oldPath, newPath, object : AsyncCallback<String?> {
                override fun handleResponse(response: String?) {
                }

                override fun handleFault(fault: BackendlessFault?) {
                    Log.e("RenameFolder", "Failed to rename folder: ${fault?.message}")
                }
            })
            loadFolders()
        }
    }
}