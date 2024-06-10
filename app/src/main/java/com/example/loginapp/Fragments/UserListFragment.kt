package com.example.loginapp.Fragments

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
import com.backendless.persistence.DataQueryBuilder
import com.backendless.persistence.Point
import com.example.loginapp.Adapters.UserAdapter
import com.example.loginapp.Listeners.UserClickListener
import com.example.loginapp.Models.User
import com.example.loginapp.R

class UserListFragment : Fragment(), UserClickListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var userListAdapter: UserAdapter
    private val userList: MutableList<User> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_list, container, false)
        recyclerView = view.findViewById(R.id.usersRecycler)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val subsType = arguments?.getString("subsType")
        val id = requireArguments().getString("currentId").toString()

        Log.e("Type", subsType.toString())

        when (subsType) {
            "Subscribers" -> {
                initUserList(id, UserListType.SUBSCRIBERS)
            }

            "Subscriptions" -> {
                initUserList(id, UserListType.SUBSCRIPTIONS)
            }

            "AllUsers" -> {
                initUserList(id, UserListType.ALL_USERS)
            }
        }

        setupRecyclerView()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initUserList(userId: String, userListType: UserListType) {
        getUsers(userId, userListType) { users ->
            Log.d("UserList", users.toString())

            userList.addAll(users)
            userListAdapter.notifyDataSetChanged()
        }
    }

    private fun setupRecyclerView() {
        userListAdapter = UserAdapter(requireContext(), userList, this)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = userListAdapter
        }
    }

    private fun getUsers(userId: String, userListType: UserListType, callback: (List<User>) -> Unit) {
        when (userListType) {
            UserListType.ALL_USERS -> {
                Backendless.Data.of("Users").find(
                        object : AsyncCallback<List<MutableMap<Any?, Any?>>> {
                    override fun handleResponse(response: List<MutableMap<Any?, Any?>>?) {
                        val userList = mutableListOf<User>()
                        response?.let { userMapList ->
                            for (userMap in userMapList) {
                                val user = getUserFromMap(userMap)
                                if (user.objectId != userId) {
                                    userList.add(user)
                                }
                            }
                        }
                        callback(userList)
                    }

                    override fun handleFault(fault: BackendlessFault?) {
                        Log.e("FAULT", fault.toString())
                    }
                })
            }

            UserListType.SUBSCRIPTIONS -> {
                fetchUsersBySubscription(userId, "subscribedOn", callback)
            }

            UserListType.SUBSCRIBERS -> {
                fetchUsersBySubscription(userId, "subscribedBy", callback)
            }
        }
    }

    private fun getCurrentUser(): String {
        return Backendless.UserService.CurrentUser().objectId
    }

    override fun onUserClick(user: User) {
    }

    private fun fetchUsersBySubscription(userObjectId: String, subscriptionField: String,
                                         callback: (List<User>) -> Unit) {
        Backendless.Data.of("Users").findById(
            userObjectId,
            object : AsyncCallback<MutableMap<Any?, Any?>> {
                override fun handleResponse(response: MutableMap<Any?, Any?>?) {
                    response.let { user ->
                        val subsArray = user?.get(subscriptionField) as? Array<String>
                        val subsList = subsArray?.toList() ?: emptyList()

                        Log.e("SUBSCR", subsList.toString())

                        val whereClause = "objectId IN " +
                                "('${subsList.joinToString("', '")}')"
                        val queryBuilder = DataQueryBuilder.create().setWhereClause(whereClause)
                        Backendless.Data.of("Users").find(queryBuilder,
                            object : AsyncCallback<List<MutableMap<Any?, Any?>>> {
                                override fun handleResponse(response: List<MutableMap<Any?, Any?>>?) {
                                    val userList = response?.mapNotNull { getUserFromMap(it) }
                                    callback(userList ?: emptyList())
                                }

                                override fun handleFault(fault: BackendlessFault?) {
                                    Log.e("FAULT", fault.toString())
                                    callback(emptyList())
                                }
                            })
                    }
                }

                override fun handleFault(fault: BackendlessFault?) {
                    Log.e("FAULT", fault.toString())
                    callback(emptyList())
                }
            })
    }

    private fun getUserFromMap(userMap: Map<Any?, Any?>): User {
        return User(
            objectId = userMap["objectId"] as? String ?: "",
            email = userMap["email"] as? String ?: "",
            password = userMap["password"] as? String ?: "",
            name = userMap["name"] as? String ?: "",
            nickname = userMap["nickname"] as? String ?: "",
            baseNickname = userMap["baseNickname"] as? String ?: "",
            age = userMap["age"] as? Int ?: 0,
            gender = userMap["gender"] as? String ?: "",
            country = userMap["country"] as? String ?: "",
            subscribersCount = userMap["subscribersCount"] as? Int ?: 0,
            subscriptionsCount = userMap["subscriptionsCount"] as? Int ?: 0,
            avatarPath = userMap["avatarPath"] as? String ?: "",
            subscribedBy = (userMap["subscribedBy"] as? List<String>) ?: listOf(),
            subscribedOn = (userMap["subscribedOn"] as? List<String>) ?: listOf(),
            friendsWith = (userMap["friendsWith"] as? List<String>) ?: listOf(),
            friendRequests = (userMap["friendRequests"] as? List<String>) ?: listOf(),
            geolocationEnabled = userMap["geolocationEnabled"].toString().toBoolean(),
            myLocation = userMap["myLocation"] as Point?
        )
    }

    enum class UserListType {
        ALL_USERS,
        SUBSCRIBERS,
        SUBSCRIPTIONS
    }
}
