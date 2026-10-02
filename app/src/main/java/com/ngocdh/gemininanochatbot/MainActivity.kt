package com.ngocdh.gemininanochatbot
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface

class MainActivity : ComponentActivity() {

    private val repository by lazy { GeminiNanoRepository(applicationContext) }
    private val viewModel: ChatViewModel by viewModels {
        ChatViewModel.Factory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    ChatScreen(viewModel = viewModel)
                }
            }
        }
    }
}