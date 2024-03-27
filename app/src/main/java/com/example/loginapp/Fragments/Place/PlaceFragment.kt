package com.example.loginapp.Fragments.Place

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.example.loginapp.Adapters.PlaceAdapter
import com.example.loginapp.Listeners.PlaceClickListener
import com.example.loginapp.Models.Place
import com.example.loginapp.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class PlaceFragment : Fragment(), PlaceClickListener {
    private lateinit var placeSearch: TextInputEditText
    private lateinit var searchButton: ImageButton
    private lateinit var searchLayout: LinearLayout
    private lateinit var searchCategory: TextInputLayout

    private lateinit var placeRecyclerView: RecyclerView
    private lateinit var placeAdapter: PlaceAdapter
    private var selectedSearchItem: String = ""
    private val placeList: MutableList<Place> = ArrayList()
    private lateinit var originalCathegories: MutableList<String>
    private val LIKE_PREFERENCES = "like_preferences"
    private var isSearchPressed: Boolean = false
    private lateinit var searchItems: List<String>
    private val currentUserId: String = Backendless.UserService.CurrentUser().userId

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_place, container, false)

        placeSearch = view.findViewById(R.id.editTextSearch)
        searchButton = view.findViewById(R.id.searchButton)
        searchLayout = view.findViewById<LinearLayout>(R.id.searchLayout)
        searchCategory = view.findViewById<TextInputLayout>(R.id.cathegoryLayout)

        placeRecyclerView = view.findViewById(R.id.placesRecycler)

        originalCathegories = placeList.map { it.cathegory }.toMutableList()

        searchItems = getSearchTypes()
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, searchItems)
        (searchCategory.editText as? AutoCompleteTextView)?.setAdapter(adapter)

        (searchCategory.editText as? AutoCompleteTextView)?.setOnItemClickListener { _, _,
                                                                                     position, _ ->
            selectedSearchItem = searchItems[position]
            Log.e("SEARCH ITEM", selectedSearchItem)
        }

        placeSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterPlaces((searchCategory.editText as? AutoCompleteTextView)?.text.toString())
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        searchButton.setOnClickListener {
            search()
        }

        placeAdapter = PlaceAdapter(requireContext(), placeList, this, currentUserId)
        placeRecyclerView.adapter = placeAdapter
        placeRecyclerView.layoutManager = LinearLayoutManager(context)

        for (place in placeList) {
            loadLikeState(place.objectId)
        }

        return view
    }

    private fun search() {
        if(isSearchPressed) {
            searchLayout.visibility = View.VISIBLE
            filterPlaces(selectedSearchItem)
        } else {
            searchLayout.visibility = View.GONE
        }

        isSearchPressed = !isSearchPressed
    }

    private fun filterPlaces(selectedItem: String) {
        val query = placeSearch.text.toString().toLowerCase(Locale.getDefault())

        val filteredPlaces: List<Place> = when (selectedItem) {
            "Опис" -> placeList.filter { it.description.toLowerCase(Locale.getDefault()).contains(query) }
            "Категорія" -> placeList.filter { it.cathegory.toLowerCase(Locale.getDefault()).contains(query) }
            "Тег" -> placeList.filter { it.hashtags.toLowerCase(Locale.getDefault()).contains(query) }
            "Радіус" -> {
                val userLocation = getUserLocation(requireContext())
                val radius = query.toDoubleOrNull() ?: return
                placeList.filter { place ->
                    val placeLocation = Location("").apply {
                        latitude = place.coordinates.latitude
                        longitude = place.coordinates.longitude
                    }
                    val distance = calculateDistance(userLocation!!, placeLocation)
                    distance <= radius
                }
            }
            else -> placeList
        }

        placeAdapter.setPlaces(filteredPlaces)
    }

    private fun getUserLocation(context: Context): Location? {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            return null
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val lastKnownLocationGPS =
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        val lastKnownLocationNetwork =
            locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        return if (lastKnownLocationGPS != null &&
                lastKnownLocationGPS.time > System.currentTimeMillis() - 2 * 60 * 1000) {
            lastKnownLocationGPS
        } else {
            lastKnownLocationNetwork
        }
    }

    private fun calculateDistance(location1: Location, location2: Location): Double {
        val earthRadius = 6371

        val lat1 = Math.toRadians(location1.latitude)
        val lon1 = Math.toRadians(location1.longitude)
        val lat2 = Math.toRadians(location2.latitude)
        val lon2 = Math.toRadians(location2.longitude)

        val dlon = lon2 - lon1
        val dlat = lat2 - lat1

        val a = sin(dlat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dlon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }

    override fun onLikeClick(place: Place) {
        val index = placeList.indexOf(place)
        Log.e("INDEX", index.toString())
        val selectedPlace = placeList[index]
        Log.e("SELECTED ID", selectedPlace.objectId)
        val userId = Backendless.UserService.CurrentUser().objectId

        val isLikedByCurrentUser: Boolean = selectedPlace.likedBy.contains(userId)
        Log.e("LIKED BY", selectedPlace.likedBy.toString())
        Log.e("ISLIKED", isLikedByCurrentUser.toString())

        if (isLikedByCurrentUser) {
            selectedPlace.likeCount--
            selectedPlace.likedBy = selectedPlace.likedBy.filter { it != userId }
        } else {
            selectedPlace.likeCount++
            selectedPlace.likedBy = selectedPlace.likedBy + userId
        }

        placeAdapter.notifyItemChanged(index)

        updateBackendlessLike(selectedPlace.objectId, userId, !isLikedByCurrentUser)

        saveLikeState(selectedPlace.objectId, isLikedByCurrentUser)
    }

    override fun onBookmarkClick(place: Place) {
    }

    override fun onCategoryClick(place: Place) {
        val index = placeList.indexOf(place)
        val selectedPlace = placeList[index]

        val newCathegory = if (!isCoordinates(selectedPlace.cathegory)) {
            selectedPlace.coordinates.toString()
        } else {
            originalCathegories[index]
        }

        selectedPlace.cathegory = newCathegory

        placeList[index] = selectedPlace
        placeAdapter.notifyItemChanged(index)
    }

    private fun isCoordinates(cathegory: String): Boolean {
        return cathegory.contains(".")
    }

    override fun onPopClick(place: Place, imageView: ImageView) {
        val popup = PopupMenu(requireContext(), imageView)
        popup.menuInflater.inflate(R.menu.place_popup, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_edit -> {
                    true
                }
                R.id.action_delete -> {
                    deleteDialog(place.objectId)
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private fun deleteDialog(objectId: String) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle("Видалення")
        alertDialogBuilder.setMessage("Ви впевнені у видаленні?")
        alertDialogBuilder.setPositiveButton("Так") { _, _ ->
            deletePlace(objectId)
        }
        alertDialogBuilder.setNegativeButton("Відмінити") { dialog, _ ->
            dialog.dismiss()
        }
        alertDialogBuilder.create().show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun deletePlace(objectId: String) {
        val whereClause: String = "objectId = '$objectId'"

        Backendless.Data.of("Place").remove(whereClause, object : AsyncCallback<Int> {
            override fun handleResponse(response: Int?) {
                Log.d("Delete", "'Місце' успішно видалено")
            }

            override fun handleFault(fault: BackendlessFault?) {
                Log.e("Delete", "Помилка при видаленні: ${fault?.message}")
            }
        })
    }

    private fun saveLikeState(placeId: String, isLiked: Boolean) {
        val sharedPreferences = requireContext().getSharedPreferences(LIKE_PREFERENCES,
            Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(placeId, isLiked)
        editor.apply()
    }

    private fun loadLikeState(placeId: String): Boolean {
        val sharedPreferences = requireContext().getSharedPreferences(LIKE_PREFERENCES,
            Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(placeId, false)
    }

    private fun updateBackendlessLike(objectId: String, userId: String, update: Boolean) {
        Backendless.Data.of("Place").findById(objectId,
            object : AsyncCallback<Map<Any?, Any?>> {
                override fun handleResponse(response: Map<Any?, Any?>?) {
                    response?.let { placeData ->
                        val whereClause = "objectId = '$objectId'"
                        val changes = HashMap<String, Any>()

                        val likedByArray = placeData["likedBy"] as? Array<String>
                        val likedByList = likedByArray?.toList() ?: emptyList()

                        val updatedLikedByList = if (update) {
                            if (userId !in likedByList) likedByList + userId
                            else likedByList
                        } else {
                            likedByList - userId
                        }
                        changes["likedBy"] = updatedLikedByList

                        val itemCount = updatedLikedByList.size
                        changes["likeCount"] = itemCount

                        Backendless.Data.of("Place").update(whereClause, changes,
                            object : AsyncCallback<Int> {
                                override fun handleResponse(response: Int) {
                                    val updatedPlaceIndex = placeList.indexOfFirst {
                                        it.objectId == objectId
                                    }
                                    if (updatedPlaceIndex != -1) {
                                        placeList[updatedPlaceIndex].likeCount = itemCount
                                        placeList[updatedPlaceIndex].likedBy = updatedLikedByList
                                        placeAdapter.notifyItemChanged(updatedPlaceIndex)
                                    }
                                }

                                override fun handleFault(fault: BackendlessFault?) {
                                    Log.e("ERROR", "Error: $fault")
                                }
                            })
                    }
                }

                override fun handleFault(fault: BackendlessFault?) {
                }
            })
    }

    private fun getSearchTypes(): List<String> {
        return listOf(
            "Опис",
            "Категорія",
            "Тег",
            "Радіус"
        )
    }

    fun setPlaceList(places: List<Place>) {
        placeList.clear()
        placeList.addAll(places)
    }
}