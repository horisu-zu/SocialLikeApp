package com.example.loginapp.Models

class Place (var objectId: String,
             var description: String,
             var latitude: Double,
             var longitude: Double,
             var hashtags: String,
             val metadata: String?,
             var createdAt: Long,
             var imageUrl: String?,
             var likeCount: Int = 0){
}