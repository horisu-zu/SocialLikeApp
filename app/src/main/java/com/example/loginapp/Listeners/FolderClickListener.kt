package com.example.loginapp.Listeners

import androidx.cardview.widget.CardView
import com.example.loginapp.Models.Folder

interface FolderClickListener {
    fun onClick(folder: Folder?)
    fun onLongClick(cardView: CardView?, folder: Folder?)
}