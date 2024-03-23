package com.example.loginapp

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.Manifest
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import com.example.loginapp.Models.Place
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class CreatePlaceActivity : AppCompatActivity() {
    private lateinit var descriptionEditText: EditText
    private lateinit var locationTextView: TextView
    private lateinit var tagsEdit: EditText
    private lateinit var locationImage: ImageView
    private lateinit var addImageCard: CardView
    private lateinit var savePlaceButton: Button
    private lateinit var place: Place

    private val GALLERY_REQUEST_CODE: Int = 1648
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var tagsList: String

    private val SELECT_LOCATION_REQUEST_CODE = 476

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_place)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        descriptionEditText = findViewById(R.id.descriptionEditText)
        locationTextView = findViewById(R.id.locationTextView)
        tagsEdit = findViewById(R.id.tagsEdit)
        locationImage = findViewById(R.id.locationImage)
        addImageCard = findViewById(R.id.addImageCard)
        savePlaceButton = findViewById(R.id.savePlaceButton)

        locationTextView.setOnClickListener {
            showLocationOptionsDialog()
        }

        savePlaceButton.setOnClickListener {
            tagsList = tagsEdit.text.toString()
            getTags(tagsList)
        }

        addImageCard.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
        }

        tagsEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(tags: Editable?) {
                tags?.let { editable ->
                    val text = editable.toString()

                    if (text.isNotEmpty() && text.contains('#')) {
                        val lastCharIndex = text.length - 1

                        if (text[lastCharIndex] == ' ') {
                            editable.append("#")
                        }
                    }
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImage: Uri? = data.data
            locationImage.setImageURI(selectedImage)
        }
        else if (requestCode == SELECT_LOCATION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val latitude = data?.getDoubleExtra("latitude", 0.0)
            val longitude = data?.getDoubleExtra("longitude", 0.0)
        }
    }

    private fun getTags(list: String): List<String> {
        val tags = mutableListOf<String>()
        val text = list.split(" ")

        for(tag in text) {
            if(tag.startsWith("#")) {
                val extractTag = tag.substring(1)
                tags.add(extractTag)
            }
        }

        return tags
    }

    private fun showLocationOptionsDialog() {
        val options = arrayOf("Поточна позиція", "Обрати на карті")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Оберіть опцію")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> {
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            REQUEST_LOCATION_PERMISSION
                        )
                    } else {
                        getCurrentLocation()
                    }
                }
                1 -> {
                    openMapActivity()
                }
            }
        }
        builder.show()
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    val coordinates = "POINT($longitude $latitude)"

                    locationTextView.text = coordinates
                } else {
                    Toast.makeText(
                        this, "Не вдалося отримати місцеположення",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
    }

    private fun openMapActivity() {
        val intent = Intent(this, MapActivity::class.java)
        startActivityForResult(intent, SELECT_LOCATION_REQUEST_CODE)
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }
}