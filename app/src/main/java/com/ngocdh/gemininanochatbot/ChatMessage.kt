package com.ngocdh.gemininanochatbot


import java.util.UUID

/**
 * Phân loại người gửi tin nhắn
 */
enum class Sender {
    USER,    // Tin nhắn do người dùng nhập
    GEMINI,  // Tin nhắn do Gemini Nano phản hồi
    SYSTEM   // Tin nhắn thông báo hệ thống (như trạng thái AICore)
}

/**
 * Data class biểu diễn một tin nhắn trong khung chat
 */
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val sender: Sender,
    val isPending: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)