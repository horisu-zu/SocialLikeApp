package com.example.loginapp.Models

class Place (var objectId: String,
             var description: String,
             var latitude: Double,
             var longitude: Double,
             var hashtags: List<String>,
             val locationName: String?,
             var createdAt: String,
             var imageUrl: String?,
             var likeCount: Int,
             var authorNickname: String){
}