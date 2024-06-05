package com.example.loginapp.Fragments.Friend

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.example.loginapp.Adapters.RequestAdapter
import com.example.loginapp.Listeners.RequestClickListener
import com.example.loginapp.Models.User
import com.example.loginapp.Models.UserListManager
import com.example.loginapp.R

class RequestFragment : Fragment(), RequestClickListener {
    private lateinit var recyclerViewRequests: RecyclerView
    private lateinit var requestAdapter: RequestAdapter
    private var userList: List<User> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_request, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewRequests = view.findViewById(R.id.requestRecycler)
        recyclerViewRequests.layoutManager = LinearLayoutManager(requireContext())

        userList = UserListManager.getUserList()!!
        requestAdapter = RequestAdapter(requireContext(), userList, this)
        recyclerViewRequests.adapter = requestAdapter
    }

    override fun onAcceptClick(user: User) {
        handleFriendRequest(user, isAccept = true)
    }

    override fun onDeclineClick(user: User) {
        handleFriendRequest(user, isAccept = false)
    }

    private fun handleFriendRequest(user: User, isAccept: Boolean) {
        val currentUser = Backendless.UserService.CurrentUser()

        val currentUserClause = "objectId = '${currentUser.objectId}'"
        val friendUserClause = "objectId = '${user.objectId}'"

        val currentUserChanges = HashMap<String, Any>()
        val friendUserChanges = HashMap<String, Any>()

        val updatedFriendRequests = user.friendRequests.filter { it != currentUser.objectId }
        currentUserChanges["friendRequests"] = updatedFriendRequests

        if (isAccept) {
            val friendsWithArray = currentUser.getProperty("friendsWith") as? Array<String>
            val friendsWithList = friendsWithArray?.toList() ?: emptyList()
            val updatedFriendsWithList = friendsWithList + user.objectId
            val updatedFriendsWithFriendUser = (user.friendsWith + currentUser.objectId).distinct()

            currentUserChanges["friendsWith"] = updatedFriendsWithList
            friendUserChanges["friendsWith"] = updatedFriendsWithFriendUser
        }

        val friendUserUpdatedRequests = user.friendRequests.filter { it != currentUser.objectId }
        friendUserChanges["friendRequests"] = friendUserUpdatedRequests

        Backendless.Data.of("Users").update(currentUserClause, currentUserChanges,
            object : AsyncCallback<Int> {
            @SuppressLint("NotifyDataSetChanged")
            override fun handleResponse(response: Int?) {
                Log.d("UPDATE_SUCCESS", "Current user updated successfully")
                userList = userList.filter { it.objectId != user.objectId }
                requestAdapter.notifyDataSetChanged()
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
