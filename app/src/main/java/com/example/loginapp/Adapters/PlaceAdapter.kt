package com.example.loginapp.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.loginapp.Listeners.PlaceClickListener
import com.example.loginapp.R
import com.example.loginapp.Models.Place

class PlaceAdapter(private val context: Context, private val dataList: List<Place>,
        private val placeClickListener: PlaceClickListener) :
    RecyclerView.Adapter<PlaceAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.place_item, parent,
            false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]

        holder.descriptionView.text = data.description
        holder.cathegoryView.text = data.cathegory
        holder.locationView.text = data.coordinates.toString()
        holder.metadataView.text = data.hashtags
        holder.usernameView.text = data.authorNickname
        holder.creationDate.text = data.created
        holder.likeCount.text = data.likeCount.toString()

        //holder.likeImage.setImageResource(R.drawable.ic_like)

        holder.likeImage.setOnClickListener {
            placeClickListener.onLikeClick(dataList.get(holder.adapterPosition))
        }

        holder.bookmarkImage.setOnClickListener {
            placeClickListener.onBookmarkClick(dataList.get(holder.adapterPosition))
        }

        if (data.imageUrl.isNullOrEmpty()) {
            holder.locationImage.visibility = View.GONE
        } else {
            holder.locationImage.visibility = View.VISIBLE
            Glide.with(context)
                .load(data.imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .into(holder.locationImage)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val descriptionView: TextView = itemView.findViewById(R.id.descriptionView)
        val cathegoryView: TextView = itemView.findViewById(R.id.cathegoryView)
        val locationView: TextView = itemView.findViewById(R.id.locationView)
        val metadataView: TextView = itemView.findViewById(R.id.metadataView)
        val usernameView: TextView = itemView.findViewById(R.id.usernameView)
        val creationDate: TextView = itemView.findViewById(R.id.creationDate)
        val likeCount: TextView = itemView.findViewById(R.id.likeCount)
        val likeImage: ImageView = itemView.findViewById(R.id.likeView)
        val bookmarkImage: ImageView = itemView.findViewById(R.id.bookmarkView)
        val locationImage: ImageView = itemView.findViewById(R.id.locationImage)
    }
}
