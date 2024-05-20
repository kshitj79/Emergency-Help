package com.android.gps

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity2 : AppCompatActivity() {

    private lateinit var editText: EditText
    private lateinit var saveButton: Button
    private lateinit var clearButton: Button
    private lateinit var displayText: TextView

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity2)

        // Initialize views
        editText = findViewById(R.id.editText)
        saveButton = findViewById(R.id.saveButton)
        clearButton = findViewById(R.id.clearButton)
        displayText = findViewById(R.id.displayText)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        // Load saved phone numbers
        loadPhoneNumbers()

        // Save button click listener
        saveButton.setOnClickListener {
            val phoneNumber = editText.text.toString()
            savePhoneNumber(phoneNumber)
        }

        // Clear button click listener
        clearButton.setOnClickListener {
            clearPhoneNumbers()
        }

        // Apply insets listener
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun savePhoneNumber(phoneNumber: String) {
        // Save the phone number in SharedPreferences
        val phoneNumbers = loadPhoneNumbers().toMutableList()
        if (phoneNumbers.size < 5) {
            phoneNumbers.add(phoneNumber)
            sharedPreferences.edit().putStringSet("phoneNumbers", phoneNumbers.toSet()).apply()
            loadPhoneNumbers()
        } else {
            // Show a message that maximum limit reached
        }
    }

    private fun loadPhoneNumbers(): Set<String> {
        // Retrieve the saved phone numbers from SharedPreferences
        val phoneNumbers = sharedPreferences.getStringSet("phoneNumbers", emptySet())

        // Display the saved phone numbers
        displayText.text = "Saved Phone Numbers: ${phoneNumbers?.joinToString(", ")}"
        return phoneNumbers ?: emptySet()
    }


    private fun clearPhoneNumbers() {
        // Clear all saved phone numbers in SharedPreferences
        sharedPreferences.edit().remove("phoneNumbers").apply()
        loadPhoneNumbers()
    }
}
