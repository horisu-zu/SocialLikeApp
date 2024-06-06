package com.example.loginapp.Listeners

import com.example.loginapp.Models.User

interface SearchClickListener {
    fun onUserClick(user: User)
    fun onSendRequestClick(user: User)
    fun onCancelRequestClick(user: User)
}