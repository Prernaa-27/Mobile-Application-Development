package com.example.q4_cameraapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var btnChooseFolder: Button
    private lateinit var btnTakePhoto: Button
    private lateinit var btnOpenGallery: Button
    private lateinit var tvFolderPath: TextView

    // used to remember the selected folder across app restarts
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnChooseFolder = findViewById(R.id.btnChooseFolder)
        btnTakePhoto = findViewById(R.id.btnTakePhoto)
        btnOpenGallery = findViewById(R.id.btnOpenGallery)
        tvFolderPath = findViewById(R.id.tvFolderPath)

        sharedPreferences = getSharedPreferences("q4_prefs", Context.MODE_PRIVATE)

        // show whatever folder was saved last time the app was opened
        showSavedFolder()

        btnChooseFolder.setOnClickListener {
            folderPickerLauncher.launch(null)
        }

        btnTakePhoto.setOnClickListener {
            // need camera permission before going to gallery with camera open
            checkCameraPermissionAndOpenGallery()
        }

        btnOpenGallery.setOnClickListener {
            // false means just open gallery, don't trigger camera
            checkFolderAndOpenGallery(false)
        }
    }

    private fun showSavedFolder() {
        val folderUri = sharedPreferences.getString("folder_uri", null)
        if (folderUri != null) {
            tvFolderPath.text = folderUri
        } else {
            tvFolderPath.text = "No folder selected yet"
        }
    }

    private fun checkFolderAndOpenGallery(openCameraDirectly: Boolean) {
        val folderUri = sharedPreferences.getString("folder_uri", null)

        // don't let the user proceed without a folder, photos need somewhere to save
        if (folderUri == null) {
            Toast.makeText(this, "Please choose a folder first", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, GalleryActivity::class.java)
        // tells GalleryActivity whether to launch the camera straight away
        intent.putExtra("open_camera", openCameraDirectly)
        startActivity(intent)
    }

    private fun checkCameraPermissionAndOpenGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            checkFolderAndOpenGallery(true)
        } else {
            // permission not granted yet, ask for it
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private val folderPickerLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
            if (uri != null) {
                // takePersistableUriPermission makes sure access survives a reboot
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

                sharedPreferences.edit().putString("folder_uri", uri.toString()).apply()
                tvFolderPath.text = uri.toString()

                Toast.makeText(this, "Folder selected successfully", Toast.LENGTH_SHORT).show()
            }
        }

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                checkFolderAndOpenGallery(true)
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
}