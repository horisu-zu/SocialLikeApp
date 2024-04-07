package com.example.loginapp.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.loginapp.Listeners.UserClickListener
import com.example.loginapp.Models.User
import com.example.loginapp.R
import com.squareup.picasso.Picasso

class UserAdapter(private val context: Context, private val userList: List<User>,
                  private val userClickListener: UserClickListener) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userLayout: LinearLayout = itemView.findViewById(R.id.userLayout)
        private val avatarImageView: ImageView = itemView.findViewById(R.id.avatarImageView)
        private val authorNameTextView: TextView = itemView.findViewById(R.id.authorName)
        private val authorNicknameTextView: TextView = itemView.findViewById(R.id.authorNickname)

        @SuppressLint("SetTextI18n")
        fun bind(user: User) {
            authorNameTextView.text = user.name
            authorNicknameTextView.text = "@${user.nickname}"

            userLayout.setOnClickListener {
                userClickListener.onUserClick(user)
            }

            Picasso.get().load(user.avatarPath).into(avatarImageView)
        }
    }
}