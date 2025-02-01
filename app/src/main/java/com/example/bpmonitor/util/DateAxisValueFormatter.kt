package com.example.bpmonitor.util

import android.graphics.Color
import android.util.Log
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*

class DateAxisValueFormatter : ValueFormatter() {
    // 只显示日期，不显示时间
    private val dateFormat = SimpleDateFormat("MM-dd", Locale.getDefault())
    
    override fun getFormattedValue(value: Float): String {
        try {
            val timestamp = value.toLong()
            val formatted = dateFormat.format(Date(timestamp))
            Log.d("DateFormatter", "时间戳: $timestamp, 格式化后: $formatted")
            return formatted
        } catch (e: Exception) {
            Log.e("DateFormatter", "格式化失败: ${e.message}, 值: $value")
            return ""
        }
    }
} 