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

    // uri of the photo being taken, kept until the camera returns
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

        // 2 column grid for the gallery
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        btnBack.setOnClickListener {
            finish()
        }

        btnCamera.setOnClickListener {
            openCamera()
        }

        loadImages()

        // MainActivity passes this as true when the user taps "Take Photo"
        val openCameraDirectly = intent.getBooleanExtra("open_camera", false)
        if (openCameraDirectly) {
            openCamera()
        }
    }

    override fun onResume() {
        super.onResume()
        // reload so newly taken photos show up when coming back from camera
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
        // fromTreeUri gives access to the whole folder, not just a single file
        val folderDocument = DocumentFile.fromTreeUri(this, folderUri)

        if (folderDocument == null || !folderDocument.exists()) {
            tvEmpty.text = "Folder not available"
            tvEmpty.visibility = TextView.VISIBLE
            recyclerView.adapter = null
            return
        }

        val files = folderDocument.listFiles()

        for (file in files) {
            // skip non-image files like videos or docs that might be in the same folder
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

        // newest photos at the top
        imageList.sortByDescending { it.dateTaken }

        if (imageList.isEmpty()) {
            tvEmpty.text = "No images found in this folder"
            tvEmpty.visibility = TextView.VISIBLE
        } else {
            tvEmpty.visibility = TextView.GONE
        }

        // pass all image details through the intent so ImageDetailActivity doesn't need to reload them
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

        // elapsedRealtime gives a unique number so two photos never share a filename
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
                // user backed out of the camera, file was already created so nothing to clean up here
                Toast.makeText(this, "Photo capture cancelled", Toast.LENGTH_SHORT).show()
            }
        }
}