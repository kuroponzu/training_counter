package com.example.training_counter

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var counter = 0
    private var currentPeriod = 0
    private var isTraining = false
    private val periodRecords = mutableListOf<Int>()
    
    private lateinit var counterTextView: TextView
    private lateinit var counterButton: Button
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var periodTextView: TextView
    private lateinit var historyTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        counterTextView = findViewById(R.id.counterTextView)
        counterButton = findViewById(R.id.counterButton)
        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)
        periodTextView = findViewById(R.id.periodTextView)
        historyTextView = findViewById(R.id.historyTextView)

        updateDisplays()

        counterButton.setOnClickListener {
            if (isTraining) {
                counter++
                updateCounterDisplay()
            }
        }

        startButton.setOnClickListener {
            startPeriod()
        }

        stopButton.setOnClickListener {
            stopPeriod()
        }
    }

    private fun startPeriod() {
        isTraining = true
        currentPeriod++
        counter = 0
        
        startButton.isEnabled = false
        stopButton.isEnabled = true
        counterButton.isEnabled = true
        
        updateDisplays()
    }

    private fun stopPeriod() {
        isTraining = false
        periodRecords.add(counter)
        
        startButton.isEnabled = true
        stopButton.isEnabled = false
        counterButton.isEnabled = false
        
        updateDisplays()
    }

    private fun updateDisplays() {
        updateCounterDisplay()
        updatePeriodDisplay()
        updateHistoryDisplay()
    }

    private fun updateCounterDisplay() {
        counterTextView.text = counter.toString()
    }

    private fun updatePeriodDisplay() {
        periodTextView.text = when {
            isTraining -> "ピリオド: $currentPeriod (進行中)"
            currentPeriod == 0 -> "ピリオド: 待機中"
            else -> "ピリオド: $currentPeriod (完了)"
        }
    }

    private fun updateHistoryDisplay() {
        if (periodRecords.isEmpty()) {
            historyTextView.text = "記録: なし"
        } else {
            val recordsText = periodRecords.mapIndexed { index, count ->
                "P${index + 1}: ${count}回"
            }.joinToString(", ")
            historyTextView.text = "記録: $recordsText"
        }
    }
}