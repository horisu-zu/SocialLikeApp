package com.example.loginapp.Listeners

import android.widget.ImageView
import com.example.loginapp.Models.User

interface FriendClickListener {
    fun onUserClick(user: User)
    fun onPopClick(user: User, imageView: ImageView)
}