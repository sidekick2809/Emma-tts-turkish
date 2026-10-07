package com.emma.tts.service

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID

enum class TtsStatus {
    INITIALIZING,
    READY,
    SPEAKING,
    PAUSED,
    COMPLETED,
    ERROR
}

data class VoiceOption(
    val name: String,
    val displayName: String,
    val isNetworkConnectionRequired: Boolean
)

class NativeTtsManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null

    private val _status = MutableStateFlow(TtsStatus.INITIALIZING)
    val status: StateFlow<TtsStatus> = _status.asStateFlow()

    private val _currentSpeed = MutableStateFlow(1.0f)
    val currentSpeed: StateFlow<Float> = _currentSpeed.asStateFlow()

    private val _currentPitch = MutableStateFlow(1.0f)
    val currentPitch: StateFlow<Float> = _currentPitch.asStateFlow()

    private val _availableVoices = MutableStateFlow<List<VoiceOption>>(emptyList())
    val availableVoices: StateFlow<List<VoiceOption>> = _availableVoices.asStateFlow()

    private val _selectedVoice = MutableStateFlow<String?>(null)
    val selectedVoice: StateFlow<String?> = _selectedVoice.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _currentUtteranceId = MutableStateFlow<String?>(null)

    // Keeps track of the text being spoken
    private var lastSpokenText: String = ""
    private var currentTextPosition: Int = 0

    init {
        initTts()
    }

    private fun initTts() {
        _status.value = TtsStatus.INITIALIZING
        _errorMessage.value = null
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            _status.value = TtsStatus.ERROR
            _errorMessage.value = "TTS başlatılamadı: ${e.localizedMessage}"
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val turkishLocale = Locale("tr", "TR")
            val result = tts?.setLanguage(turkishLocale) ?: TextToSpeech.LANG_NOT_SUPPORTED

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to generic Turkish if TR country is missing
                val fallbackResult = tts?.setLanguage(Locale("tr")) ?: TextToSpeech.LANG_NOT_SUPPORTED
                if (fallbackResult == TextToSpeech.LANG_MISSING_DATA || fallbackResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    _status.value = TtsStatus.ERROR
                    _errorMessage.value = "Cihazınızda Türkçe ses paketi eksik veya desteklenmiyor. Ayarlardan Türkçe TTS paketini indirebilirsiniz."
                    loadAvailableVoices()
                    return
                }
            }

            tts?.setSpeechRate(_currentSpeed.value)
            tts?.setPitch(_currentPitch.value)

            setupProgressListener()
            loadAvailableVoices()

            _status.value = TtsStatus.READY
            _errorMessage.value = null
        } else {
            _status.value = TtsStatus.ERROR
            _errorMessage.value = "Android Metin Okuma (TTS) motoru başlatılamadı ($status)."
        }
    }

    private fun setupProgressListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _status.value = TtsStatus.SPEAKING
                _errorMessage.value = null
            }

            override fun onDone(utteranceId: String?) {
                if (_currentUtteranceId.value == utteranceId) {
                    _status.value = TtsStatus.COMPLETED
                    currentTextPosition = 0
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                if (_currentUtteranceId.value == utteranceId) {
                    _status.value = TtsStatus.ERROR
                    _errorMessage.value = "Metin seslendirilirken bir hata oluştu."
                }
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                if (_currentUtteranceId.value == utteranceId) {
                    _status.value = TtsStatus.ERROR
                    _errorMessage.value = "Seslendirme hatası (Kod: $errorCode)"
                }
            }

            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                currentTextPosition = start
            }
        })
    }

    private fun loadAvailableVoices() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                val voices = tts?.voices?.filter { voice ->
                    val lang = voice.locale.language.lowercase()
                    lang == "tr"
                }?.mapIndexed { index, voice ->
                    val quality = if (voice.quality >= Voice.QUALITY_VERY_HIGH) "Yüksek Kalite" else "Standart"
                    val label = "Ses ${index + 1} ($quality)"
                    VoiceOption(
                        name = voice.name,
                        displayName = label,
                        isNetworkConnectionRequired = voice.isNetworkConnectionRequired
                    )
                } ?: emptyList()

                _availableVoices.value = voices

                // Restore previously selected voice if valid
                _selectedVoice.value?.let { savedName ->
                    setVoice(savedName)
                }
            } catch (e: Exception) {
                // Ignore voice enumeration issues on custom ROMs
            }
        }
    }

    fun speak(text: String, speed: Float? = null, pitch: Float? = null) {
        val clean = text.trim()
        if (clean.isBlank()) return

        val engine = tts
        if (engine == null) {
            initTts()
            return
        }

        speed?.let { setSpeed(it) }
        pitch?.let { setPitch(it) }

        lastSpokenText = clean
        currentTextPosition = 0

        val utteranceId = UUID.randomUUID().toString()
        _currentUtteranceId.value = utteranceId
        _status.value = TtsStatus.SPEAKING

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }

        // Split long texts into manageable sentences if longer than 3500 chars (safe Android TTS limit)
        if (clean.length > 3500) {
            speakLongText(clean, utteranceId, params)
        } else {
            engine.speak(clean, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        }
    }

    private fun speakLongText(text: String, baseUtteranceId: String, params: Bundle) {
        val chunks = text.chunked(3000)
        chunks.forEachIndexed { index, chunk ->
            val uId = if (index == chunks.size - 1) baseUtteranceId else "${baseUtteranceId}_chunk_$index"
            val queueMode = if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            val chunkParams = Bundle(params).apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, uId)
            }
            tts?.speak(chunk, queueMode, chunkParams, uId)
        }
    }

    fun togglePlayPause(textToSpeak: String) {
        when (_status.value) {
            TtsStatus.SPEAKING -> {
                // Pause by stopping playback and preserving text position
                tts?.stop()
                _status.value = TtsStatus.PAUSED
            }
            TtsStatus.PAUSED -> {
                // Resume from remaining text if available, or replay
                val remainingText = if (currentTextPosition > 0 && currentTextPosition < lastSpokenText.length) {
                    lastSpokenText.substring(currentTextPosition).trim()
                } else {
                    textToSpeak
                }
                speak(if (remainingText.isNotEmpty()) remainingText else textToSpeak)
            }
            else -> {
                speak(textToSpeak)
            }
        }
    }

    fun stop() {
        tts?.stop()
        _status.value = TtsStatus.READY
        currentTextPosition = 0
    }

    fun setSpeed(speed: Float) {
        _currentSpeed.value = speed
        tts?.setSpeechRate(speed)
    }

    fun setPitch(pitch: Float) {
        _currentPitch.value = pitch
        tts?.setPitch(pitch)
    }

    fun setVoice(voiceName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val matching = tts?.voices?.firstOrNull { it.name == voiceName }
            if (matching != null) {
                tts?.voice = matching
                _selectedVoice.value = voiceName
            }
        }
    }

    fun openSystemTtsSettings(context: Context) {
        try {
            val intent = Intent("com.android.settings.TTS_SETTINGS").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent(android.provider.Settings.ACTION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
