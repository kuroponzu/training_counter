package com.example.training_counter

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), SensorEventListener {
    private var counter = 0
    private var currentPeriod = 0
    private var isTraining = false
    private var periodStartTime = 0L
    private var currentMode = ExerciseMode.PUSHUP
    private val periodRecords = mutableListOf<PeriodRecord>()
    
    // センサー関連
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var lastAcceleration = 9.8f
    private var currentAcceleration = 9.8f
    private var acceleration = 0f
    private var squatCount = 0
    private var lastSquatTime = 0L
    private val squatThreshold = 2f
    private val squatCooldown = 500L
    
    private lateinit var counterTextView: TextView
    private lateinit var counterButton: Button
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var periodTextView: TextView
    private lateinit var historyTextView: TextView
    private lateinit var recordsButton: Button
    private lateinit var currentModeTextView: TextView
    private lateinit var pushupModeButton: Button
    private lateinit var squatModeButton: Button
    
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()
    
    companion object {
        private const val PREFS_NAME = "training_counter_prefs"
        private const val KEY_COUNTER = "counter"
        private const val KEY_CURRENT_PERIOD = "current_period"
        private const val KEY_IS_TRAINING = "is_training"
        private const val KEY_PERIOD_START_TIME = "period_start_time"
        private const val KEY_CURRENT_MODE = "current_mode"
        private const val KEY_PERIOD_RECORDS_PUSHUP = "period_records_pushup"
        private const val KEY_PERIOD_RECORDS_SQUAT = "period_records_squat"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        
        // センサー初期化
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        
        counterTextView = findViewById(R.id.counterTextView)
        counterButton = findViewById(R.id.counterButton)
        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)
        periodTextView = findViewById(R.id.periodTextView)
        historyTextView = findViewById(R.id.historyTextView)
        recordsButton = findViewById(R.id.recordsButton)
        currentModeTextView = findViewById(R.id.currentModeTextView)
        pushupModeButton = findViewById(R.id.pushupModeButton)
        squatModeButton = findViewById(R.id.squatModeButton)
        
        // ボタンの取得確認
        Log.d("MainActivity", "pushupModeButton: $pushupModeButton")
        Log.d("MainActivity", "squatModeButton: $squatModeButton")
        
        // ボタンがnullでないことを確認
        if (pushupModeButton == null) {
            Log.e("MainActivity", "pushupModeButton is null!")
        }
        if (squatModeButton == null) {
            Log.e("MainActivity", "squatModeButton is null!")
        }

        try {
            loadData()
            updateDisplays()
            Log.d("MainActivity", "loadData and updateDisplays completed successfully")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in loadData or updateDisplays", e)
            Toast.makeText(this, "初期化エラー: ${e.message}", Toast.LENGTH_LONG).show()
        }

        // 最もシンプルなテスト - 直接ログのみ
        Log.d("MainActivity", "Setting up button listeners")
        
        pushupModeButton.setOnClickListener {
            Log.d("MainActivity", "=== PUSHUP BUTTON CLICKED ===")
            Toast.makeText(this, "腕立て伏せボタンがクリックされました", Toast.LENGTH_SHORT).show()
            switchMode(ExerciseMode.PUSHUP)
        }

        squatModeButton.setOnClickListener {
            Log.d("MainActivity", "=== SQUAT BUTTON CLICKED ===")
            Toast.makeText(this, "スクワットボタンがクリックされました", Toast.LENGTH_SHORT).show()
            switchMode(ExerciseMode.SQUAT)
        }
        
        Log.d("MainActivity", "Button listeners set up complete")

        counterButton.setOnClickListener {
            if (isTraining && currentMode == ExerciseMode.PUSHUP) {
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
            intent.putExtra("currentMode", currentMode.name)
            startActivity(intent)
        }
    }

    private fun switchMode(mode: ExerciseMode) {
        Log.d("MainActivity", "switchMode called with: ${mode.name}, isTraining: $isTraining")
        if (!isTraining) {
            Log.d("MainActivity", "Switching from ${currentMode.name} to ${mode.name}")
            currentMode = mode
            
            // モード切り替え時は既存の記録をクリアして新しいモードのデータを読み込み
            loadModeSpecificData()
            updateDisplays()
            saveCurrentMode()
            Log.d("MainActivity", "Mode switch completed")
        } else {
            Log.d("MainActivity", "Cannot switch mode while training")
        }
    }
    
    private fun loadModeSpecificData() {
        // モード別の記録データのみを読み込み
        val recordsKey = when (currentMode) {
            ExerciseMode.PUSHUP -> KEY_PERIOD_RECORDS_PUSHUP
            ExerciseMode.SQUAT -> KEY_PERIOD_RECORDS_SQUAT
        }
        
        val recordsJson = sharedPreferences.getString(recordsKey, "[]")
        val type = object : TypeToken<MutableList<PeriodRecord>>() {}.type
        val loadedRecords: MutableList<PeriodRecord> = gson.fromJson(recordsJson, type) ?: mutableListOf()
        periodRecords.clear()
        periodRecords.addAll(loadedRecords)
        
        Log.d("MainActivity", "Loaded ${periodRecords.size} records for ${currentMode.name}")
    }
    
    private fun saveCurrentMode() {
        // 現在のモードのみを保存
        with(sharedPreferences.edit()) {
            putString(KEY_CURRENT_MODE, currentMode.name)
            apply()
        }
    }

    override fun onResume() {
        super.onResume()
        if (isTraining && currentMode == ExerciseMode.SQUAT) {
            accelerometer?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER && currentMode == ExerciseMode.SQUAT && isTraining) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            lastAcceleration = currentAcceleration
            currentAcceleration = sqrt(x * x + y * y + z * z)
            acceleration = kotlin.math.abs(currentAcceleration - lastAcceleration)

            Log.d("SquatDetection", "Acceleration: $acceleration, Threshold: $squatThreshold")

            if (acceleration > squatThreshold) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastSquatTime > squatCooldown) {
                    counter++
                    lastSquatTime = currentTime
                    Log.d("SquatDetection", "Squat detected! Count: $counter")
                    updateCounterDisplay()
                    saveData()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 実装不要
    }

    private fun startPeriod() {
        isTraining = true
        currentPeriod++
        counter = 0
        periodStartTime = System.currentTimeMillis()
        
        startButton.isEnabled = false
        stopButton.isEnabled = true
        counterButton.isEnabled = (currentMode == ExerciseMode.PUSHUP)
        
        // スクワットモードの場合はセンサーを開始
        if (currentMode == ExerciseMode.SQUAT) {
            accelerometer?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
        }
        
        updateDisplays()
        saveData()
    }

    private fun stopPeriod() {
        isTraining = false
        val endTime = System.currentTimeMillis()
        val duration = endTime - periodStartTime
        
        // センサーを停止
        sensorManager.unregisterListener(this)
        
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
        updateModeDisplay()
    }
    
    private fun updateModeDisplay() {
        Log.d("MainActivity", "updateModeDisplay called with mode: ${currentMode.displayName}")
        currentModeTextView.text = "現在のモード: ${currentMode.displayName}"
        
        // モードボタンの状態を更新
        pushupModeButton.isEnabled = !isTraining
        squatModeButton.isEnabled = !isTraining
        
        // 現在選択されているモードをハイライト
        when (currentMode) {
            ExerciseMode.PUSHUP -> {
                pushupModeButton.isSelected = true
                squatModeButton.isSelected = false
                counterButton.text = "TAP"
            }
            ExerciseMode.SQUAT -> {
                pushupModeButton.isSelected = false
                squatModeButton.isSelected = true
                counterButton.text = "センサー検出中"
            }
        }
        Log.d("MainActivity", "Mode display updated")
    }

    private fun updateCounterDisplay() {
        val displayCount = if (currentMode == ExerciseMode.SQUAT) {
            counter / 2
        } else {
            counter
        }
        counterTextView.text = displayCount.toString()
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
            val displayCount = if (currentMode == ExerciseMode.SQUAT) {
                latestRecord.count / 2
            } else {
                latestRecord.count
            }
            historyTextView.text = "最新記録: P${latestRecord.periodNumber}: ${displayCount}回 (${latestRecord.getFormattedDuration()})"
        }
    }
    
    private fun saveData() {
        val recordsKey = when (currentMode) {
            ExerciseMode.PUSHUP -> KEY_PERIOD_RECORDS_PUSHUP
            ExerciseMode.SQUAT -> KEY_PERIOD_RECORDS_SQUAT
        }
        
        with(sharedPreferences.edit()) {
            putInt(KEY_COUNTER, counter)
            putInt(KEY_CURRENT_PERIOD, currentPeriod)
            putBoolean(KEY_IS_TRAINING, isTraining)
            putLong(KEY_PERIOD_START_TIME, periodStartTime)
            putString(KEY_CURRENT_MODE, currentMode.name)
            putString(recordsKey, gson.toJson(periodRecords))
            apply()
        }
    }
    
    private fun loadData() {
        // 既存データをクリアして新しいデータ形式で開始
        // clearOldData() // 一時的にコメントアウト
        
        // 保存されたモードを読み込み
        val savedMode = sharedPreferences.getString(KEY_CURRENT_MODE, ExerciseMode.PUSHUP.name)
        currentMode = try {
            ExerciseMode.valueOf(savedMode ?: ExerciseMode.PUSHUP.name)
        } catch (e: IllegalArgumentException) {
            ExerciseMode.PUSHUP
        }
        
        counter = sharedPreferences.getInt(KEY_COUNTER, 0)
        currentPeriod = sharedPreferences.getInt(KEY_CURRENT_PERIOD, 0)
        isTraining = sharedPreferences.getBoolean(KEY_IS_TRAINING, false)
        periodStartTime = sharedPreferences.getLong(KEY_PERIOD_START_TIME, 0L)
        
        // 現在のモードに応じた記録を読み込み
        val recordsKey = when (currentMode) {
            ExerciseMode.PUSHUP -> KEY_PERIOD_RECORDS_PUSHUP
            ExerciseMode.SQUAT -> KEY_PERIOD_RECORDS_SQUAT
        }
        
        val recordsJson = sharedPreferences.getString(recordsKey, "[]")
        val type = object : TypeToken<MutableList<PeriodRecord>>() {}.type
        val loadedRecords: MutableList<PeriodRecord> = gson.fromJson(recordsJson, type) ?: mutableListOf()
        periodRecords.clear()
        periodRecords.addAll(loadedRecords)
        
        startButton.isEnabled = !isTraining
        stopButton.isEnabled = isTraining
        counterButton.isEnabled = isTraining && currentMode == ExerciseMode.PUSHUP
    }
    
    private fun clearOldData() {
        // 一度だけ実行するためのフラグ
        val hasCleared = sharedPreferences.getBoolean("data_cleared_v3", false)
        if (!hasCleared) {
            with(sharedPreferences.edit()) {
                // 既存データをクリア（古いキー名を使用）
                remove("period_records")
                remove(KEY_COUNTER)
                remove(KEY_CURRENT_PERIOD)
                remove(KEY_IS_TRAINING)
                remove(KEY_PERIOD_START_TIME)
                // クリア済みフラグを設定
                putBoolean("data_cleared_v3", true)
                apply()
            }
        }
    }
}