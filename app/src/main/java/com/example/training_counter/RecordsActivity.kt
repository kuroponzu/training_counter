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
    private val gson = Gson()
    
    companion object {
        private const val PREFS_NAME = "training_counter_prefs"
        private const val KEY_PERIOD_RECORDS = "period_records"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_records)

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        
        recordsRecyclerView = findViewById(R.id.recordsRecyclerView)
        totalRecordsTextView = findViewById(R.id.totalRecordsTextView)
        backButton = findViewById(R.id.backButton)

        setupRecyclerView()
        updateTotalRecords()

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        val periodRecords = loadPeriodRecords()
        val adapter = RecordsAdapter(periodRecords)
        recordsRecyclerView.layoutManager = LinearLayoutManager(this)
        recordsRecyclerView.adapter = adapter
    }

    private fun loadPeriodRecords(): List<Int> {
        val recordsJson = sharedPreferences.getString(KEY_PERIOD_RECORDS, "[]")
        val type = object : TypeToken<List<Int>>() {}.type
        return gson.fromJson(recordsJson, type) ?: emptyList()
    }

    private fun updateTotalRecords() {
        val records = loadPeriodRecords()
        val totalPeriods = records.size
        val totalCount = records.sum()
        val averageCount = if (totalPeriods > 0) totalCount.toDouble() / totalPeriods else 0.0

        totalRecordsTextView.text = "総ピリオド数: ${totalPeriods}\n総回数: ${totalCount}\n平均: %.1f回".format(averageCount)
    }
}