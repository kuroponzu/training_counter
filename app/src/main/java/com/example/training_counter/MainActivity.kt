package com.example.training_counter

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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
    private lateinit var recordsButton: Button
    
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()
    
    companion object {
        private const val PREFS_NAME = "training_counter_prefs"
        private const val KEY_COUNTER = "counter"
        private const val KEY_CURRENT_PERIOD = "current_period"
        private const val KEY_IS_TRAINING = "is_training"
        private const val KEY_PERIOD_RECORDS = "period_records"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        
        counterTextView = findViewById(R.id.counterTextView)
        counterButton = findViewById(R.id.counterButton)
        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)
        periodTextView = findViewById(R.id.periodTextView)
        historyTextView = findViewById(R.id.historyTextView)
        recordsButton = findViewById(R.id.recordsButton)

        loadData()
        updateDisplays()

        counterButton.setOnClickListener {
            if (isTraining) {
                counter++
                updateCounterDisplay()
                saveData()
            }
        }

        startButton.setOnClickListener {
            startPeriod()
        }

        stopButton.setOnClickListener {
            stopPeriod()
        }

        recordsButton.setOnClickListener {
            val intent = Intent(this, RecordsActivity::class.java)
            startActivity(intent)
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
        saveData()
    }

    private fun stopPeriod() {
        isTraining = false
        periodRecords.add(counter)
        
        startButton.isEnabled = true
        stopButton.isEnabled = false
        counterButton.isEnabled = false
        
        updateDisplays()
        saveData()
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
            val latestPeriod = periodRecords.size
            val latestCount = periodRecords.last()
            historyTextView.text = "最新記録: P${latestPeriod}: ${latestCount}回"
        }
    }
    
    private fun saveData() {
        with(sharedPreferences.edit()) {
            putInt(KEY_COUNTER, counter)
            putInt(KEY_CURRENT_PERIOD, currentPeriod)
            putBoolean(KEY_IS_TRAINING, isTraining)
            putString(KEY_PERIOD_RECORDS, gson.toJson(periodRecords))
            apply()
        }
    }
    
    private fun loadData() {
        counter = sharedPreferences.getInt(KEY_COUNTER, 0)
        currentPeriod = sharedPreferences.getInt(KEY_CURRENT_PERIOD, 0)
        isTraining = sharedPreferences.getBoolean(KEY_IS_TRAINING, false)
        
        val recordsJson = sharedPreferences.getString(KEY_PERIOD_RECORDS, "[]")
        val type = object : TypeToken<MutableList<Int>>() {}.type
        val loadedRecords: MutableList<Int> = gson.fromJson(recordsJson, type) ?: mutableListOf()
        periodRecords.clear()
        periodRecords.addAll(loadedRecords)
        
        startButton.isEnabled = !isTraining
        stopButton.isEnabled = isTraining
        counterButton.isEnabled = isTraining
    }
}