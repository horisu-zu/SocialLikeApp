package com.example.loginapp.Models

import com.backendless.persistence.Point
import java.util.Date

class Place (
    var objectId: String = "",
    var description: String = "",
    var cathegory: String = "",
    var coordinates: Point,
    var hashtags: String = "",
    var created: String,
    var imageUrl: String? = null,
    var likeCount: Int = 0,
    var authorNickname: String = ""
)