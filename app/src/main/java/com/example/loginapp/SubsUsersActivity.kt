package com.example.loginapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.loginapp.Fragments.UserListFragment

class SubsUsersActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subs_users)

        val subsType = intent.getStringExtra("subsType")
        val curObjectId = intent.getStringExtra("currentId")

        val bundle = Bundle().apply {
            putString("subsType", subsType)
            putString("currentId", curObjectId)
        }

        val userListFragment = UserListFragment().apply {
            arguments = bundle
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.subsUserContainer, userListFragment)
            .commit()
    }
}