package com.example.q1_currencyconverter

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var etAmount: EditText
    private lateinit var spinnerFrom: Spinner
    private lateinit var spinnerTo: Spinner
    private lateinit var btnConvert: Button
    private lateinit var btnSettings: Button
    private lateinit var tvResult: TextView

    private val currencyRates = mapOf(
        "INR" to 1.0,
        "USD" to 83.0,
        "JPY" to 0.56,
        "EUR" to 90.0
    )

    override fun onCreate(savedInstanceState: Bundle?) {

        val sharedPreferences = getSharedPreferences("theme_pref", MODE_PRIVATE)
        val savedTheme = sharedPreferences.getString("theme_mode", "LIGHT")

        if (savedTheme == "DARK") {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etAmount = findViewById(R.id.etAmount)
        spinnerFrom = findViewById(R.id.spinnerFrom)
        spinnerTo = findViewById(R.id.spinnerTo)
        btnConvert = findViewById(R.id.btnConvert)
        btnSettings = findViewById(R.id.btnSettings)
        tvResult = findViewById(R.id.tvResult)

        val currencyList = arrayOf("INR", "USD", "JPY", "EUR")

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            currencyList
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinnerFrom.adapter = adapter
        spinnerTo.adapter = adapter

        btnConvert.setOnClickListener {
            convertCurrency()
        }

        btnSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun convertCurrency() {
        val amountText = etAmount.text.toString().trim()

        if (amountText.isEmpty()) {
            Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDoubleOrNull()

        if (amount == null) {
            Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show()
            return
        }

        val fromCurrency = spinnerFrom.selectedItem.toString()
        val toCurrency = spinnerTo.selectedItem.toString()

        val fromRate = currencyRates[fromCurrency] ?: 1.0
        val toRate = currencyRates[toCurrency] ?: 1.0

        // Convert source currency to INR, then INR to target currency
        val amountInInr = amount * fromRate
        val convertedAmount = amountInInr / toRate

        val resultText = String.format(
            Locale.US,
            "%.2f %s = %.2f %s",
            amount,
            fromCurrency,
            convertedAmount,
            toCurrency
        )

        tvResult.text = resultText
    }
}