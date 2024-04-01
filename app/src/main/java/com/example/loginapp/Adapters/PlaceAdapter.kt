package com.example.loginapp.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.bumptech.glide.Glide
import com.example.loginapp.Listeners.PlaceClickListener
import com.example.loginapp.Models.Place
import com.example.loginapp.R

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
            creationDate.text = data.created
            likeCount.text = data.likeCount.toString()

            if (data.authorId.isNullOrEmpty()) {
                Log.e("PlaceAdapter", "AuthorID is null/empty")
                return
            }

            Backendless.Data.of("Users").findById(data.authorId,
                object : AsyncCallback<Map<Any?, Any?>> {
                    @SuppressLint("SetTextI18n")
                    override fun handleResponse(response: Map<Any?, Any?>) {
                        response.let { userData ->
                            usernameView.text = userData["name"].toString()
                            nicknameView.text = "@${userData["nickname"].toString()}"

                            val avatarUrl = userData["avatarPath"].toString()
                            Glide.with(context)
                                .load(avatarUrl)
                                .placeholder(R.drawable.placeholder_image)
                                .into(avatarImageView)
                        }
                    }

                    override fun handleFault(fault: BackendlessFault?) {
                    Log.e("PlaceAdapter", "Не вдалося отримати дані користувача: " +
                            fault?.message)
                }
            })

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

            mapCard.setOnClickListener {
                placeClickListener.onMapClick(data)
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
        val avatarImageView: ImageView = itemView.findViewById(R.id.avatarImageView)
        val cathegoryView: TextView = itemView.findViewById(R.id.cathegoryView)
        val metadataView: TextView = itemView.findViewById(R.id.metadataView)
        val usernameView: TextView = itemView.findViewById(R.id.authorName)
        val nicknameView: TextView = itemView.findViewById(R.id.authorNickname)
        val creationDate: TextView = itemView.findViewById(R.id.dateView)
        val likeCount: TextView = itemView.findViewById(R.id.likeCount)
        val likeImage: ImageView = itemView.findViewById(R.id.likeView)
        val bookmarkImage: ImageView = itemView.findViewById(R.id.bookmarkView)
        val locationImage: ImageView = itemView.findViewById(R.id.locationImage)
        val imagePop: ImageView = itemView.findViewById(R.id.imagePop)
        val mapCard: CardView = itemView.findViewById(R.id.mapCard)
    }
}
