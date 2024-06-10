package com.example.loginapp.Fragments.Friend

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.persistence.Point
import com.example.loginapp.Adapters.FriendsAdapter
import com.example.loginapp.Listeners.FriendClickListener
import com.example.loginapp.Models.User
import com.example.loginapp.Models.UserListManager
import com.example.loginapp.ProfileActivity
import com.example.loginapp.R
import com.google.android.material.textfield.TextInputEditText

class FriendsFragment : Fragment(), FriendClickListener {
    private lateinit var recyclerViewFriends: RecyclerView
    private lateinit var friendsAdapter: FriendsAdapter
    private lateinit var searchEditText: TextInputEditText
    private var userList: List<User> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_friends, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewFriends = view.findViewById(R.id.friendsRecycler)
        recyclerViewFriends.layoutManager = LinearLayoutManager(requireContext())

        searchEditText = view.findViewById(R.id.searchEditText)

        userList = UserListManager.getUserList() ?: listOf()
        friendsAdapter = FriendsAdapter(requireContext(), userList, this)
        recyclerViewFriends.adapter = friendsAdapter

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val radius = s.toString().toIntOrNull() ?: 0
                filterUsersByLocation(Location(""), radius)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterUsersByLocation(currentLocation: Location, radius: Int) {
        val radiusInKm = radius * 1000
        val filteredList = userList.filter { user ->
            val coordinates = extractCoordinates(user.myLocation)
            if (coordinates != null && user.geolocationEnabled) {
                val userLocation = Location("").apply {
                    latitude = coordinates.first
                    longitude = coordinates.second
                }
                val distance = currentLocation.distanceTo(userLocation)
                distance <= radiusInKm
            } else {
                false
            }
        }
        friendsAdapter.setData(filteredList)
    }

    private fun extractCoordinates(location: Any?): Pair<Double, Double>? {
        return when (location) {
            is Point -> Pair(location.latitude, location.longitude)
            is String -> {
                try {
                    val point = location.removePrefix("POINT (")
                        .removeSuffix(")").split(" ")
                    val longitude = point[0].toDouble()
                    val latitude = point[1].toDouble()
                    Pair(latitude, longitude)
                } catch (e: Exception) {
                    Log.e("WKT_PARSE_ERROR", "Error parsing WKT: $e")
                    null
                }
            }
            else -> null
        }
    }

    override fun onUserClick(user: User) {
        val intent = Intent(requireContext(), ProfileActivity::class.java)
        intent.putExtra("userId", user.objectId)
        startActivity(intent)
    }

    override fun onPopClick(user: User, imageView: ImageView) {
        val popup = PopupMenu(requireContext(), imageView)
        popup.menuInflater.inflate(R.menu.friend_popup, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_send_message -> {
                    val intent = Intent(requireContext(), ProfileActivity::class.java)
                    intent.putExtra("userId", user.objectId)
                    startActivity(intent)
                    true
                }
                R.id.action_show_on_map -> {
                    if(user.geolocationEnabled) {
                        val latitude = user.myLocation?.latitude
                        val longitude = user.myLocation?.longitude
                        val uri = Uri.parse("geo:$latitude,$longitude")
                        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        startActivity(mapIntent)
                    } else {
                        Toast.makeText(requireContext(),
                            "User hasn't provided access to geolocation",
                            Toast.LENGTH_SHORT).show()
                    }

                    true
                }
                R.id.action_remove_friend -> {
                    showRemoveFriendDialog(user)
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private fun showRemoveFriendDialog(user: User) {
        AlertDialog.Builder(requireContext())
            .setTitle("Remove Friend")
            .setMessage("Are you sure you want to remove ${user.name} from your friends?")
            .setPositiveButton("Yes") { dialog, which ->
                removeFriend(user)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun removeFriend(user: User) {
        val currentUser = Backendless.UserService.CurrentUser()
        val currentUserClause = "objectId = '${currentUser.objectId}'"
        val friendUserClause = "objectId = '${user.objectId}'"

        val currentUserChanges = HashMap<String, Any>()
        val friendUserChanges = HashMap<String, Any>()

        val friendsWithArray = currentUser.getProperty("friendsWith") as? Array<String>
        val friendsWithList = friendsWithArray?.toList() ?: emptyList()
        val updatedFriendsWithList = friendsWithList - user.objectId
        currentUserChanges["friendsWith"] = updatedFriendsWithList

        val updatedFriendsWithFriendUser = user.friendsWith.filter { it != currentUser.objectId }
        friendUserChanges["friendsWith"] = updatedFriendsWithFriendUser

        Backendless.Data.of("Users").update(currentUserClause, currentUserChanges,
            object : AsyncCallback<Int> {
                @SuppressLint("NotifyDataSetChanged")
                override fun handleResponse(response: Int?) {
                    Log.d("UPDATE_SUCCESS", "Current user updated successfully")
                    userList = userList.filter { it.objectId != user.objectId }
                    friendsAdapter.notifyDataSetChanged()
                }

                override fun handleFault(fault: BackendlessFault?) {
                    Log.e("CURRENT_ERROR", "Error: $fault")
                }
            })

        Backendless.Data.of("Users").update(friendUserClause, friendUserChanges,
            object : AsyncCallback<Int> {
                override fun handleResponse(response: Int?) {
                    Log.d("UPDATE_SUCCESS", "Friend user updated successfully")
                }

                override fun handleFault(fault: BackendlessFault?) {
                    Log.e("FRIEND_ERROR", "Error: $fault")
                }
            })
    }
}
