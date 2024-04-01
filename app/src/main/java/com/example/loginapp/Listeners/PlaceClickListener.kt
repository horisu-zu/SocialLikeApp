package com.example.loginapp.Listeners

import android.widget.ImageView
import com.example.loginapp.Models.Place

interface PlaceClickListener {
    fun onLikeClick(place: Place)
    fun onBookmarkClick(place: Place)
    fun onCategoryClick(place: Place)
    fun onPopClick(place: Place, imageView: ImageView)
    fun onMapClick(place: Place)
}