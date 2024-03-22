package com.example.loginapp

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.Manifest
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import com.backendless.Backendless
import com.backendless.BackendlessUser
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.android.gms.location.*
import org.json.JSONArray
import org.json.JSONObject

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
    private lateinit var postIndicator: View
    private lateinit var likeIndicator: View

    private val LOCATION_REQUEST_CODE = 1869
    private lateinit var locationCallback: LocationCallback

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        user = Backendless.UserService.CurrentUser() ?: BackendlessUser()

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

        postIndicator = findViewById(R.id.postIndicator)
        likeIndicator = findViewById(R.id.likeIndicator)

        isPostSelected(true)

        getUserInfo()

        postItem.setOnClickListener {
            isPostSelected(true)
        }

        likeItem.setOnClickListener {
            isPostSelected(false)
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

    private fun isPostSelected(isPostsSelected: Boolean) {
        if(isPostsSelected) {
            postIndicator.visibility = View.VISIBLE
            likeIndicator.visibility = View.INVISIBLE
        } else {
            postIndicator.visibility = View.INVISIBLE
            likeIndicator.visibility = View.VISIBLE
        }
    }

    private fun getCurrentLocation() {
        val locationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    updateLocation(location)
                    locationClient.removeLocationUpdates(locationCallback)
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
}