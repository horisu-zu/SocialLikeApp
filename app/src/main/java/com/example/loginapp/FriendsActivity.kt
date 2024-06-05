package com.example.loginapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.persistence.DataQueryBuilder
import com.example.loginapp.Fragments.Friend.FriendsFragment
import com.example.loginapp.Fragments.Friend.RequestFragment
import com.example.loginapp.Models.User
import com.example.loginapp.Models.UserListManager
import com.google.android.material.card.MaterialCardView

class FriendsActivity : AppCompatActivity() {
    private lateinit var friendsTab: MaterialCardView
    private lateinit var requestsTab: MaterialCardView
    private lateinit var backCard: MaterialCardView
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends)

        userId = intent.getStringExtra("userId").toString()

        friendsTab = findViewById(R.id.friendsTab)
        requestsTab = findViewById(R.id.requestsTab)
        backCard = findViewById(R.id.backCard)

        fetchAndLoadFragment(FriendsListType.FRIENDS, friendsTab)

        friendsTab.setOnClickListener {
            fetchAndLoadFragment(FriendsListType.FRIENDS, friendsTab)
        }

        requestsTab.setOnClickListener {
            fetchAndLoadFragment(FriendsListType.REQUESTS, requestsTab)
        }

        backCard.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setSelectedTab(selectedTab: MaterialCardView) {
        friendsTab.setCardBackgroundColor(ContextCompat.getColor(this,
            R.color.default_tab_color))
        requestsTab.setCardBackgroundColor(ContextCompat.getColor(this,
            R.color.default_tab_color))

        selectedTab.setCardBackgroundColor(ContextCompat.getColor(this,
            R.color.selected_tab_color))
    }

    private fun fetchAndLoadFragment(friendsListType: FriendsListType, selectedTab: MaterialCardView) {
        getUsers(userId, friendsListType) { userList ->
            UserListManager.setUserList(userList)

            val fragment: Fragment = when (friendsListType) {
                FriendsListType.FRIENDS -> FriendsFragment()
                FriendsListType.REQUESTS -> RequestFragment()
                else -> throw IllegalArgumentException("Invalid friends list type")
            }

            loadFragment(fragment)

            setSelectedTab(selectedTab)
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.friendsFragment, fragment)
            .commit()
    }

    private fun getUsers(userId: String, friendsListType: FriendsListType,
                         callback: (List<User>) -> Unit) {
        when (friendsListType) {
            FriendsListType.ALL_USERS -> {
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

            FriendsListType.FRIENDS -> {
                fetchUserByStatus(userId, "friendsWith", callback)
            }

            FriendsListType.REQUESTS -> {
                fetchUserByStatus(userId, "friendRequests", callback)
            }
        }
    }

    private fun fetchUserByStatus(userObjectId: String, field: String,
                                  callback: (List<User>) -> Unit) {
        Backendless.Data.of("Users").findById(
            userObjectId,
            object : AsyncCallback<MutableMap<Any?, Any?>> {
                override fun handleResponse(response: MutableMap<Any?, Any?>?) {
                    response.let { user ->
                        val frArray = user?.get(field) as? Array<String>
                        val frList = frArray?.toList() ?: emptyList()

                        Log.e("SUBSCR", frList.toString())

                        val whereClause = "objectId IN ('${frList.joinToString("', '")}')"
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
            friendRequests = (userMap["friendRequests"] as? List<String>) ?: listOf()
        )
    }

    enum class FriendsListType {
        ALL_USERS,
        FRIENDS,
        REQUESTS
    }
}
