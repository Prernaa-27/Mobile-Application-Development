package com.example.q4_cameraapp

import android.net.Uri
import android.os.Bundle
import android.text.format.Formatter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ImageDetailActivity : AppCompatActivity() {

    private lateinit var ivPreview: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvPath: TextView
    private lateinit var tvSize: TextView
    private lateinit var tvDate: TextView
    private lateinit var btnDelete: Button
    private lateinit var btnBack: ImageButton

    // all of these come in through the intent from GalleryActivity
    private lateinit var imageUri: Uri
    private lateinit var imageName: String
    private lateinit var imagePath: String
    private var imageSize: Long = 0
    private var imageDate: Long = 0  // raw timestamp, formatted before displaying

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_detail)

        ivPreview = findViewById(R.id.ivPreview)
        tvName = findViewById(R.id.tvImageName)
        tvPath = findViewById(R.id.tvImagePath)
        tvSize = findViewById(R.id.tvImageSize)
        tvDate = findViewById(R.id.tvImageDate)
        btnDelete = findViewById(R.id.btnDelete)
        btnBack = findViewById(R.id.btnBackDetail)

        // pull everything passed in from the gallery
        imageName = intent.getStringExtra("image_name") ?: "Unknown"
        imageUri = Uri.parse(intent.getStringExtra("image_uri") ?: "")
        imagePath = intent.getStringExtra("image_path") ?: ""
        imageSize = intent.getLongExtra("image_size", 0)
        imageDate = intent.getLongExtra("image_date", 0)

        // glide handles caching and loading efficiently
        Glide.with(this)
            .load(imageUri)
            .into(ivPreview)

        tvName.text = "Name: $imageName"
        tvPath.text = "Path: $imagePath"
        // formatShortFileSize converts bytes to KB/MB automatically
        tvSize.text = "Size: ${Formatter.formatShortFileSize(this, imageSize)}"

        // imageDate can be 0 if the metadata wasn't available
        val dateText = if (imageDate != 0L) {
            val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            formatter.format(Date(imageDate))
        } else {
            "Not available"
        }
        tvDate.text = "Date Taken: $dateText"

        btnBack.setOnClickListener {
            finish()
        }

        btnDelete.setOnClickListener {
            showDeleteDialog()
        }
    }

    private fun showDeleteDialog() {
        // confirm before deleting, can't undo this
        AlertDialog.Builder(this)
            .setTitle("Delete Image")
            .setMessage("Are you sure you want to delete this image?")
            .setPositiveButton("Delete") { _, _ ->
                deleteImage()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteImage() {
        // DocumentFile lets us delete files in user-picked folders via uri
        val documentFile = DocumentFile.fromSingleUri(this, imageUri)

        if (documentFile != null && documentFile.exists()) {
            val deleted = documentFile.delete()

            if (deleted) {
                Toast.makeText(this, "Image deleted successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Unable to delete image", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Image file not found", Toast.LENGTH_SHORT).show()
        }
    }
}