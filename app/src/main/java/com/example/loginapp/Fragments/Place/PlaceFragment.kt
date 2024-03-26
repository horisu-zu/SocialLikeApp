package com.example.loginapp.Fragments.Place

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
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

class PlaceFragment : Fragment(), PlaceClickListener {
    private lateinit var placeRecyclerView: RecyclerView
    private lateinit var placeAdapter: PlaceAdapter
    private val placeList: MutableList<Place> = ArrayList()
    private lateinit var originalCathegories: MutableList<String>
    private val LIKE_PREFERENCES = "like_preferences"
    private val currentUserId: String = Backendless.UserService.CurrentUser().userId

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_place, container, false)
        placeRecyclerView = view.findViewById(R.id.placesRecycler)

        originalCathegories = placeList.map { it.cathegory }.toMutableList()

        placeAdapter = PlaceAdapter(requireContext(), placeList, this, currentUserId)
        placeRecyclerView.adapter = placeAdapter
        placeRecyclerView.layoutManager = LinearLayoutManager(context)

        return view
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

    /*private fun loadLikeState(placeId: String): Boolean {
        val sharedPreferences = requireContext().getSharedPreferences(LIKE_PREFERENCES,
            Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(placeId, false)
    }*/

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

    fun setPlaceList(places: List<Place>) {
        placeList.clear()
        placeList.addAll(places)
    }
}