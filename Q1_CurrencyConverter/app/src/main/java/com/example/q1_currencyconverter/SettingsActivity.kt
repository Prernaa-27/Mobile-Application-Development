package com.example.q1_currencyconverter

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class SettingsActivity : AppCompatActivity() {

    private lateinit var btnLightMode: Button
    private lateinit var btnDarkMode: Button
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {

        sharedPreferences = getSharedPreferences("theme_pref", MODE_PRIVATE)
        val savedTheme = sharedPreferences.getString("theme_mode", "LIGHT")

        if (savedTheme == "DARK") {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        btnLightMode = findViewById(R.id.btnLightMode)
        btnDarkMode = findViewById(R.id.btnDarkMode)

        btnLightMode.setOnClickListener {
            sharedPreferences.edit().putString("theme_mode", "LIGHT").apply()
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            recreate()
        }

        btnDarkMode.setOnClickListener {
            sharedPreferences.edit().putString("theme_mode", "DARK").apply()
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            recreate()
        }
    }
}