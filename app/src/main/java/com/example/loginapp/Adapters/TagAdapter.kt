package com.example.loginapp.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.loginapp.Listeners.TagClickListener
import com.example.loginapp.R
import com.google.android.material.card.MaterialCardView

class TagAdapter(private val context: Context, private val hashtags: List<String>,
                 private val listener: TagClickListener
) :
    RecyclerView.Adapter<TagAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tag_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(hashtags[position])
        holder.itemView.setOnClickListener {
            val tag = hashtags[position]
            listener.onTagClick(tag)
        }
    }

    override fun getItemCount(): Int = hashtags.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val hashtagCard: MaterialCardView = itemView.findViewById(R.id.tagCard)
        private val hashtagText: TextView = itemView.findViewById(R.id.tagText)

        fun bind(hashtag: String) {
            hashtagText.text = hashtag
        }
    }
}
