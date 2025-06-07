package com.example.training_counter

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RecordsActivity : AppCompatActivity() {
    private lateinit var recordsRecyclerView: RecyclerView
    private lateinit var totalRecordsTextView: TextView
    private lateinit var backButton: Button
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var currentMode: ExerciseMode
    private val gson = Gson()
    
    companion object {
        private const val PREFS_NAME = "training_counter_prefs"
        private const val KEY_PERIOD_RECORDS_PUSHUP = "period_records_pushup"
        private const val KEY_PERIOD_RECORDS_SQUAT = "period_records_squat"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_records)

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        
        // インテントからモードを取得
        val modeName = intent.getStringExtra("currentMode") ?: ExerciseMode.PUSHUP.name
        currentMode = try {
            ExerciseMode.valueOf(modeName)
        } catch (e: IllegalArgumentException) {
            ExerciseMode.PUSHUP
        }
        
        recordsRecyclerView = findViewById(R.id.recordsRecyclerView)
        totalRecordsTextView = findViewById(R.id.totalRecordsTextView)
        backButton = findViewById(R.id.backButton)

        setupRecyclerView()
        updateTotalRecords()
        updateTitle()

        backButton.setOnClickListener {
            finish()
        }
    }
    
    private fun updateTitle() {
        val titleTextView = findViewById<TextView>(R.id.titleTextView)
        titleTextView.text = "${currentMode.displayName} 記録一覧"
    }

    private fun setupRecyclerView() {
        val periodRecords = loadPeriodRecords()
        val adapter = RecordsAdapter(periodRecords)
        recordsRecyclerView.layoutManager = LinearLayoutManager(this)
        recordsRecyclerView.adapter = adapter
    }

    private fun loadPeriodRecords(): List<PeriodRecord> {
        val recordsKey = when (currentMode) {
            ExerciseMode.PUSHUP -> KEY_PERIOD_RECORDS_PUSHUP
            ExerciseMode.SQUAT -> KEY_PERIOD_RECORDS_SQUAT
        }
        
        val recordsJson = sharedPreferences.getString(recordsKey, "[]")
        val type = object : TypeToken<List<PeriodRecord>>() {}.type
        return gson.fromJson(recordsJson, type) ?: emptyList()
    }

    private fun updateTotalRecords() {
        val records = loadPeriodRecords()
        val totalPeriods = records.size
        val totalCount = records.sumOf { it.count }
        val averageCount = if (totalPeriods > 0) totalCount.toDouble() / totalPeriods else 0.0
        val totalDuration = records.sumOf { it.durationMillis }
        val averageDuration = if (totalPeriods > 0) totalDuration / totalPeriods else 0L

        val avgMinutes = averageDuration / 60000
        val avgSeconds = (averageDuration % 60000) / 1000

        totalRecordsTextView.text = "総ピリオド数: ${totalPeriods}\n総回数: ${totalCount}\n平均: %.1f回\n平均時間: ${avgMinutes}分${avgSeconds}秒".format(averageCount)
    }
}