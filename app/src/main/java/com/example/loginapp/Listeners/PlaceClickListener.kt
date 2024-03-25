package com.example.loginapp.Listeners

import com.example.loginapp.Models.Place

interface PlaceClickListener {
    fun onLikeClick(place: Place)
    fun onBookmarkClick(place: Place)
}