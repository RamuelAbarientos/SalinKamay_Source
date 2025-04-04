package com.example.salinkamay

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatMessage(
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFromServer: Boolean = false // New flag to identify server messages
) {
    fun getFormattedTime(): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}