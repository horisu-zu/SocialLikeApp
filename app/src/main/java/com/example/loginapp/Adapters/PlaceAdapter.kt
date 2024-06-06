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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.bumptech.glide.Glide
import com.example.loginapp.Listeners.PlaceClickListener
import com.example.loginapp.Listeners.TagClickListener
import com.example.loginapp.Models.Place
import com.example.loginapp.R
import java.text.SimpleDateFormat
import java.util.*

class PlaceAdapter(
    private val context: Context,
    private var dataList: List<Place>,
    private val placeClickListener: PlaceClickListener,
    private val currentUser: String,
    private val tagClickListener: TagClickListener,
    private var selectedTag: String
) : RecyclerView.Adapter<PlaceAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.place_item, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]

        with(holder) {
            descriptionView.text = data.description
            cathegoryView.text = data.cathegory
            likeCount.text = data.likeCount.toString()

            if (data.authorId.isNullOrEmpty()) {
                Log.e("PlaceAdapter", "AuthorID is null/empty")
                return
            }

            val tagsList: List<String> = data.hashtags.split(" ")

            val hashtagAdapter = TagAdapter(context, tagsList, tagClickListener, selectedTag)
            metadataRecyclerView.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,
                    false)
                adapter = hashtagAdapter
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

            val isBookmarkedBy = data.bookmarkedBy.contains(currentUser)
            bookmarkImage.setImageResource(
                if (isBookmarkedBy) R.drawable.ic_bookmarked
                else R.drawable.ic_bookmark_empty
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

            avatarImageView.setOnClickListener {
                placeClickListener.onUserClick(data)
            }

            usernameView.setOnClickListener {
                placeClickListener.onUserClick(data)
            }

            nicknameView.setOnClickListener {
                placeClickListener.onUserClick(data)
            }

            setInitialCreationDate(data.created)

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

    @SuppressLint("NotifyDataSetChanged")
    fun setPlaces(places: List<Place>) {
        dataList = places
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setTag(tag: String) {
        selectedTag = tag
        notifyDataSetChanged()
    }

    private fun getTime(creationTimeString: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
            Locale("uk", "UA"))
        val creationTime: Date = dateFormat.parse(creationTimeString) ?: Date()

        val currentTime = System.currentTimeMillis()

        val elapsedTimeMillis = currentTime - creationTime.time
        val elapsedTimeSeconds = elapsedTimeMillis / 1000
        val elapsedTimeMinutes = elapsedTimeSeconds / 60
        val elapsedTimeHours = elapsedTimeMinutes / 60

        return when {
            elapsedTimeHours >= 24 -> {
                val dateFormat = SimpleDateFormat("dd MMM", Locale("uk", "UA"))
                dateFormat.format(creationTime)
            }
            elapsedTimeHours >= 1 -> "$elapsedTimeHours год"
            elapsedTimeMinutes >= 1 -> "$elapsedTimeMinutes хв"
            else -> "$elapsedTimeSeconds с"
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var isDateSelected = false

        val descriptionView: TextView = itemView.findViewById(R.id.descriptionView)
        val avatarImageView: ImageView = itemView.findViewById(R.id.avatarImageView)
        val cathegoryView: TextView = itemView.findViewById(R.id.cathegoryView)
        val metadataRecyclerView: RecyclerView = itemView.findViewById(R.id.metadataView)
        val usernameView: TextView = itemView.findViewById(R.id.authorName)
        val nicknameView: TextView = itemView.findViewById(R.id.authorNickname)
        val creationDate: TextView = itemView.findViewById(R.id.dateView)
        val likeCount: TextView = itemView.findViewById(R.id.likeCount)
        val likeImage: ImageView = itemView.findViewById(R.id.likeView)
        val bookmarkImage: ImageView = itemView.findViewById(R.id.bookmarkView)
        val locationImage: ImageView = itemView.findViewById(R.id.locationImage)
        val imagePop: ImageView = itemView.findViewById(R.id.imagePop)
        val mapCard: CardView = itemView.findViewById(R.id.mapCard)

        init {
            creationDate.setOnClickListener {
                isDateSelected = !isDateSelected
                updateDateView()
            }
        }

        fun setInitialCreationDate(initialValue: String) {
            val formattedElapsedTime = getTime(initialValue)
            creationDate.text = formattedElapsedTime
        }

        private fun updateDateView() {
            val position = adapterPosition
            val data = dataList[position]

            if (isDateSelected) {
                creationDate.text = data.created
            } else {
                setInitialCreationDate(data.created)
            }
        }
    }
}
