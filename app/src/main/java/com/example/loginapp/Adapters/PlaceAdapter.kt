package com.example.loginapp.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.loginapp.Listeners.PlaceClickListener
import com.example.loginapp.R
import com.example.loginapp.Models.Place

class PlaceAdapter(
    private val context: Context,
    private var dataList: List<Place>,
    private val placeClickListener: PlaceClickListener,
    private val currentUser: String) :
        RecyclerView.Adapter<PlaceAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.place_item, parent,
            false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]

        with(holder) {
            descriptionView.text = data.description
            cathegoryView.text = data.cathegory
            metadataView.text = data.hashtags
            usernameView.text = data.authorNickname
            creationDate.text = data.created
            likeCount.text = data.likeCount.toString()

            val isLikedBy = data.likedBy.contains(currentUser)
            likeImage.setImageResource(
                if (isLikedBy) R.drawable.ic_like_pressed
                else R.drawable.ic_like
            )

            likeImage.setOnClickListener {
                placeClickListener.onLikeClick(data)
            }

            bookmarkImage.setOnClickListener {
                placeClickListener.onBookmarkClick(data)
            }

            cathegoryView.setOnClickListener {
                placeClickListener.onCategoryClick(data)
            }

            imagePop.setOnClickListener {
                placeClickListener.onPopClick(data, imagePop)
            }

            if (data.imageUrl.isNullOrEmpty()) {
                locationImage.visibility = View.GONE
            } else {
                locationImage.visibility = View.VISIBLE
                Glide.with(context)
                    .load(data.imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .into(locationImage)
            }

            if (data.authorId == currentUser) {
                imagePop.visibility = View.VISIBLE
            } else {
                imagePop.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    fun setPlaces(places: List<Place>) {
        dataList = places
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //val layout: LinearLayout = itemView.findViewById(R.id.placeItemLayout)

        val descriptionView: TextView = itemView.findViewById(R.id.descriptionView)
        val cathegoryView: TextView = itemView.findViewById(R.id.cathegoryView)
        val metadataView: TextView = itemView.findViewById(R.id.metadataView)
        val usernameView: TextView = itemView.findViewById(R.id.usernameView)
        val creationDate: TextView = itemView.findViewById(R.id.creationDate)
        val likeCount: TextView = itemView.findViewById(R.id.likeCount)
        val likeImage: ImageView = itemView.findViewById(R.id.likeView)
        val bookmarkImage: ImageView = itemView.findViewById(R.id.bookmarkView)
        val locationImage: ImageView = itemView.findViewById(R.id.locationImage)
        val imagePop: ImageView = itemView.findViewById(R.id.imagePop)
    }
}
