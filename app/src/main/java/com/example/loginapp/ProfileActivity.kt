package com.example.loginapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import com.backendless.Backendless
import com.backendless.BackendlessUser
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.persistence.DataQueryBuilder
import com.backendless.persistence.Point
import com.example.loginapp.Fragments.Place.EmptyPlaceFragment
import com.example.loginapp.Fragments.Place.PlaceFragment
import com.example.loginapp.Models.Place
import com.google.android.gms.location.*
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileActivity : AppCompatActivity() {
    private lateinit var user: BackendlessUser
    private lateinit var avatarPath: String

    private lateinit var profileAvatar: ImageView
    private lateinit var profileName: TextView
    private lateinit var profileNickname: TextView
    private lateinit var profileSubscriptionsCount: TextView
    private lateinit var profileSubscribersCount: TextView
    private lateinit var profileCreationDate: TextView
    private lateinit var profileGeolocation: TextView

    private lateinit var backCard: CardView
    private lateinit var configurationCard: CardView
    private lateinit var postItem: TextView
    private lateinit var likeItem: TextView
    private lateinit var placeItem: TextView
    private lateinit var postIndicator: View
    private lateinit var likeIndicator: View
    private lateinit var placeIndicator: View

    private val LOCATION_REQUEST_CODE = 1869

    private val placeList: MutableList<Place> = ArrayList()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        user = Backendless.UserService.CurrentUser() ?: BackendlessUser()

        getUserPlaceFromServer()

        profileName = findViewById(R.id.nameTextView)
        profileNickname = findViewById(R.id.nicknameTextView)
        profileSubscribersCount = findViewById(R.id.subscribersCount)
        profileSubscriptionsCount = findViewById(R.id.subscriptionsCount)
        profileAvatar = findViewById(R.id.avatarImageView)
        profileGeolocation = findViewById(R.id.locationView)
        profileCreationDate = findViewById(R.id.createDate)

        backCard = findViewById(R.id.backCard)
        configurationCard = findViewById(R.id.configurationCard)
        postItem = findViewById(R.id.postNavigationItem)
        likeItem = findViewById(R.id.likeNavigationItem)
        placeItem = findViewById(R.id.placeNavigationItem)

        postIndicator = findViewById(R.id.postIndicator)
        likeIndicator = findViewById(R.id.likeIndicator)
        placeIndicator = findViewById(R.id.placeIndicator)

        selectNavigationItem(R.id.postNavigationItem)

        getUserInfo()

        postItem.setOnClickListener {
            selectNavigationItem(R.id.postNavigationItem)
        }

        likeItem.setOnClickListener {
            selectNavigationItem(R.id.likeNavigationItem)
        }

        placeItem.setOnClickListener {
            selectNavigationItem(R.id.placeNavigationItem)
        }

        backCard.setOnClickListener {
            onBackPressed()
        }

        configurationCard.setOnClickListener {
            val intent = Intent(this, ConfigurationActivity::class.java)
            startActivity(intent)
        }

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation()
        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
        }
    }
    @SuppressLint("SetTextI18n")
    private fun getUserInfo() {
        val createdDate = user.getProperty("created") as Date
        val locale = Locale("uk", "UA")
        val dateFormatter = SimpleDateFormat("dd MMMM yyyy", locale)
        val formattedDate = dateFormatter.format(createdDate)

        profileName.text = user.getProperty("name").toString()
        profileNickname.text = "@${user.getProperty("nickname")}"
        profileSubscribersCount.text = user.getProperty("subscribersCount").toString()
        profileSubscriptionsCount.text = user.getProperty("subscriptionsCount").toString()
        avatarPath = user.getProperty("avatarPath").toString()
        profileCreationDate.text = formattedDate
        profileGeolocation.text = user.getProperty("myLocation").toString()

        Picasso.get().load(avatarPath).into(profileAvatar)
    }

    private fun selectNavigationItem(selectedItemId: Int) {
        postIndicator.visibility = if (selectedItemId == R.id.postNavigationItem)
            View.VISIBLE else View.INVISIBLE
        likeIndicator.visibility = if (selectedItemId == R.id.likeNavigationItem)
            View.VISIBLE else View.INVISIBLE
        placeIndicator.visibility = if (selectedItemId == R.id.placeNavigationItem)
            View.VISIBLE else View.INVISIBLE

        if (selectedItemId == R.id.placeNavigationItem) {
            loadFragment()
        }
    }

    private fun getCurrentLocation() {
        val locationClient = LocationServices.getFusedLocationProviderClient(this)

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    updateLocation(location)
                    locationClient.removeLocationUpdates(this)
                    break
                }
            }
        }

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 10000
            fastestInterval = 5000
        }

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationClient.requestLocationUpdates(locationRequest,
                locationCallback, Looper.getMainLooper())
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Toast.makeText(this, "Немає доступу до геолокації",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateLocation(location: Location) {
        val wkt = convertToWKT(location.latitude, location.longitude)
        user.setProperty("myLocation", wkt)

        Backendless.UserService.update(user, object : AsyncCallback<BackendlessUser> {
            override fun handleResponse(response: BackendlessUser?) {
            }

            override fun handleFault(fault: BackendlessFault?) {
                Log.e("ERROR LOCATION UPDATE", fault.toString())

                Toast.makeText(this@ProfileActivity,
                    "Помилка при оновленні місцеположення", Toast.LENGTH_SHORT).show()
            }
        })

        profileGeolocation.text = user.getProperty("myLocation").toString()
    }

    /*private fun convertToGeoJSON(latitude: Double, longitude: Double): String {
        val jsonObject = JSONObject()
        jsonObject.put("type", "Point")
        val coordinates = JSONArray()
        coordinates.put(longitude)
        coordinates.put(latitude)
        jsonObject.put("coordinates", coordinates)
        return jsonObject.toString()
    }*/

    private fun convertToWKT(latitude: Double, longitude: Double): String {
        return "POINT($longitude $latitude)"
    }

    private fun getUserPlaceFromServer() {
        val nickname = Backendless.UserService.CurrentUser().getProperty("nickname")

        val whereClause = "authorNickname = '$nickname'"
        val queryBuilder = DataQueryBuilder.create().setWhereClause(whereClause)

        Backendless.Data.of("Place").find(queryBuilder,
                object : AsyncCallback<List<MutableMap<Any?, Any?>>> {
            override fun handleResponse(response: List<MutableMap<Any?, Any?>>?) {
                response?.let { places ->
                    placeList.clear()
                    for (placeData in places) {
                        val likedByArray = placeData["likedBy"] as? Array<String>
                        val likedByList = likedByArray?.toList() ?: emptyList()

                        val place = Place(
                            objectId = placeData["objectId"] as? String ?: "",
                            description = placeData["description"] as? String ?: "",
                            cathegory = placeData["cathegory"] as? String ?: "",
                            coordinates = (placeData["coordinates"] as? Point)!!,
                            hashtags = placeData["hashtags"] as? String ?: "",
                            created = formatDate(placeData["created"] as? Date),
                            imageUrl = placeData["imageUrl"] as? String?,
                            likeCount = placeData["likeCount"] as? Int ?: 0,
                            authorNickname = placeData["authorNickname"] as? String ?: "",
                            authorId = placeData["authorId"] as? String ?: "",
                            likedBy = likedByList
                        )
                        Log.e("LIKED BY", likedByList.toString())
                        placeList.add(place)
                    }
                }
            }

            override fun handleFault(fault: BackendlessFault?) {
                Log.e("GETTING DATA ERROR", "Error: $fault")
            }
        })
    }

    private fun loadFragment() {
        if(placeList.isEmpty()) {
            supportFragmentManager.beginTransaction().replace(
                R.id.fragment_container,
                EmptyPlaceFragment()
            ).commit()
        }
        else {
            val placeFragment = PlaceFragment()
            placeFragment.setPlaceList(placeList)

            supportFragmentManager.beginTransaction().replace(
                R.id.fragment_container,
                placeFragment
            ).commit()
        }
    }

    private fun formatDate(date: Date?): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return date?.let { dateFormat.format(it) } ?: ""
    }
}