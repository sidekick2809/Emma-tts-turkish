package com.emma.tts.ui.screens

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emma.tts.data.PreferencesManager
import com.emma.tts.service.NativeTtsManager
import com.emma.tts.service.TtsStatus
import com.emma.tts.ui.components.WaveformVisualizer
import com.emma.tts.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    currentText: String,
    onTextChanged: (String) -> Unit,
    ttsManager: NativeTtsManager,
    preferencesManager: PreferencesManager,
    onSpeak: (String) -> Unit
) {
    val context = LocalContext.current

    val ttsStatus by ttsManager.status.collectAsState()
    val currentSpeed by ttsManager.currentSpeed.collectAsState()
    val currentPitch by ttsManager.currentPitch.collectAsState()
    val availableVoices by ttsManager.availableVoices.collectAsState()
    val selectedVoice by ttsManager.selectedVoice.collectAsState()
    val errorMessage by ttsManager.errorMessage.collectAsState()

    var showSettingsSheet by remember { mutableStateOf(false) }
    var showHistorySheet by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    var autoPlayState by remember { mutableStateOf(preferencesManager.autoPlayOnShare) }
    var historyItems by remember { mutableStateOf(preferencesManager.getHistory()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(EmmaAccent, EmmaPrimary)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Emma",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = EmmaTextPrimary
                            )
                            Text(
                                text = "Türkçe TTS • Çevrimdışı & Cihaz İçi",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmmaTeal
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        historyItems = preferencesManager.getHistory()
                        showHistorySheet = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Geçmiş",
                            tint = EmmaTextPrimary
                        )
                    }
                    IconButton(onClick = {
                        showSettingsSheet = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Ayarlar",
                            tint = EmmaTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = EmmaBackground
                )
            )
        },
        containerColor = EmmaBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Offline Status Badge Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(EmmaSurface)
                    .clickable { showInfoDialog = true }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                when (ttsStatus) {
                                    TtsStatus.SPEAKING -> EmmaAccent
                                    TtsStatus.ERROR -> EmmaError
                                    else -> EmmaSuccess
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (ttsStatus) {
                            TtsStatus.SPEAKING -> "Seslendiriliyor (Yerel Motor)"
                            TtsStatus.PAUSED -> "Duraklatıldı"
                            TtsStatus.ERROR -> "Hata Oluştu"
                            TtsStatus.INITIALIZING -> "Motor Başlatılıyor…"
                            else -> "⚡ Sunucusuz Mod (%100 Cihaz İçi)"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = EmmaTextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Bilgi",
                    tint = EmmaTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Text Input / Shared Text Display Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = EmmaSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Seslendirilecek Metin",
                            style = MaterialTheme.typography.labelLarge,
                            color = EmmaTextSecondary,
                            fontWeight = FontWeight.Medium
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Paste Button
                            TextButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = clipboard.primaryClip
                                    if (clip != null && clip.itemCount > 0) {
                                        val pasteText = clip.getItemAt(0).text?.toString() ?: ""
                                        if (pasteText.isNotBlank()) {
                                            onTextChanged(pasteText)
                                        }
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Yapıştır", fontSize = 12.sp)
                            }

                            // Clear Button
                            if (currentText.isNotEmpty()) {
                                TextButton(
                                    onClick = {
                                        ttsManager.stop()
                                        onTextChanged("")
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Temizle", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = currentText,
                        onValueChange = onTextChanged,
                        placeholder = {
                            Text(
                                "Herhangi bir uygulamadan metin seçip Emma ile paylaşın veya buraya bir metin yazın…",
                                color = EmmaTextSecondary.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmmaPrimary,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = EmmaTextPrimary,
                            unfocusedTextColor = EmmaTextPrimary,
                            cursorColor = EmmaAccent
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp, lineHeight = 22.sp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "${currentText.length} karakter",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmmaTextSecondary
                        )
                    }
                }
            }

            // Error Display if any
            if (errorMessage != null) {
                Surface(
                    color = EmmaError.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = EmmaError)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = EmmaError,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { ttsManager.openSystemTtsSettings(context) }) {
                            Icon(Icons.Default.Settings, contentDescription = "Ayarlar", tint = EmmaError)
                        }
                    }
                }
            }

            // Modern Player Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = EmmaSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Waveform Animation (Animates while speaking)
                    WaveformVisualizer(
                        isPlaying = ttsStatus == TtsStatus.SPEAKING,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Big Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Stop Button
                        IconButton(
                            onClick = { ttsManager.stop() },
                            enabled = ttsStatus == TtsStatus.SPEAKING || ttsStatus == TtsStatus.PAUSED
                        ) {
                            Icon(
                                Icons.Default.Stop,
                                contentDescription = "Durdur",
                                tint = if (ttsStatus == TtsStatus.SPEAKING || ttsStatus == TtsStatus.PAUSED)
                                    EmmaTextPrimary else EmmaTextSecondary.copy(alpha = 0.3f),
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        // Main Big Play/Speak Button
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(EmmaAccent, EmmaPrimary)
                                    )
                                )
                                .clickable {
                                    if (currentText.isNotBlank()) {
                                        ttsManager.togglePlayPause(currentText)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            when (ttsStatus) {
                                TtsStatus.INITIALIZING -> {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(32.dp),
                                        strokeWidth = 3.dp
                                    )
                                }
                                TtsStatus.SPEAKING -> {
                                    Icon(
                                        Icons.Default.Pause,
                                        contentDescription = "Duraklat",
                                        tint = Color.White,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                else -> {
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        contentDescription = "Seslendir",
                                        tint = Color.White,
                                        modifier = Modifier.size(38.dp)
                                    )
                                }
                            }
                        }

                        // Replay Button (Speak again from top)
                        IconButton(
                            onClick = {
                                if (currentText.isNotBlank()) {
                                    ttsManager.speak(currentText)
                                }
                            },
                            enabled = currentText.isNotBlank() && ttsStatus != TtsStatus.INITIALIZING
                        ) {
                            Icon(
                                Icons.Default.Replay,
                                contentDescription = "Tekrar Seslendir",
                                tint = if (currentText.isNotBlank()) EmmaTextPrimary else EmmaTextSecondary.copy(alpha = 0.3f),
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Speed Selector Chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(0.75f, 1.0f, 1.25f, 1.5f).forEach { speed ->
                            val isSelected = kotlin.math.abs(currentSpeed - speed) < 0.05f
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    ttsManager.setSpeed(speed)
                                    preferencesManager.speechSpeed = speed
                                },
                                label = { Text("${speed}x", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmmaPrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = EmmaSurfaceVariant,
                                    labelColor = EmmaTextSecondary
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    // Info Dialog
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = {
                Text(
                    text = "Emma Çevrimdışı TTS",
                    color = EmmaTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "• Sunucu Gerekmez: Uygulama telefonunuzun dahili donanımını kullanarak sesi tamamen cihaz üzerinde üretir.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = EmmaTextSecondary
                    )
                    Text(
                        text = "• %100 Çevrimdışı: Uçak modunda veya internetsiz ortamlarda da kesintisiz çalışır.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = EmmaTextSecondary
                    )
                    Text(
                        text = "• Gizlilik: Okuttuğunuz hiçbir metin veya ses internete veya sunucuya gönderilmez.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = EmmaTextSecondary
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("Anladım", color = EmmaAccent)
                }
            },
            containerColor = EmmaSurface
        )
    }

    // Settings Bottom Sheet
    if (showSettingsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSettingsSheet = false },
            containerColor = EmmaSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Emma Ses & Motor Ayarları",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EmmaTextPrimary
                )

                // Voice Pitch (Ses Tonu) Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Ses Tonu (Pitch)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = EmmaTextPrimary
                        )
                        Text(
                            text = "%.1fx".format(currentPitch),
                            style = MaterialTheme.typography.bodySmall,
                            color = EmmaAccent,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Slider(
                        value = currentPitch,
                        onValueChange = {
                            ttsManager.setPitch(it)
                            preferencesManager.speechPitch = it
                        },
                        valueRange = 0.7f..1.4f,
                        steps = 6,
                        colors = SliderDefaults.colors(
                            thumbColor = EmmaAccent,
                            activeTrackColor = EmmaPrimary,
                            inactiveTrackColor = EmmaSurfaceVariant
                        )
                    )
                }

                // Voice Selector (If device has multiple Turkish voices)
                if (availableVoices.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Türkçe Ses Seçimi",
                            style = MaterialTheme.typography.bodyMedium,
                            color = EmmaTextPrimary
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            availableVoices.take(4).forEach { voiceOption ->
                                val isSelected = selectedVoice == voiceOption.name
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        ttsManager.setVoice(voiceOption.name)
                                        preferencesManager.selectedVoiceName = voiceOption.name
                                    },
                                    label = { Text(voiceOption.displayName, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EmmaPrimary,
                                        selectedLabelColor = Color.White,
                                        containerColor = EmmaSurfaceVariant,
                                        labelColor = EmmaTextSecondary
                                    )
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = EmmaSurfaceVariant)

                // Open System TTS Settings Button
                Button(
                    onClick = { ttsManager.openSystemTtsSettings(context) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = EmmaSurfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Android TTS Sistem Ayarları")
                }
                Text(
                    text = "Google veya Samsung TTS Türkçe yüksek kaliteli ses paketlerini indirmek ve yönetmek için dokunun.",
                    style = MaterialTheme.typography.labelSmall,
                    color = EmmaTextSecondary
                )

                HorizontalDivider(color = EmmaSurfaceVariant)

                // Auto Play Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Paylaşımda Otomatik Oku",
                            style = MaterialTheme.typography.bodyMedium,
                            color = EmmaTextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Metin paylaşıldığında hemen seslendirmeye başla",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmmaTextSecondary
                        )
                    }
                    Switch(
                        checked = autoPlayState,
                        onCheckedChange = {
                            autoPlayState = it
                            preferencesManager.autoPlayOnShare = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = EmmaAccent,
                            checkedTrackColor = EmmaPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // History Bottom Sheet
    if (showHistorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showHistorySheet = false },
            containerColor = EmmaSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Son Paylaşılan Metinler",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmmaTextPrimary
                    )

                    if (historyItems.isNotEmpty()) {
                        TextButton(onClick = {
                            preferencesManager.clearHistory()
                            historyItems = emptyList()
                        }) {
                            Text("Temizle", color = EmmaAccent, fontSize = 12.sp)
                        }
                    }
                }

                if (historyItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Henüz kaydedilmiş metin geçmişi yok.",
                            color = EmmaTextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(historyItems) { itemText ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = EmmaSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onTextChanged(itemText)
                                        showHistorySheet = false
                                        onSpeak(itemText)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.PlayCircle,
                                        contentDescription = null,
                                        tint = EmmaPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = itemText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = EmmaTextPrimary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
