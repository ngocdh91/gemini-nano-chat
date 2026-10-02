package com.ngocdh.gemininanochatbot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.jvm.java

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isInitialized: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ChatViewModel(
    private val repository: GeminiNanoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        initializeModel()
    }

    private fun initializeModel() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.initialize()
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isInitialized = true,
                            isLoading = false,
                            messages = listOf(
                                ChatMessage(
                                    text = "Gemini Nano (On-Device NPU) đã sẵn sàng! Bạn có thể chat offline hoàn toàn.",
                                    sender = Sender.SYSTEM
                                )
                            )
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Không thể khởi tạo AICore: ${error.localizedMessage}"
                        )
                    }
                }
        }
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank() || !_uiState.value.isInitialized || _uiState.value.isLoading) return

        val userMessage = ChatMessage(text = userText, sender = Sender.USER)
        val botPendingMessage = ChatMessage(text = "", sender = Sender.GEMINI, isPending = true)

        _uiState.update { state ->
            state.copy(
                messages = state.messages + userMessage + botPendingMessage,
                isLoading = true
            )
        }

        viewModelScope.launch {
            var accumulatedText = ""
            try {
                repository.generateResponseStream(userText).collect { chunk ->
                    accumulatedText += chunk
                    updateLastBotMessage(accumulatedText, isPending = false)
                }
            } catch (e: Exception) {
                updateLastBotMessage(
                    text = "Lỗi: ${e.localizedMessage ?: "Không thể tạo phản hồi."}",
                    isPending = false
                )
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun updateLastBotMessage(text: String, isPending: Boolean) {
        _uiState.update { state ->
            val updatedList = state.messages.toMutableList()
            val lastIndex = updatedList.indexOfLast { it.sender == Sender.GEMINI }
            if (lastIndex != -1) {
                updatedList[lastIndex] = updatedList[lastIndex].copy(
                    text = text,
                    isPending = isPending
                )
            }
            state.copy(messages = updatedList)
        }
    }

    class Factory(private val repository: GeminiNanoRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
                return ChatViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}