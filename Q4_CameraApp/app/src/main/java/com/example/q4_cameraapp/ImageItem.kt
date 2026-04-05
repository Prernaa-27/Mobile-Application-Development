package com.example.q4_cameraapp

// holds all the info we need for each image in the gallery
data class ImageItem(
    val name: String,
    val uriString: String,
    val path: String,
    val size: Long,
    val dateTaken: Long  // stored as a timestamp, formatted when displayed
)