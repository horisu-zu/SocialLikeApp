package com.example.loginapp.Fragments.Place

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.loginapp.Adapters.PlaceAdapter
import com.example.loginapp.Listeners.PlaceClickListener
import com.example.loginapp.Models.Place
import com.example.loginapp.R

class PlaceFragment : Fragment(), PlaceClickListener {
    private lateinit var placeRecyclerView: RecyclerView
    private lateinit var placeAdapter: PlaceAdapter
    private val placeList: MutableList<Place> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_place, container, false)
        placeRecyclerView = view.findViewById(R.id.placesRecycler)

        placeAdapter = PlaceAdapter(requireContext(), placeList, this)
        placeRecyclerView.adapter = placeAdapter
        placeRecyclerView.layoutManager = LinearLayoutManager(context)
        return view
    }

    override fun onLikeClick(place: Place) {
    }

    override fun onBookmarkClick(place: Place) {
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setPlaceList(places: List<Place>) {
        placeList.clear()
        placeList.addAll(places)
    }
}