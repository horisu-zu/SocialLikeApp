package com.example.loginapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.backendless.Backendless
import com.google.android.material.navigation.NavigationView
import com.squareup.picasso.Picasso

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

        drawerLayout = findViewById(R.id.drawer_layout)
        profileNavigationView = findViewById(R.id.profile_nav_view)
        avatarIcon = findViewById(R.id.avatarIcon)
        settingsIcon = findViewById(R.id.settingIcon)

        /*val userAvatarPath = user.getProperty("avatarPath").toString()
        Picasso.get().load(userAvatarPath).into(avatarIcon)*/

        avatarIcon.setOnClickListener {
            drawerLayout.openDrawer(profileNavigationView)
        }

        profileNavigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    true
                }
                R.id.nav_settings -> {
                    true
                }
                R.id.nav_list -> {
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
        val userSubscribersCount = navHeader.findViewById<TextView>(R.id.subscribersTextView)
        val userSubscriptionsCount = navHeader.findViewById<TextView>(R.id.subscriptionsTextView)

        if (user != null) {
            userNameTextView.text = user.getProperty("name").toString()
            userNicknameTextView.text = "@" + user.getProperty("nickname").toString()

            val userAvatarPath = user.getProperty("avatarPath").toString()
            Picasso.get().load(userAvatarPath).into(userAvatarImageView)

            userSubscribersCount.text = "Кількість підписників: " +
                    user.getProperty("subscribersCount").toString()
            userSubscriptionsCount.text = "Кількість підписок: " +
                    user.getProperty("subscriptionsCount").toString()
        } else {
        }
    }
}
