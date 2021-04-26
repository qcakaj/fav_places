package com.ckj.fraktonjobapplicationexample.ui.favorite_places

import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.ckj.fraktonjobapplicationexample.R
import com.ckj.fraktonjobapplicationexample.data.db.PlaceEntity
import com.ckj.fraktonjobapplicationexample.databinding.ItemPlacesBinding
import com.ckj.fraktonjobapplicationexample.util.roundTo

class FavoritePlacesAdapter(private val onPlaceClicked: (PlaceEntity) -> Unit) :
    ListAdapter<PlaceEntity, FavoritePlacesAdapter.PlaceViewHolder>(ItemPlaceDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        return PlaceViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        holder.bind(getItem(position), onPlaceClicked)
    }

    class PlaceViewHolder private constructor(private val binding: ItemPlacesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: PlaceEntity,
            onPlaceClicked: (PlaceEntity) -> Unit,
        ) {

            binding.placeImageView.load(uri = Uri.parse(item.imageUri)) {
                placeholder(ColorDrawable(item.placeHolderColor))
            }
            binding.locationNameTv.text = item.name
            val latitude =
                binding.root.context.getString(
                    R.string.latitude,
                    item.latitude?.roundTo(2).toString()
                )
            val longitude = binding.root.context.getString(
                R.string.longitude,
                item.longitude?.roundTo(2).toString()
            )
            binding.locationAddress.text = "$latitude/n$longitude"

            binding.root.setOnClickListener {
                onPlaceClicked(item)
            }

        }

        companion object {
            fun from(parent: ViewGroup): PlaceViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemPlacesBinding.inflate(layoutInflater, parent, false)

                return PlaceViewHolder(binding)
            }
        }
    }
}

class ItemPlaceDiffCallback : DiffUtil.ItemCallback<PlaceEntity>() {
    override fun areItemsTheSame(oldItem: PlaceEntity, newItem: PlaceEntity): Boolean {
        return oldItem.placeId == newItem.placeId
    }

    override fun areContentsTheSame(oldItem: PlaceEntity, newItem: PlaceEntity): Boolean {
        return oldItem == newItem
    }
}

