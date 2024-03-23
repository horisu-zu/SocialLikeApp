package com.example.loginapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView

class PlaceActivity : AppCompatActivity() {
    private lateinit var placesRecycler: RecyclerView
    private lateinit var addButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place)

        placesRecycler = findViewById(R.id.placesRecycler)
        addButton = findViewById(R.id.addButton)

        addButton.setOnClickListener {
            val intent = Intent(this@PlaceActivity, CreatePlaceActivity::class.java)
            startActivity(intent)
        }
    }
}