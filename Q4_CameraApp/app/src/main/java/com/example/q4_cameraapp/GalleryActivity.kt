package com.example.q4_cameraapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GalleryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var btnBack: ImageButton
    private lateinit var btnCamera: ImageButton

    private lateinit var sharedPreferences: SharedPreferences

    private var currentPhotoUri: Uri? = null
    private val imageList = ArrayList<ImageItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        recyclerView = findViewById(R.id.recyclerViewImages)
        tvEmpty = findViewById(R.id.tvEmpty)
        btnBack = findViewById(R.id.btnBack)
        btnCamera = findViewById(R.id.btnCamera)

        sharedPreferences = getSharedPreferences("q4_prefs", Context.MODE_PRIVATE)

        recyclerView.layoutManager = GridLayoutManager(this, 2)

        btnBack.setOnClickListener {
            finish()
        }

        btnCamera.setOnClickListener {
            openCamera()
        }

        loadImages()

        val openCameraDirectly = intent.getBooleanExtra("open_camera", false)
        if (openCameraDirectly) {
            openCamera()
        }
    }

    override fun onResume() {
        super.onResume()
        loadImages()
    }

    private fun loadImages() {
        imageList.clear()

        val folderUriString = sharedPreferences.getString("folder_uri", null)
        if (folderUriString == null) {
            tvEmpty.text = "No folder selected"
            tvEmpty.visibility = TextView.VISIBLE
            recyclerView.adapter = null
            return
        }

        val folderUri = Uri.parse(folderUriString)
        val folderDocument = DocumentFile.fromTreeUri(this, folderUri)

        if (folderDocument == null || !folderDocument.exists()) {
            tvEmpty.text = "Folder not available"
            tvEmpty.visibility = TextView.VISIBLE
            recyclerView.adapter = null
            return
        }

        val files = folderDocument.listFiles()

        for (file in files) {
            if (file.isFile && file.type?.startsWith("image/") == true) {
                val imageItem = ImageItem(
                    name = file.name ?: "Unknown",
                    uriString = file.uri.toString(),
                    path = file.uri.toString(),
                    size = file.length(),
                    dateTaken = file.lastModified()
                )
                imageList.add(imageItem)
            }
        }

        imageList.sortByDescending { it.dateTaken }

        if (imageList.isEmpty()) {
            tvEmpty.text = "No images found in this folder"
            tvEmpty.visibility = TextView.VISIBLE
        } else {
            tvEmpty.visibility = TextView.GONE
        }

        recyclerView.adapter = ImageAdapter(imageList) { imageItem ->
            val intent = Intent(this, ImageDetailActivity::class.java)
            intent.putExtra("image_name", imageItem.name)
            intent.putExtra("image_uri", imageItem.uriString)
            intent.putExtra("image_path", imageItem.path)
            intent.putExtra("image_size", imageItem.size)
            intent.putExtra("image_date", imageItem.dateTaken)
            startActivity(intent)
        }
    }

    private fun openCamera() {
        val folderUriString = sharedPreferences.getString("folder_uri", null)
        if (folderUriString == null) {
            Toast.makeText(this, "Please choose a folder first", Toast.LENGTH_SHORT).show()
            return
        }

        val folderUri = Uri.parse(folderUriString)
        val folderDocument = DocumentFile.fromTreeUri(this, folderUri)

        if (folderDocument == null || !folderDocument.exists()) {
            Toast.makeText(this, "Folder not available", Toast.LENGTH_SHORT).show()
            return
        }

        val fileName = "IMG_${SystemClock.elapsedRealtime()}.jpg"
        val imageFile = folderDocument.createFile("image/jpeg", fileName)

        if (imageFile != null) {
            currentPhotoUri = imageFile.uri
            takePictureLauncher.launch(currentPhotoUri!!)
        } else {
            Toast.makeText(this, "Unable to create image file", Toast.LENGTH_SHORT).show()
        }
    }

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                Toast.makeText(this, "Photo saved successfully", Toast.LENGTH_SHORT).show()
                loadImages()
            } else {
                Toast.makeText(this, "Photo capture cancelled", Toast.LENGTH_SHORT).show()
            }
        }
}