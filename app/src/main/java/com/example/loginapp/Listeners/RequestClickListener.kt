package com.example.loginapp.Listeners

import com.example.loginapp.Models.User

interface RequestClickListener {
    fun onAcceptClick(user: User)
    fun onDeclineClick(user: User)
}