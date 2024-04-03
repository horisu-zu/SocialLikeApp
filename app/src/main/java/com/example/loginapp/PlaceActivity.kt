    package com.example.loginapp

    import android.content.Intent
    import android.os.Bundle
    import android.util.Log
    import android.widget.Button
    import androidx.appcompat.app.AppCompatActivity
    import com.backendless.Backendless
    import com.backendless.async.callback.AsyncCallback
    import com.backendless.exceptions.BackendlessFault
    import com.backendless.persistence.Point
    import com.example.loginapp.Fragments.Place.EmptyPlaceFragment
    import com.example.loginapp.Fragments.Place.PlaceFragment
    import com.example.loginapp.Models.Place
    import java.text.SimpleDateFormat
    import java.util.Date
    import java.util.Locale

    class PlaceActivity : AppCompatActivity() {
        private lateinit var addButton: Button
        private val placeList: MutableList<Place> = ArrayList()

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_place)

            addButton = findViewById(R.id.addButton)

            getDataFromServer()

            addButton.setOnClickListener {
                val intent = Intent(this@PlaceActivity, CreatePlaceActivity::class.java)
                startActivity(intent)
            }
        }

        private fun getDataFromServer() {
            Backendless.Data.of("Place").find(object :
                AsyncCallback<List<MutableMap<Any?, Any?>>> {
                override fun handleResponse(response: List<MutableMap<Any?, Any?>>?) {
                    response?.let { places ->
                        placeList.clear()
                        for (placeData in places) {
                            val likedByArray = placeData["likedBy"] as? Array<String>
                            val likedByList = likedByArray?.toList() ?: emptyList()

                            val bookmarkedByArray = placeData["bookmarkedBy"] as? Array<String>
                            val bookmarkedByList = bookmarkedByArray?.toList() ?: emptyList()

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
                                likedBy = likedByList,
                                bookmarkedBy = bookmarkedByList,
                                bookmarkCount = placeData["bookmarkCount"] as? Int ?: 0
                            )
                            Log.e("LIKED BY", likedByList.toString())
                            placeList.add(place)
                        }
                    }
                    updateUI()
                }

                override fun handleFault(fault: BackendlessFault?) {
                    Log.e("GETTING DATA ERROR", "Error: $fault")
                }
            })
        }

        private fun updateUI() {
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