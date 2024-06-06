package com.example.loginapp.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.loginapp.Listeners.SearchClickListener
import com.example.loginapp.Models.CurrentUserItems
import com.example.loginapp.Models.User
import com.example.loginapp.R
import com.squareup.picasso.Picasso

class SearchAdapter(private val context: Context, private var userList: List<User>,
    private val searchClickListener: SearchClickListener
) : RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.search_component, parent,
            false)
        return SearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val user = userList[position]
        Log.d("SearchAdapter", "Binding user: $user")
        holder.bind(user)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    inner class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatarImageView: ImageView = itemView.findViewById(R.id.avatarImageView)
        private val searchUserName: TextView = itemView.findViewById(R.id.searchUserName)
        private val searchUserNicknameTextView: TextView =
            itemView.findViewById(R.id.searchUserNickname)
        private val sendRequest: ImageView = itemView.findViewById(R.id.sendRequest)

        @SuppressLint("SetTextI18n")
        fun bind(user: User) {
            searchUserName.text = user.name
            searchUserNicknameTextView.text = "@${user.nickname}"

            avatarImageView.setOnClickListener {
                searchClickListener.onUserClick(user)
            }
            searchUserName.setOnClickListener {
                searchClickListener.onUserClick(user)
            }

            if (user.friendRequests.contains(CurrentUserItems.currentUserId)) {
                sendRequest.setImageResource(R.drawable.ic_decline)
            } else {
                sendRequest.setImageResource(R.drawable.ic_send_request)
            }

            sendRequest.setOnClickListener {
                if (user.friendRequests.contains(CurrentUserItems.currentUserId)) {
                    Log.d("IS IN", user.objectId)
                    searchClickListener.onCancelRequestClick(user)
                } else {
                    Log.d("NOT IN", CurrentUserItems.currentUserId.toString())
                    searchClickListener.onSendRequestClick(user)
                }
            }

            Picasso.get().load(user.avatarPath).into(avatarImageView)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(newUserList: List<User>) {
        userList = newUserList
        notifyDataSetChanged()
    }
}
