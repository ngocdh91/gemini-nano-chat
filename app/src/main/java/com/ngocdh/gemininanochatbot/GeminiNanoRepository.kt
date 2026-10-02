package com.ngocdh.gemininanochatbot
import android.content.Context
import com.google.mlkit.genai.common.FeatureStatus

import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.GenerativeModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GeminiNanoRepository(private val context: Context) {

    private var generativeModel: GenerativeModel? = null

    /**
     * Khởi tạo và kiểm tra trạng thái mô hình Gemini Nano (sử dụng suspend function gốc của SDK)
     */
    suspend fun initialize(): Result<Unit> {
        return runCatching {
            // 1. Khởi tạo client kết nối tới mô hình Gemini Nano
            val model = Generation.getClient()

            // 2. Gọi suspend function checkStatus() trực tiếp
            val status = model.checkStatus()

            when (status) {
                FeatureStatus.AVAILABLE -> {
                    // Mô hình đã sẵn sàng sử dụng
                    generativeModel = model
                }
                FeatureStatus.DOWNLOADABLE -> {
                    // Kích hoạt tải xuống mô hình (suspend function)
                    model.download()
                    generativeModel = model
                }
                FeatureStatus.DOWNLOADING -> {
                    throw IllegalStateException("Mô hình Gemini Nano đang được tải xuống. Vui lòng chờ trong giây lát...")
                }
                FeatureStatus.UNAVAILABLE -> {
                    throw UnsupportedOperationException("Thiết bị này không hỗ trợ mô hình Gemini Nano / AICore.")
                }
                else -> {
                    throw IllegalStateException("Trạng thái mô hình không xác định: $status")
                }
            }
        }
    }

    /**
     * Stream phản hồi từ Gemini Nano qua Kotlin Flow
     */
    fun generateResponseStream(prompt: String): Flow<String> = flow {
        val model = generativeModel
            ?: throw IllegalStateException("Gemini Nano chưa sẵn sàng trên thiết bị.")

        // Thực thi tạo nội dung dạng stream
        model.generateContentStream(prompt).collect { chunk ->
            val textChunk = chunk.candidates
                ?.firstOrNull()
                ?.text
                ?: ""
        }
    }
}