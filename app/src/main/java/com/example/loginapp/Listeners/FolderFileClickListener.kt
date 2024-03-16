package com.example.loginapp.Listeners

import androidx.cardview.widget.CardView
import com.example.loginapp.Models.Folder
import com.example.loginapp.Models.FolderFile

interface FolderFileClickListener {
    fun onClick(file : FolderFile)
    fun onLongClick(cardView: CardView?, file : FolderFile)
}