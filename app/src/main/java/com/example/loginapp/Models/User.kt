package com.example.loginapp.Models

import com.backendless.persistence.Point

class User(
    var objectId: String,
    var email: String,
    var password: String,
    var name: String,
    var nickname: String,
    var baseNickname: String,
    var age: Int,
    var gender: String,
    var country: String,
    var subscribersCount: Int,
    var subscriptionsCount: Int,
    var avatarPath: String,
    var subscribedBy: List<String> = listOf(),
    var subscribedOn: List<String> = listOf(),
    var myLocation: Point?,
    var geolocationEnabled: Boolean,
    var friendsWith: List<String> = listOf(),
    var friendRequests: List<String> = listOf()
)