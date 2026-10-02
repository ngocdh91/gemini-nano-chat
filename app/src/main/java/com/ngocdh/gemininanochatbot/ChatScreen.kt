package com.ngocdh.gemininanochatbot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Tự động cuộn xuống cuối khi có tin nhắn mới
    LaunchedEffect(uiState.messages.size, uiState.messages.lastOrNull()?.text) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Gemini Nano Chat", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = if (uiState.isInitialized) "Offline NPU Active" else "Initializing AICore...",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (uiState.isInitialized) Color(0xFF4CAF50) else Color.Gray
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            uiState.errorMessage?.let { error ->
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(uiState.messages, key = { it.id }) { message ->
                    ChatMessageBubble(message = message)
                }
            }

            // Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Nhập tin nhắn offline...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4,
                    enabled = uiState.isInitialized
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    },
                    enabled = uiState.isInitialized && inputText.isNotBlank() && !uiState.isLoading,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (inputText.isNotBlank() && uiState.isInitialized)
                                MaterialTheme.colorScheme.primary
                            else Color.Gray
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Gửi",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ChatMessageBubble(message: ChatMessage) {
    val isUser = message.sender == Sender.USER
    val isSystem = message.sender == Sender.SYSTEM

    val alignment = when {
        isUser -> Alignment.CenterEnd
        isSystem -> Alignment.Center
        else -> Alignment.CenterStart
    }

    val bubbleColor = when {
        isUser -> MaterialTheme.colorScheme.primaryContainer
        isSystem -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.secondaryContainer
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Surface(
            color = bubbleColor,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Box(modifier = Modifier.padding(12.dp)) {
                if (message.isPending && message.text.isEmpty()) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}