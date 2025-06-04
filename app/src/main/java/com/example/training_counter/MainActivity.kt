package com.example.training_counter

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var counter = 0
    private lateinit var counterTextView: TextView
    private lateinit var counterButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        counterTextView = findViewById(R.id.counterTextView)
        counterButton = findViewById(R.id.counterButton)

        updateCounterDisplay()

        counterButton.setOnClickListener {
            counter++
            updateCounterDisplay()
        }
    }

    private fun updateCounterDisplay() {
        counterTextView.text = counter.toString()
    }
}