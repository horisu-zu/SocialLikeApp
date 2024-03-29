package com.example.loginapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.files.BackendlessFile
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.io.File

class CreatePlaceActivity : AppCompatActivity() {
    private lateinit var descriptionLayout: TextInputLayout
    private lateinit var descriptionEditText: TextInputEditText
    private lateinit var locationTextView: TextView
    private lateinit var tagsEdit: EditText
    private lateinit var locationImage: ImageView
    private lateinit var addImageCard: CardView
    private lateinit var savePlaceButton: Button
    private lateinit var cathegoryField: TextInputLayout

    private val GALLERY_REQUEST_CODE: Int = 1648
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var tagsList: String

    private val SELECT_LOCATION_REQUEST_CODE = 476
    private lateinit var cathegoryItems: List<String>

    private lateinit var selectedImagePath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_place)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        descriptionLayout = findViewById(R.id.descriptionLayout)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        cathegoryField = findViewById(R.id.textField)
        locationTextView = findViewById(R.id.locationTextView)
        tagsEdit = findViewById(R.id.tagsEdit)
        locationImage = findViewById(R.id.locationImage)
        addImageCard = findViewById(R.id.addImageCard)
        savePlaceButton = findViewById(R.id.savePlaceButton)

        cathegoryItems = getCategories()
        val adapter = ArrayAdapter(this, R.layout.list_item, cathegoryItems)
        (cathegoryField.editText as? AutoCompleteTextView)?.setAdapter(adapter)

        locationTextView.setOnClickListener {
            showLocationOptionsDialog()
        }

        savePlaceButton.setOnClickListener {
            tagsList = tagsEdit.text.toString()
            getTags(tagsList)

            val userNickname = Backendless.UserService.CurrentUser().getProperty("nickname")
            val userId = Backendless.UserService.CurrentUser().objectId
            val description = descriptionEditText.text.toString()
            val location = locationTextView.text.toString()
            val cathegory = cathegoryField.editText?.text.toString()

            uploadImageToBackendless(selectedImagePath) { loadedImagePath ->
                val placeData = HashMap<String, Any>()
                placeData["description"] = description
                placeData["cathegory"] = cathegory
                placeData["coordinates"] = location
                placeData["hashtags"] = tagsList
                placeData["likeCount"] = 0
                placeData["authorNickname"] = userNickname
                placeData["authorId"] = userId
                placeData["imageUrl"] = loadedImagePath
                placeData["likedBy"] = "[]"

                savePlaceToBackendless(placeData)
            }
        }

        addImageCard.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
        }

        descriptionEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s != null && s.length > 100) {
                    descriptionLayout.error = "Перевищено максимальну кількість символів"
                } else {
                    descriptionLayout.error = null
                }
            }
        })

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
            selectedImagePath = getPathFromURI(selectedImage)
        }
        else if (requestCode == SELECT_LOCATION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val latitude = data?.getStringExtra("latitude").toString()
            val longitude = data?.getStringExtra("longitude").toString()
            val coordinates = "POINT($longitude $latitude)"

            locationTextView.text = coordinates
        }
    }

    private fun getPathFromURI(uri: Uri?): String {
        val cursor = contentResolver.query(uri!!, null, null, null,
            null)
        cursor!!.moveToFirst()
        val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
        val path = cursor.getString(idx)
        cursor.close()
        return path
    }

    private fun uploadImageToBackendless(selectedImagePath: String, callback: (String) -> Unit) {
        val userNickname: String = Backendless.UserService.CurrentUser().getProperty("baseNickname").toString()
        val imagePath = "PlaceImages/$userNickname/"

        Backendless.Files.upload(File(selectedImagePath), imagePath, object : AsyncCallback<BackendlessFile> {
            override fun handleResponse(response: BackendlessFile?) {
                val loadedImagePath = response?.fileURL ?: ""
                callback(loadedImagePath)
                Log.e("FILE URL", loadedImagePath)
            }

            override fun handleFault(fault: BackendlessFault) {
                callback("")
            }
        })
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
                    val latitude = location.latitude.toString()
                    val longitude = location.longitude.toString()
                    val coordinates = "POINT($longitude $latitude)"

                    locationTextView.text = coordinates
                } else {
                    Toast.makeText(
                        this, "Не вдалося отримати місцеположення",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun openMapActivity() {
        val intent = Intent(this, MapActivity::class.java)
        startActivityForResult(intent, SELECT_LOCATION_REQUEST_CODE)
    }

    fun getCategories(): List<String> {
        return listOf(
            "Парки та сади",
            "Музеї та галереї",
            "Історичні місця",
            "Ресторани та кав'ярні",
            "Театри та кінотеатри",
            "Пляжі та набережні",
            "Магазини та ринки",
            "Спортивні об'єкти",
            "Пам'ятники та скульптури",
            "Релігійні об'єкти"
        )
    }

    private fun savePlaceToBackendless(placeData: HashMap<String, Any>) {
        Backendless.Data.of("Place").save(placeData, object :
                AsyncCallback<MutableMap<Any?, Any?>> {
            override fun handleResponse(response: MutableMap<Any?, Any?>?) {
                Toast.makeText(this@CreatePlaceActivity, "Місце успішно додано",
                    Toast.LENGTH_SHORT).show()
            }

            override fun handleFault(fault: BackendlessFault) {
                Log.e("ERROR", "Помилка при " +
                        "додаванні місця: ${fault.message}")
            }
        })
    }

    interface ImageUploadCallback {
        fun onImageUploaded(imageUrl: String)
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }
}