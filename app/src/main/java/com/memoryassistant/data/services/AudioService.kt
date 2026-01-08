package com.memoryassistant.data.services

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.speech.v1.*
import com.google.protobuf.ByteString
import com.memoryassistant.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/**
 * AudioService - Audio recording and speech-to-text service for Android
 *
 * This service handles:
 * 1. Recording audio using MediaRecorder API
 * 2. Saving audio files to app-specific storage
 * 3. Transcribing audio using Google Cloud Speech-to-Text API
 * 4. Playing back audio recordings
 *
 * How it works:
 * 1. User presses record button
 * 2. MediaRecorder captures audio from microphone
 * 3. Audio saved as .3gp file in app's files directory
 * 4. File sent to Speech-to-Text API for transcription
 * 5. API automatically detects language and returns transcript
 */
class AudioService(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var currentAudioFile: File? = null

    /**
     * Start recording audio from microphone
     *
     * @return The file where audio will be saved
     */
    @Throws(IOException::class)
    fun startRecording(): File {
        // Create a unique filename with timestamp
        val fileName = "audio_${System.currentTimeMillis()}.3gp"
        val audioFile = File(context.filesDir, fileName)
        currentAudioFile = audioFile

        // Initialize MediaRecorder
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(audioFile.absolutePath)

            try {
                prepare()
                start()
            } catch (e: IOException) {
                throw IOException("Failed to start recording: ${e.message}")
            }
        }

        return audioFile
    }

    /**
     * Stop recording and return the audio file URI
     *
     * @return URI of the recorded audio file
     */
    fun stopRecording(): Uri? {
        mediaRecorder?.apply {
            try {
                stop()
                release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        mediaRecorder = null

        return currentAudioFile?.let { Uri.fromFile(it) }
    }

    /**
     * Cancel recording and delete the file
     */
    fun cancelRecording() {
        mediaRecorder?.apply {
            try {
                stop()
                release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        mediaRecorder = null

        // Delete the file
        currentAudioFile?.delete()
        currentAudioFile = null
    }

    /**
     * Check if currently recording
     */
    fun isRecording(): Boolean {
        return mediaRecorder != null
    }

    /**
     * Play audio from URI
     *
     * @param audioUri - URI of the audio file to play
     * @param onComplete - Callback when playback completes
     */
    fun playAudio(audioUri: Uri, onComplete: (() -> Unit)? = null) {
        // Stop any existing playback
        stopPlayback()

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, audioUri)
                prepare()
                start()

                // Set completion listener
                setOnCompletionListener {
                    stopPlayback()
                    onComplete?.invoke()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onComplete?.invoke()
        }
    }

    /**
     * Stop audio playback
     */
    fun stopPlayback() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
    }

    /**
     * Check if currently playing audio
     */
    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying == true
    }

    /**
     * Transcribe audio using Google Cloud Speech-to-Text API
     *
     * Multi-language support: API automatically detects the spoken language
     * from a list of common languages.
     *
     * @param audioUri - URI of the audio file to transcribe
     * @return Transcribed text
     */
    suspend fun transcribeAudio(audioUri: Uri): String = withContext(Dispatchers.IO) {
        try {
            // Load credentials from raw resource
            val credentials = context.resources.openRawResource(R.raw.vision_credentials).use {
                GoogleCredentials.fromStream(it)
            }

            // Create Speech API client
            val settings = SpeechSettings.newBuilder()
                .setCredentialsProvider { credentials }
                .build()

            SpeechClient.create(settings).use { speech ->
                // Read audio file bytes
                val audioBytes = readAudioFile(audioUri)

                // Build the audio request
                val audio = RecognitionAudio.newBuilder()
                    .setContent(audioBytes)
                    .build()

                // Configure recognition with multi-language support
                val config = RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.AMR)
                    .setSampleRateHertz(8000)
                    .setLanguageCode("en-US")  // Primary language hint
                    // Alternative languages for automatic detection
                    .addAllAlternativeLanguageCodes(
                        listOf(
                            "es-ES",  // Spanish (Spain)
                            "fr-FR",  // French (France)
                            "de-DE",  // German (Germany)
                            "it-IT",  // Italian (Italy)
                            "pt-BR",  // Portuguese (Brazil)
                            "zh-CN",  // Chinese (Simplified)
                            "ja-JP",  // Japanese
                            "ko-KR",  // Korean
                            "ar-SA",  // Arabic (Saudi Arabia)
                            "hi-IN",  // Hindi (India)
                            "ru-RU",  // Russian
                            "nl-NL",  // Dutch (Netherlands)
                            "pl-PL",  // Polish
                            "tr-TR",  // Turkish
                            "vi-VN",  // Vietnamese
                        )
                    )
                    .setEnableAutomaticPunctuation(true)  // Add punctuation
                    .build()

                // Execute the recognition request
                val response = speech.recognize(config, audio)

                // Extract transcription from response
                val transcription = response.resultsList
                    .joinToString(" ") { result ->
                        result.alternativesList.firstOrNull()?.transcript ?: ""
                    }
                    .trim()

                return@withContext transcription
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Return empty string on error
            return@withContext ""
        }
    }

    /**
     * Read audio file bytes from URI
     */
    private fun readAudioFile(uri: Uri): ByteString {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes() ?: byteArrayOf()
        inputStream?.close()
        return ByteString.copyFrom(bytes)
    }

    /**
     * Release all resources
     * Call this when the service is no longer needed
     */
    fun release() {
        cancelRecording()
        stopPlayback()
    }
}
