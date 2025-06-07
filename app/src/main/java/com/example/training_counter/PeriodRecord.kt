package com.example.training_counter

import java.text.SimpleDateFormat
import java.util.*

data class PeriodRecord(
    val periodNumber: Int,
    val count: Int,
    val startTime: Long,
    val endTime: Long,
    val durationMillis: Long
) {
    fun getFormattedStartTime(): String {
        val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return formatter.format(Date(startTime))
    }
    
    fun getFormattedEndTime(): String {
        val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return formatter.format(Date(endTime))
    }
    
    fun getFormattedDuration(): String {
        val minutes = durationMillis / 60000
        val seconds = (durationMillis % 60000) / 1000
        return "${minutes}分${seconds}秒"
    }
}