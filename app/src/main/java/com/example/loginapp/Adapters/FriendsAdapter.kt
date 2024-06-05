package com.example.loginapp.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.loginapp.Listeners.FriendClickListener
import com.example.loginapp.Models.User
import com.example.loginapp.R
import com.squareup.picasso.Picasso

class FriendsAdapter(private val context: Context, private val userList: List<User>,
                     private val friendClickListener: FriendClickListener) :
    RecyclerView.Adapter<FriendsAdapter.FriendViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.friends_component, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val user = userList[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    inner class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatarImageView: ImageView = itemView.findViewById(R.id.avatarImageView)
        private val friendNameTextView: TextView = itemView.findViewById(R.id.friendName)
        private val friendNicknameTextView: TextView = itemView.findViewById(R.id.friendNickname)
        private val friendInteraction: ImageView = itemView.findViewById(R.id.friendInteraction)

        @SuppressLint("SetTextI18n")
        fun bind(user: User) {
            friendNameTextView.text = user.name
            friendNicknameTextView.text = "@${user.nickname}"

            avatarImageView.setOnClickListener {
                friendClickListener.onUserClick(user)
            }

            friendNameTextView.setOnClickListener {
                friendClickListener.onUserClick(user)
            }

            friendInteraction.setOnClickListener {
                friendClickListener.onPopClick(user, imageView = friendInteraction)
            }

            Picasso.get().load(user.avatarPath).into(avatarImageView)
        }
    }
}