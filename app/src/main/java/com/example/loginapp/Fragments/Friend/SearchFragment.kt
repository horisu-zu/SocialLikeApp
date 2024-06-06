package com.example.loginapp.Fragments.Friend

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.example.loginapp.Adapters.SearchAdapter
import com.example.loginapp.Listeners.SearchClickListener
import com.example.loginapp.Models.CurrentUserItems
import com.example.loginapp.Models.User
import com.example.loginapp.Models.UserListManager
import com.example.loginapp.ProfileActivity
import com.example.loginapp.R
import com.google.android.material.textfield.TextInputEditText

class SearchFragment : Fragment(), SearchClickListener {
    private lateinit var searchRecycler: RecyclerView
    private lateinit var searchAdapter: SearchAdapter
    private lateinit var searchEditText: TextInputEditText
    private var userList: List<User> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchRecycler = view.findViewById(R.id.searchRecycler)
        searchRecycler.layoutManager = LinearLayoutManager(requireContext())

        searchEditText = view.findViewById(R.id.searchEditText)

        userList = UserListManager.getUserList() ?: listOf()
        userList.forEach { user ->
            Log.e("SEARCH LIST", "ObjectId: ${user.objectId}, Nickname: ${user.nickname}")
        }

        searchAdapter = SearchAdapter(requireContext(), userList, this)
        searchRecycler.adapter = searchAdapter

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterUsers(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterUsers(query: String) {
        val filteredList = userList.filter {
            it.name.contains(query, true) || it.nickname.contains(query, true)
        }
        searchAdapter.setData(filteredList)
    }

    override fun onUserClick(user: User) {
        val intent = Intent(requireContext(), ProfileActivity::class.java)
        intent.putExtra("userId", user.objectId)
        startActivity(intent)
    }

    override fun onSendRequestClick(user: User) {
        user.friendRequests = user.friendRequests + CurrentUserItems.currentUserId!!
        updateFriendRequests(user)
    }

    override fun onCancelRequestClick(user: User) {
        user.friendRequests = user.friendRequests - CurrentUserItems.currentUserId!!
        updateFriendRequests(user)
    }

    private fun updateFriendRequests(user: User) {
        val currentUserChanges = mapOf("friendRequests" to user.friendRequests)
        val friendUserClause = "objectId = '${user.objectId}'"

        Backendless.Data.of("Users").update(friendUserClause, currentUserChanges,
            object : AsyncCallback<Int> {
                @SuppressLint("NotifyDataSetChanged")
                override fun handleResponse(response: Int?) {
                    Log.d("UPDATE_SUCCESS",
                        "Friend request updated successfully for user: ${user.objectId}")
                    searchAdapter.notifyDataSetChanged()
                }

                override fun handleFault(fault: BackendlessFault?) {
                    Log.e("CURRENT_ERROR", "Error updating friend request: $fault")
                }
            })
    }
}
