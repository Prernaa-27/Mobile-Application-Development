package com.example.q4_cameraapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ImageAdapter(
    private val imageList: List<ImageItem>,
    // lambda so the activity decides what happens when an image is tapped
    private val onItemClick: (ImageItem) -> Unit
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    // holds references to the views inside each grid item
    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imagePreview: ImageView = itemView.findViewById(R.id.ivImage)
        val imageName: TextView = itemView.findViewById(R.id.tvImageName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        // inflate the item_image layout for each grid cell
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val image = imageList[position]

        holder.imageName.text = image.name

        // glide loads from the uri string and crops to fit the fixed thumbnail size
        Glide.with(holder.itemView.context)
            .load(image.uriString)
            .centerCrop()
            .into(holder.imagePreview)

        holder.itemView.setOnClickListener {
            onItemClick(image)
        }
    }
}