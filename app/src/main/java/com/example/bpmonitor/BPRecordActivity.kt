package com.example.bpmonitor

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.example.bpmonitor.data.BPDatabase
import com.example.bpmonitor.model.BloodPressure
import com.example.bpmonitor.util.DateAxisValueFormatter
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class BPRecordActivity : AppCompatActivity() {
    private lateinit var db: BPDatabase
    private lateinit var chart: LineChart
    private var userId: Long = 0
    private lateinit var progressBar: ProgressBar
    
    companion object {
        private const val SPEECH_REQUEST_CODE = 0
        private const val PERMISSION_REQUEST_CODE = 1
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bp_record)
        
        userId = intent.getLongExtra("userId", -1)
        if (userId == -1L) {
            finish()
            return
        }
        
        db = Room.databaseBuilder(
            applicationContext,
            BPDatabase::class.java,
            "bp_database"
        ).build()
        
        setupChart()
        setupVoiceButton()
        progressBar = findViewById(R.id.progressBar)
        loadBPData()
    }
    
    private fun setupChart() {
        chart = findViewById(R.id.bpChart)
        chart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = DateAxisValueFormatter()
                labelRotationAngle = -45f
                setLabelCount(5, false)
                granularity = 1f
                textSize = 12f
                setDrawGridLines(true)
                setDrawAxisLine(true)
                setAvoidFirstLastClipping(true)
                spaceMin = 0.5f
                spaceMax = 0.5f
                isGranularityEnabled = true
                textColor = ContextCompat.getColor(context, android.R.color.black)
                yOffset = 10f
            }
            
            axisLeft.apply {
                axisMinimum = 40f
                axisMaximum = 200f
                setLabelCount(8, true)
                textSize = 10f
                setDrawGridLines(true)
            }
            
            axisRight.isEnabled = false
            
            legend.apply {
                textSize = 12f
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
                yOffset = 15f
            }

            setExtraOffsets(20f, 20f, 20f, 40f)
        }
    }
    
    private fun setupVoiceButton() {
        findViewById<Button>(R.id.recordButton).setOnClickListener {
            if (checkVoicePermission()) {
                startVoiceRecognition()
            }
        }
    }
    
    private fun checkVoicePermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                PERMISSION_REQUEST_CODE
            )
            return false
        }
        return true
    }
    
    private fun startVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            // 设置语音识别模式为自由形式，不限制语法结构
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            
            // 设置语音识别的语言为中文（中国）
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
            
            // 设置首选语言为中文（中国）
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "zh-CN")
            
            // 设置只返回首选语言的结果
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, true)
            
            // 设置用户看到的提示文本
            putExtra(RecognizerIntent.EXTRA_PROMPT, "请说出血压值，例如：高压120，低压80")
            
            // 设置最短录音时长为5秒，防止用户说话太快被打断
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 10000L)
            
            // 设置完全静音超时时长为1.5秒，超过这个时间没有声音就认为录音结束
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
            
            // 设置可能完成的静音时长为1秒，超过这个时间的静音可能表示用户说完了
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1000L)
            
            // 设置最多返回3个可能的识别结果
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
        
        try {
            // 启动语音识别活动
            startActivityForResult(intent, SPEECH_REQUEST_CODE)
        } catch (e: Exception) {
            // 如果设备不支持语音识别，显示提示信息
            Toast.makeText(this, "您的设备不支持语音识别", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SPEECH_REQUEST_CODE) {
            when (resultCode) {
                RESULT_OK -> {
                    val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    if (results.isNullOrEmpty()) {
                        Toast.makeText(this, "未能识别到语音内容", Toast.LENGTH_SHORT).show()
                    } else {
                        results[0]?.let { processVoiceInput(it) }
                    }
                }
                RESULT_CANCELED -> {
                    Toast.makeText(this, "语音识别已取消", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(this, "语音识别失败，错误码：$resultCode", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun processVoiceInput(input: String) {
        Log.d("VoiceRecognition", "识别结果: $input")
        
        val systolicRegex = "高压\\s*(\\d+)".toRegex()
        val diastolicRegex = "低压\\s*(\\d+)".toRegex()
        
        val systolic = systolicRegex.find(input)?.groupValues?.get(1)?.toIntOrNull()
        val diastolic = diastolicRegex.find(input)?.groupValues?.get(1)?.toIntOrNull()
        
        if (systolic != null && diastolic != null) {
            val bp = BloodPressure(
                userId = userId,
                systolic = systolic,
                diastolic = diastolic,
                measureDate = System.currentTimeMillis(),
                isAbnormal = isAbnormalBP(systolic, diastolic)
            )
            
            lifecycleScope.launch {
                try {
                    db.bloodPressureDao().insertOrUpdateBP(bp)
                    Toast.makeText(this@BPRecordActivity, 
                        "已记录：高压$systolic，低压$diastolic", 
                        Toast.LENGTH_SHORT).show()
                    loadBPData()
                } catch (e: Exception) {
                    Toast.makeText(this@BPRecordActivity, 
                        "保存失败：${e.message}", 
                        Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, 
                "无法识别血压值，请按格式说：\"高压120，低压80\"",
                Toast.LENGTH_LONG).show()
        }
    }
    
    private fun isAbnormalBP(systolic: Int, diastolic: Int): Boolean {
        return systolic > BloodPressure.SYSTOLIC_MAX ||
                systolic < BloodPressure.SYSTOLIC_MIN ||
                diastolic > BloodPressure.DIASTOLIC_MAX ||
                diastolic < BloodPressure.DIASTOLIC_MIN
    }
    
    private fun loadBPData() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                db.bloodPressureDao().getUserBPRecords(userId).collect { records ->
                    progressBar.visibility = View.GONE
                    updateChart(records)
                }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    Log.e("BPRecordActivity", "加载数据失败", e)
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@BPRecordActivity, 
                        "加载数据失败：${e.message}", 
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    
    private fun updateChart(records: List<BloodPressure>) {
        if (records.isEmpty()) {
            chart.clear()
            chart.invalidate()
            return
        }

        val sortedRecords = records.sortedBy { it.measureDate }
        
        val systolicEntries = sortedRecords.map { bp ->
            Entry(bp.measureDate.toFloat(), bp.systolic.toFloat())
        }
        val diastolicEntries = sortedRecords.map { bp ->
            Entry(bp.measureDate.toFloat(), bp.diastolic.toFloat())
        }
        
        val systolicDataSet = LineDataSet(systolicEntries, "高压").apply {
            color = ContextCompat.getColor(this@BPRecordActivity, R.color.systolic_color)
            setCircleColor(color)
            valueTextSize = 12f
            lineWidth = 2f
            circleRadius = 4f
            
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return if (value > 140 || value < 80) {
                        setValueTextColors(listOf(Color.RED))
                        "⚠️$value"
                    } else {
                        setValueTextColors(listOf(Color.BLACK))
                        value.toInt().toString()
                    }
                }
            }
        }
        
        val diastolicDataSet = LineDataSet(diastolicEntries, "低压").apply {
            color = ContextCompat.getColor(this@BPRecordActivity, R.color.diastolic_color)
            setCircleColor(color)
            valueTextSize = 12f
            lineWidth = 2f
            circleRadius = 4f
            
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return if (value > 90 || value < 60) {
                        setValueTextColors(listOf(Color.RED))
                        "⚠️$value"
                    } else {
                        setValueTextColors(listOf(Color.BLACK))
                        value.toInt().toString()
                    }
                }
            }
        }
        
        // 定义颜色常量
        val systolicLimitColor = Color.rgb(255, 69, 0)  // 红橙色用于高压
        val diastolicLimitColor = Color.rgb(0, 0, 255)  // 蓝色用于低压
        
        val limitLines = listOf(
            LimitLine(140f, "高压上限").apply {
                lineWidth = 1f
                lineColor = systolicLimitColor
                labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
                textSize = 10f
                textColor = systolicLimitColor  // 文字也使用相同颜色
                enableDashedLine(10f, 5f, 0f)
            },
            LimitLine(80f, "高压下限").apply {
                lineWidth = 1f
                lineColor = systolicLimitColor
                labelPosition = LimitLine.LimitLabelPosition.RIGHT_BOTTOM
                textSize = 10f
                textColor = systolicLimitColor
                enableDashedLine(10f, 5f, 0f)
            },
            LimitLine(90f, "低压上限").apply {
                lineWidth = 1f
                lineColor = diastolicLimitColor
                labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
                textSize = 10f
                textColor = diastolicLimitColor
                enableDashedLine(10f, 5f, 0f)
            },
            LimitLine(60f, "低压下限").apply {
                lineWidth = 1f
                lineColor = diastolicLimitColor
                labelPosition = LimitLine.LimitLabelPosition.RIGHT_BOTTOM
                textSize = 10f
                textColor = diastolicLimitColor
                enableDashedLine(10f, 5f, 0f)
            }
        )
        
        chart.axisLeft.removeAllLimitLines()
        limitLines.forEach { chart.axisLeft.addLimitLine(it) }
        
        val lineData = LineData(systolicDataSet, diastolicDataSet)
        chart.data = lineData
        
        chart.xAxis.apply {
            val minTime = sortedRecords.minOf { it.measureDate }.toFloat()
            val maxTime = sortedRecords.maxOf { it.measureDate }.toFloat()
            axisMinimum = minTime
            axisMaximum = maxTime
        }
        
        chart.notifyDataSetChanged()
        chart.invalidate()
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startVoiceRecognition()
            } else {
                Toast.makeText(this, "需要录音权限才能使用语音输入功能", Toast.LENGTH_SHORT).show()
            }
        }
    }
} 