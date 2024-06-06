package com.example.loginapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.backendless.Backendless
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.loginapp.Fragments.FolderFragment
import com.example.loginapp.Models.CurrentUserItems
import com.google.android.material.navigation.NavigationView

class HomeActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var profileNavigationView: NavigationView
    private lateinit var avatarIcon : ImageView
    private lateinit var settingsIcon : ImageView

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val user = Backendless.UserService.CurrentUser()

        CurrentUserItems.setCurrentId(user.objectId)

        drawerLayout = findViewById(R.id.drawer_layout)
        profileNavigationView = findViewById(R.id.profile_nav_view)
        avatarIcon = findViewById(R.id.avatarIcon)
        settingsIcon = findViewById(R.id.settingIcon)

        /*if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(
                R.id.fragment_container,
                FolderFragment()
            ).commit()
        }*/

        val userAvatarPath = user.getProperty("avatarPath").toString()
        Glide.with(this)
            .load(userAvatarPath)
            .apply(RequestOptions().circleCrop())
            .into(avatarIcon)

        avatarIcon.setOnClickListener {
            drawerLayout.openDrawer(profileNavigationView)
        }

        profileNavigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)

                    true
                }
                R.id.nav_places -> {
                    val intent = Intent(this@HomeActivity, PlaceActivity::class.java)
                    startActivity(intent)

                    true
                }
                R.id.nav_folders -> {
                    supportFragmentManager.beginTransaction().replace(
                        R.id.fragment_container,
                        FolderFragment()
                    ).commit()
                    true
                }
                R.id.nav_bookmarks -> {
                    true
                }
                R.id.nav_friends -> {
                    val intent = Intent(this, FriendsActivity::class.java)
                    intent.putExtra("userId", user.objectId)
                    startActivity(intent)

                    true
                }
                else -> false
            }
        }

        settingsIcon.setOnClickListener {
            val intent = Intent(this@HomeActivity,
                SettingsActivity::class.java)

            startActivity(intent)
        }

        val navHeader = profileNavigationView.getHeaderView(0)
        val userNameTextView = navHeader.findViewById<TextView>(R.id.nameTextView)
        val userNicknameTextView = navHeader.findViewById<TextView>(R.id.nicknameTextView)
        val userAvatarImageView = navHeader.findViewById<ImageView>(R.id.avatarImageView)
        val userSubscribersCount = navHeader.findViewById<TextView>(R.id.subscribersCount)
        val userSubscriptionsCount = navHeader.findViewById<TextView>(R.id.subscriptionsCount)

        if (user != null) {
            userNameTextView.text = user.getProperty("name").toString()
            userNicknameTextView.text = "@" + user.getProperty("nickname").toString()

            Glide.with(this)
                .load(userAvatarPath)
                .apply(RequestOptions().circleCrop())
                .into(userAvatarImageView)

            userSubscribersCount.text = user.getProperty("subscribersCount").toString()
            userSubscriptionsCount.text = user.getProperty("subscriptionsCount").toString()
        } else {
        }
    }
}
