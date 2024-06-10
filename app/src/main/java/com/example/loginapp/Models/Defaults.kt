package com.example.loginapp.Models

object Defaults {
    const val applicationId : String = "7FD7EA68-8D2D-9F4D-FF0A-5ADB25284600"
    const val apiKey : String = "CB86EDC0-E1F2-4A70-886B-32F00FDC755C"
    const val serverUrl: String = "https://api.backendless.com"
    const val devEmail: String = "kosyakinrostislav@gmail.com"
}

object CurrentUserItems {
    var currentUserId: String? = null

    fun setCurrentId(currentId: String) {
        currentUserId = currentId
    }
}

object UserListManager {
    private var userList: List<User>? = null

    fun setUserList(list: List<User>) {
        userList = list
    }

    fun getUserList(): List<User>? {
        return userList
    }
}