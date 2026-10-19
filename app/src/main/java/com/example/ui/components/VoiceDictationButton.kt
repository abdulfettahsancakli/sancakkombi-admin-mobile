package com.example.ui.components

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

internal fun appendDictationText(currentText: String, transcript: String): String {
    val cleanTranscript = transcript.trim()
    if (cleanTranscript.isBlank()) return currentText
    val cleanCurrent = currentText.trimEnd()
    val separator = when {
        cleanCurrent.isBlank() -> ""
        cleanCurrent.last() in charArrayOf('.', '!', '?', ':', ';') -> " "
        else -> ". "
    }
    return cleanCurrent + separator + cleanTranscript
}

/**
 * Push-to-talk dictation for editable text fields. Android's speech UI makes the
 * active microphone state explicit; only the returned transcript is kept.
 */
@Composable
fun VoiceDictationButton(
    currentText: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Sesle yaz"
) {
    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListening = false
        if (result.resultCode == Activity.RESULT_OK) {
            val transcript = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (transcript.isNullOrBlank()) {
                errorMessage = "Ses anlaşılamadı. Tekrar deneyin veya yazarak devam edin."
            } else {
                errorMessage = null
                onTextChange(appendDictationText(currentText, transcript))
            }
        }
    }

    fun startListening() {
        errorMessage = null
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "tr-TR")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Konuşun; metni kaydetmeden önce düzenleyebilirsiniz")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
        try {
            isListening = true
            speechLauncher.launch(intent)
        } catch (_: ActivityNotFoundException) {
            isListening = false
            errorMessage = "Bu telefonda sesle yazma hizmeti bulunamadı."
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startListening()
        } else {
            errorMessage = "Mikrofon izni verilmedi. Yazarak devam edebilirsiniz."
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalButton(
                onClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                        PackageManager.PERMISSION_GRANTED
                    ) {
                        startListening()
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                enabled = !isListening
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicNone,
                    contentDescription = null
                )
                Text(
                    text = if (isListening) "Dinleniyor…" else label,
                    modifier = Modifier.padding(start = 8.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Text(
            text = errorMessage ?: "Konuşmanız yalnızca yazıya çevrilir; metni kaydetmeden önce düzenleyebilirsiniz.",
            color = if (errorMessage == null) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.error
            },
            fontSize = 11.sp,
            lineHeight = 15.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
