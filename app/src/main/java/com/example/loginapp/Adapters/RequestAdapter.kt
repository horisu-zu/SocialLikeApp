package com.example.loginapp.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.loginapp.Listeners.RequestClickListener
import com.example.loginapp.Models.User
import com.example.loginapp.R
import com.squareup.picasso.Picasso

class RequestAdapter(
    private val context: Context,
    private val userList: List<User>,
    private val requestClickListener: RequestClickListener
) : RecyclerView.Adapter<RequestAdapter.RequestViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.request_component, parent,
            false)
        return RequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val user = userList[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    inner class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatarImageView: ImageView = itemView.findViewById(R.id.avatarImageView)
        private val requestName: TextView = itemView.findViewById(R.id.requestName)
        private val requestNickname: TextView = itemView.findViewById(R.id.requestNickname)
        private val acceptRequest: ImageView = itemView.findViewById(R.id.acceptRequest)
        private val declineRequest: ImageView = itemView.findViewById(R.id.declineRequest)

        fun bind(user: User) {
            Picasso.get()
                .load(user.avatarPath)
                .placeholder(R.drawable.placeholder_image).into(avatarImageView)

            requestName.text = user.name
            requestNickname.text = "@${user.nickname}"

            acceptRequest.setOnClickListener {
                requestClickListener.onAcceptClick(user)
            }

            declineRequest.setOnClickListener {
                requestClickListener.onDeclineClick(user)
            }
        }
    }
}
