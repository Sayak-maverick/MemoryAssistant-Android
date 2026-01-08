package com.memoryassistant.data.services

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.vision.v1.*
import com.google.protobuf.ByteString
import com.memoryassistant.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/**
 * VisionService - Google Cloud Vision API integration for Android
 *
 * This service detects objects in images and returns labels
 * for automatic tagging of items.
 *
 * How it works:
 * 1. User captures/selects an image
 * 2. Image is converted to bytes
 * 3. Sent to Google Vision API
 * 4. API returns detected objects with confidence scores
 * 5. We filter and return top labels
 */
class VisionService(private val context: Context) {

    /**
     * Detect labels in an image using Google Cloud Vision API
     *
     * @param imageUri - URI of the image to analyze
     * @return List of detected labels sorted by confidence
     */
    suspend fun detectLabels(imageUri: Uri): List<String> = withContext(Dispatchers.IO) {
        try {
            // Load credentials from raw resource
            val credentials = context.resources.openRawResource(R.raw.vision_credentials).use {
                GoogleCredentials.fromStream(it)
            }

            // Create Vision API client
            val settings = ImageAnnotatorSettings.newBuilder()
                .setCredentialsProvider { credentials }
                .build()

            ImageAnnotatorClient.create(settings).use { vision ->
                // Convert URI to bytes
                val imageBytes = uriToByteString(imageUri)

                // Build the image request
                val img = Image.newBuilder().setContent(imageBytes).build()

                // Configure label detection
                val feat = Feature.newBuilder()
                    .setType(Feature.Type.LABEL_DETECTION)
                    .setMaxResults(10)
                    .build()

                val request = AnnotateImageRequest.newBuilder()
                    .addFeatures(feat)
                    .setImage(img)
                    .build()

                // Execute the request
                val response = vision.batchAnnotateImages(listOf(request))
                val annotations = response.responsesList[0]

                // Extract labels
                val labels = annotations.labelAnnotationsList
                    .filter { it.score > 0.7f }  // Filter by confidence > 70%
                    .map { it.description.lowercase() }
                    .take(5)  // Take top 5 labels

                return@withContext labels
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Return empty list on error
            return@withContext emptyList<String>()
        }
    }

    /**
     * Convert image URI to ByteString for Vision API
     */
    private fun uriToByteString(uri: Uri): ByteString {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)

        // Resize bitmap if too large (save bandwidth and processing time)
        val maxDimension = 1024
        val resizedBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
            val ratio = maxDimension.toFloat() / maxOf(bitmap.width, bitmap.height)
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * ratio).toInt(),
                (bitmap.height * ratio).toInt(),
                true
            )
        } else {
            bitmap
        }

        // Convert to JPEG bytes
        val outputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        val imageBytes = outputStream.toByteArray()

        return ByteString.copyFrom(imageBytes)
    }

    /**
     * Get suggested item name from detected labels
     *
     * This tries to find the most relevant label for the item name.
     * Priority: specific objects > generic objects
     */
    fun suggestItemName(labels: List<String>): String {
        if (labels.isEmpty()) return ""

        // Priority words that make good item names
        val priorityWords = listOf(
            "key", "wallet", "phone", "glasses", "watch",
            "bag", "backpack", "book", "headphone", "charger",
            "laptop", "tablet", "mouse", "keyboard", "camera"
        )

        // Find first label that matches priority words
        for (label in labels) {
            for (word in priorityWords) {
                if (label.contains(word)) {
                    // Capitalize first letter
                    return label.replaceFirstChar { it.uppercase() }
                }
            }
        }

        // If no priority match, use first label
        return labels[0].replaceFirstChar { it.uppercase() }
    }
}
