package com.emma.tts

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.emma.tts.data.PreferencesManager
import com.emma.tts.service.NativeTtsManager
import com.emma.tts.ui.screens.MainScreen
import com.emma.tts.ui.theme.EmmaBackground
import com.emma.tts.ui.theme.EmmaTheme

class MainActivity : ComponentActivity() {

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var nativeTtsManager: NativeTtsManager

    private var currentTextState = mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferencesManager = PreferencesManager(this)
        nativeTtsManager = NativeTtsManager(this)

        // Apply saved speed and pitch
        nativeTtsManager.setSpeed(preferencesManager.speechSpeed)
        nativeTtsManager.setPitch(preferencesManager.speechPitch)
        preferencesManager.selectedVoiceName?.let {
            nativeTtsManager.setVoice(it)
        }

        // Handle text shared from another app (Share Sheet or Text Selection Menu)
        handleIntent(intent)

        setContent {
            EmmaTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = EmmaBackground
                ) {
                    var text by remember { currentTextState }

                    MainScreen(
                        currentText = text,
                        onTextChanged = { text = it },
                        ttsManager = nativeTtsManager,
                        preferencesManager = preferencesManager,
                        onSpeak = { textToRead ->
                            speakText(textToRead)
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent?.let { handleIntent(it) }
    }

    private fun handleIntent(intent: Intent) {
        val action = intent.action
        val type = intent.type

        var extractedText: String? = null

        // 1. Android Share Sheet: ACTION_SEND with text/plain
        if (Intent.ACTION_SEND == action && type != null) {
            if ("text/plain" == type || type.startsWith("text/")) {
                extractedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            }
        }
        // 2. Android Highlight Context Menu: ACTION_PROCESS_TEXT ("Emma ile Oku")
        else if (Intent.ACTION_PROCESS_TEXT == action && type != null) {
            if ("text/plain" == type || type.startsWith("text/")) {
                extractedText = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()
            }
        }

        extractedText?.let { text ->
            val clean = text.trim()
            if (clean.isNotEmpty()) {
                currentTextState.value = clean
                preferencesManager.addToHistory(clean)

                if (preferencesManager.autoPlayOnShare) {
                    speakText(clean)
                }
            }
        }
    }

    private fun speakText(textToRead: String) {
        val clean = textToRead.trim()
        if (clean.isEmpty()) {
            Toast.makeText(this, "Seslendirilecek metin boş.", Toast.LENGTH_SHORT).show()
            return
        }

        nativeTtsManager.speak(
            text = clean,
            speed = preferencesManager.speechSpeed,
            pitch = preferencesManager.speechPitch
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        nativeTtsManager.shutdown()
    }
}
