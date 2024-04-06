package com.example.loginapp.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.loginapp.Listeners.TagClickListener
import com.example.loginapp.R
import com.google.android.material.card.MaterialCardView

class TagAdapter(
    private val context: Context,
    private val hashtags: List<String>,
    private val listener: TagClickListener,
    private val selectedTag: String
) : RecyclerView.Adapter<TagAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tag_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val hashtag = hashtags[position]
        holder.bind(hashtag)

        val isSelected = hashtag == selectedTag

        val backgroundColor = if (isSelected) R.color.selected_tag_background else R.color.default_tag_background
        val textColor = if (isSelected) R.color.selected_tag_text else R.color.default_tag_text

        holder.hashtagCard.setCardBackgroundColor(ContextCompat.getColor(context, backgroundColor))
        holder.hashtagText.setTextColor(ContextCompat.getColor(context, textColor))

        holder.itemView.setOnClickListener {
            listener.onTagClick(hashtag)
        }
    }

    override fun getItemCount(): Int = hashtags.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val hashtagCard: MaterialCardView = itemView.findViewById(R.id.tagCard)
        val hashtagText: TextView = itemView.findViewById(R.id.tagText)

        fun bind(hashtag: String) {
            hashtagText.text = hashtag
        }
    }
}
