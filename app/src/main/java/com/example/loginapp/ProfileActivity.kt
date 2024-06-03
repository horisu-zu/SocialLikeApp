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
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
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
import com.google.android.material.card.MaterialCardView
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileActivity : AppCompatActivity() {
    private val currentUser = Backendless.UserService.CurrentUser()
    private lateinit var user: BackendlessUser
    private lateinit var avatarPath: String

    private lateinit var profileAvatar: ImageView
    private lateinit var profileName: TextView
    private lateinit var profileNickname: TextView
    private lateinit var profileSubscriptionsCount: TextView
    private lateinit var profileSubscribersCount: TextView
    private lateinit var profileFriendsCount: TextView
    private lateinit var profileSubscribersLayout: LinearLayout
    private lateinit var profileSubscriptionsLayout: LinearLayout
    private lateinit var profileCreationDate: TextView
    private lateinit var profileGeolocation: TextView
    private lateinit var profileSubscribeText: TextView

    private lateinit var backCard: CardView
    private lateinit var configurationCard: CardView
    private lateinit var subscribeCard: MaterialCardView
    private lateinit var friendAddCard: MaterialCardView
    private lateinit var friendImage: ImageView
    private lateinit var postItem: TextView
    private lateinit var likeItem: TextView
    private lateinit var placeItem: TextView
    private lateinit var postIndicator: View
    private lateinit var likeIndicator: View
    private lateinit var placeIndicator: View

    private var userId: String? = null
    private var isCurrentUser = false

    private val LOCATION_REQUEST_CODE = 1869

    private val placeList: MutableList<Place> = ArrayList()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        userId = intent.getStringExtra("userId")

        profileName = findViewById(R.id.nameTextView)
        profileNickname = findViewById(R.id.nicknameTextView)
        profileSubscribersCount = findViewById(R.id.subscribersCount)
        profileSubscriptionsCount = findViewById(R.id.subscriptionsCount)
        profileFriendsCount = findViewById(R.id.friendsCount)
        profileSubscribersLayout = findViewById(R.id.subscribersLayout)
        profileSubscriptionsLayout = findViewById(R.id.subscriptionsLayout)
        profileAvatar = findViewById(R.id.avatarImageView)
        profileGeolocation = findViewById(R.id.locationView)
        profileCreationDate = findViewById(R.id.createDate)
        profileSubscribeText = findViewById(R.id.subscribeText)

        backCard = findViewById(R.id.backCard)
        subscribeCard = findViewById(R.id.subscribeCard)
        friendAddCard = findViewById(R.id.friendAddCard)
        friendImage = findViewById(R.id.friendImage)
        configurationCard = findViewById(R.id.configurationCard)

        configurationCard.visibility = View.GONE

        if(userId == currentUser.objectId || userId == null) {
            subscribeCard.visibility = View.GONE
            friendAddCard.visibility = View.GONE
            configurationCard.visibility = View.VISIBLE
        }

        postItem = findViewById(R.id.postNavigationItem)
        likeItem = findViewById(R.id.likeNavigationItem)
        placeItem = findViewById(R.id.placeNavigationItem)

        postIndicator = findViewById(R.id.postIndicator)
        likeIndicator = findViewById(R.id.likeIndicator)
        placeIndicator = findViewById(R.id.placeIndicator)

        selectNavigationItem(R.id.postNavigationItem)

        getUser()

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

        subscribeCard.setOnClickListener {
            subscribe()
        }

        friendAddCard.setOnClickListener {

        }

        profileSubscribersLayout.setOnClickListener {
            val intent = Intent(this@ProfileActivity, SubsUsersActivity::class.java)
            intent.putExtra("subsType", "Subscribers")
            intent.putExtra("currentId", user.objectId)
            startActivity(intent)
        }

        profileSubscriptionsLayout.setOnClickListener {
            val intent = Intent(this@ProfileActivity, SubsUsersActivity::class.java)
            intent.putExtra("subsType", "Subscriptions")
            intent.putExtra("currentId", user.objectId)
            startActivity(intent)
        }

        configurationCard.setOnClickListener {
            val intent = Intent(this, ConfigurationActivity::class.java)
            startActivity(intent)
        }

        if(isCurrentUser) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && user.getProperty("geolocationEnabled") == true) {
                getCurrentLocation()
            } else {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
            }
        }
    }

    private fun getUser() {
        if(userId != null) {
            Log.e("USERID", userId!!)
            Backendless.UserService.findById(userId, object : AsyncCallback<BackendlessUser> {
                override fun handleResponse(foundUser: BackendlessUser) {
                    user = foundUser
                    isCurrentUser = false

                    getUserPlaceFromServer()
                    getUserInfo()
                }

                override fun handleFault(fault: BackendlessFault) {
                    Log.e("BackendlessError", "Failed to find user: ${fault.message}")
                }
            })
        } else {
            Log.e("USER", "Current User")
            user = currentUser
            isCurrentUser = true

            getUserPlaceFromServer()
            getUserInfo()
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
        profileFriendsCount.text = user.getProperty("friendsCount").toString()
        avatarPath = user.getProperty("avatarPath").toString()
        profileCreationDate.text = formattedDate
        profileGeolocation.text = user.getProperty("myLocation").toString()

        Picasso.get().load(avatarPath).into(profileAvatar)

        ifIsSubscribed(currentUser)
        isFriendsWith(currentUser)
    }

    private fun ifIsSubscribed(currentUser: BackendlessUser) {
        if(isSubscribed(currentUser.objectId, user)) {
            Log.e("CARD SUBSCRIBED", isSubscribed(currentUser.objectId, user).toString())
            subscribeCard.strokeColor = getColor(R.color.red)
            profileSubscribeText.text = "Відписатися"
        } else {
            subscribeCard.strokeColor = getColor(R.color.white)
            profileSubscribeText.text = "Підписатися"
        }
    }

    private fun isFriendsWith(currentUser: BackendlessUser) {
        val isFriend = isFriendsWith(currentUser.objectId, user)

        val drawableRes = if (isFriend) {
            Log.e("CARD SUBSCRIBED", isSubscribed(currentUser.objectId, user).toString())
            friendAddCard.strokeColor = getColor(R.color.red)
            AppCompatResources.getDrawable(applicationContext, R.drawable.remove_friend)
        } else {
            friendAddCard.strokeColor = getColor(R.color.white)
            AppCompatResources.getDrawable(applicationContext, R.drawable.add_friend)
        }

        friendImage.setImageDrawable(drawableRes)
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

    private fun subscribe() {
        val subscribeUser = user

        val currentClause = "objectId = '${currentUser.objectId}'"
        val subsClause = "objectId = '${subscribeUser.objectId}'"

        val currentChanges = HashMap<String, Any>()
        val subsChanges = HashMap<String, Any>()

        Log.e("CURRENT", currentUser.objectId)
        Log.e("SUBS", subscribeUser.objectId)

        val subscribedOnArray = subscribeUser.getProperty("subscribedOn") as? Array<String>
        var subscribedOnList = subscribedOnArray?.toList() ?: emptyList()

        val subscribedByArray = currentUser.getProperty("subscribedBy") as? Array<String>
        var subscribedByList = subscribedByArray?.toList() ?: emptyList()

        if (isSubscribed(currentUser.objectId, subscribeUser)) {
            subscribedByList = subscribedByList - currentUser.objectId
            subscribedOnList = subscribedOnList - subscribeUser.objectId
        } else {
            subscribedOnList = subscribedOnList + subscribeUser.objectId
            subscribedByList = subscribedByList + currentUser.objectId
        }

        currentChanges["subscribedOn"] = subscribedOnList
        currentChanges["subscriptionsCount"] = subscribedOnList.size

        Log.e("CURRENT CHANGES", currentChanges.toString())

        subsChanges["subscribedBy"] = subscribedByList
        subsChanges["subscribersCount"] = subscribedByList.size

        /*currentUser.setProperty("subscribedOn", subscribedOnList)
        currentUser.setProperty("subscriptionsCount", subscribedOnList.size)

        subscribeUser.setProperty("subscribedBy", subscribedByList)
        subscribeUser.setProperty("subscribersCount", subscribedByList.size)*/

        Backendless.Data.of("Users").update(currentClause, currentChanges,
                object : AsyncCallback<Int> {
            override fun handleResponse(response: Int?) {
            }

            override fun handleFault(fault: BackendlessFault?) {
                Log.e("CURRENT_ERROR", "Error: $fault")
            }
        })

        Backendless.Data.of("Users").update(subsClause, subsChanges,
                object : AsyncCallback<Int> {
            override fun handleResponse(response: Int?) {
                profileSubscriptionsCount.text = user.getProperty("subscriptionsCount").toString()
                profileSubscribersCount.text = user.getProperty("subscribersCount").toString()
            }

            override fun handleFault(fault: BackendlessFault?) {
                Log.e("ERROR", "Error: $fault")
            }
        })
    }

    /*private fun addOrRemoveFriend() {
        val friendUser = user

        val currentClause = "objectId = '${currentUser.objectId}'"
        val friendClause = "objectId = '${friendUser.objectId}'"

        val currentChanges = HashMap<String, Any>()
        val friendChanges = HashMap<String, Any>()

        Log.e("CURRENT", currentUser.objectId)
        Log.e("FRIEND", friendUser.objectId)

        val friendsArray = currentUser.getProperty("friends") as? Array<String>
        var friendsList = friendsArray?.toList() ?: emptyList()

        val friendsUserArray = friendUser.getProperty("friends") as? Array<String>
        var friendsUserList = friendsUserArray?.toList() ?: emptyList()

        if (isFriendsWith(currentUser.objectId, friendUser)) {
            friendsList = friendsList - friendUser.objectId
            friendsUserList = friendsUserList - currentUser.objectId
        } else {
            friendsList = friendsList + friendUser.objectId
            friendsUserList = friendsUserList + currentUser.objectId
        }

        currentChanges["friends"] = friendsList
        currentChanges["friendsCount"] = friendsList.size

        Log.e("CURRENT CHANGES", currentChanges.toString())

        friendChanges["friends"] = friendsUserList
        friendChanges["friendsCount"] = friendsUserList.size

        Backendless.Data.of("Users").update(currentClause, currentChanges,
            object : AsyncCallback<Int> {
                override fun handleResponse(response: Int?) {
                }

                override fun handleFault(fault: BackendlessFault?) {
                    Log.e("CURRENT_ERROR", "Error: $fault")
                }
            })

        Backendless.Data.of("Users").update(friendClause, friendChanges,
            object : AsyncCallback<Int> {
                override fun handleResponse(response: Int?) {
                    profileFriendsCount.text = currentUser.getProperty("friendsCount").toString()
                }

                override fun handleFault(fault: BackendlessFault?) {
                    Log.e("FRIEND_ERROR", "Error: $fault")
                }
            })
    }*/

    private fun addFriend() {
        val friendUser = user

        val friendClause = "objectId = '${friendUser.objectId}'"

        val friendRequestsChanges = HashMap<String, Any>()

        Log.e("CURRENT", currentUser.objectId)
        Log.e("FRIEND", friendUser.objectId)

        val friendRequestsArray = friendUser.getProperty("friendRequests") as? Array<String>
        var friendRequestsList = friendRequestsArray?.toList() ?: emptyList()

        if (!isFriendsWith(currentUser.objectId, friendUser) &&
            !friendRequestsList.contains(currentUser.objectId)) {
            friendRequestsList = friendRequestsList + currentUser.objectId

            friendRequestsChanges["friendRequests"] = friendRequestsList

            Log.e("FRIEND REQUESTS CHANGES", friendRequestsChanges.toString())

            Backendless.Data.of("Users").update(friendClause, friendRequestsChanges,
                object : AsyncCallback<Int> {
                    override fun handleResponse(response: Int?) {
                        Log.i("FRIEND_REQUEST", "Friend request sent successfully")
                    }

                    override fun handleFault(fault: BackendlessFault?) {
                        Log.e("FRIEND_REQUEST_ERROR", "Error: $fault")
                    }
                })
        } else {
            Log.i("FRIEND_REQUEST", "Already friends or request already sent")
        }
    }

    private fun isSubscribed(currentUserObjectId: String, subscribeUser: BackendlessUser): Boolean {
        val subscribedByArray = subscribeUser.getProperty("subscribedBy") as? Array<String>
        val subscribedByList = subscribedByArray?.toList() ?: emptyList()

        val isSubscribed = subscribedByList.contains(currentUserObjectId).also { result ->
            Log.e("LIST", subscribedByList.toString())
            Log.e("CHECK", result.toString())
        }

        return isSubscribed
    }

    private fun isFriendsWith(currentUserObjectId: String, friendUser: BackendlessUser): Boolean {
        val friendsWithArray = friendUser.getProperty("friendsWith") as? Array<String>
        val friendsWithList = friendsWithArray?.toList() ?: emptyList()

        val isFriendsWith = friendsWithList.contains(currentUserObjectId).also { result ->
            Log.e("LIST", friendsWithList.toString())
            Log.e("CHECK", result.toString())
        }

        return isFriendsWith
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
        val objectId = user.getProperty("objectId")

        val whereClause = "ownerId = '$objectId'"
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