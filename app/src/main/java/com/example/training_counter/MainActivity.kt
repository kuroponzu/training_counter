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
    private var periodStartTime = 0L
    private val periodRecords = mutableListOf<PeriodRecord>()
    
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
        private const val KEY_PERIOD_START_TIME = "period_start_time"
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
        periodStartTime = System.currentTimeMillis()
        
        startButton.isEnabled = false
        stopButton.isEnabled = true
        counterButton.isEnabled = true
        
        updateDisplays()
        saveData()
    }

    private fun stopPeriod() {
        isTraining = false
        val endTime = System.currentTimeMillis()
        val duration = endTime - periodStartTime
        
        val periodRecord = PeriodRecord(
            periodNumber = currentPeriod,
            count = counter,
            startTime = periodStartTime,
            endTime = endTime,
            durationMillis = duration
        )
        periodRecords.add(periodRecord)
        
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
            val latestRecord = periodRecords.last()
            historyTextView.text = "最新記録: P${latestRecord.periodNumber}: ${latestRecord.count}回 (${latestRecord.getFormattedDuration()})"
        }
    }
    
    private fun saveData() {
        with(sharedPreferences.edit()) {
            putInt(KEY_COUNTER, counter)
            putInt(KEY_CURRENT_PERIOD, currentPeriod)
            putBoolean(KEY_IS_TRAINING, isTraining)
            putLong(KEY_PERIOD_START_TIME, periodStartTime)
            putString(KEY_PERIOD_RECORDS, gson.toJson(periodRecords))
            apply()
        }
    }
    
    private fun loadData() {
        // 既存データをクリアして新しいデータ形式で開始
        clearOldData()
        
        counter = sharedPreferences.getInt(KEY_COUNTER, 0)
        currentPeriod = sharedPreferences.getInt(KEY_CURRENT_PERIOD, 0)
        isTraining = sharedPreferences.getBoolean(KEY_IS_TRAINING, false)
        periodStartTime = sharedPreferences.getLong(KEY_PERIOD_START_TIME, 0L)
        
        val recordsJson = sharedPreferences.getString(KEY_PERIOD_RECORDS, "[]")
        val type = object : TypeToken<MutableList<PeriodRecord>>() {}.type
        val loadedRecords: MutableList<PeriodRecord> = gson.fromJson(recordsJson, type) ?: mutableListOf()
        periodRecords.clear()
        periodRecords.addAll(loadedRecords)
        
        startButton.isEnabled = !isTraining
        stopButton.isEnabled = isTraining
        counterButton.isEnabled = isTraining
    }
    
    private fun clearOldData() {
        // 一度だけ実行するためのフラグ
        val hasCleared = sharedPreferences.getBoolean("data_cleared_v2", false)
        if (!hasCleared) {
            with(sharedPreferences.edit()) {
                // 既存データをクリア
                remove(KEY_PERIOD_RECORDS)
                remove(KEY_COUNTER)
                remove(KEY_CURRENT_PERIOD)
                remove(KEY_IS_TRAINING)
                remove(KEY_PERIOD_START_TIME)
                // クリア済みフラグを設定
                putBoolean("data_cleared_v2", true)
                apply()
            }
        }
    }
}