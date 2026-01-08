package com.memoryassistant.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.memoryassistant.data.models.Item
import com.memoryassistant.data.repository.ItemRepository
import com.memoryassistant.data.services.AudioService
import com.memoryassistant.data.services.LocationService
import com.memoryassistant.data.services.VisionService
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * AddEditItemScreen - Screen for adding a new item or editing an existing one
 *
 * This screen allows users to:
 * 1. Add a new item (when itemId is null)
 * 2. Edit an existing item (when itemId is provided)
 * 3. Delete an existing item
 *
 * What's new here:
 * - TextField for user input
 * - Saving data to Room database
 * - Navigation callbacks (onBack, onSaved)
 * - Edit vs Add mode
 * - Camera capture and gallery picker
 * - Image display with Coil
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AddEditItemScreen(
    repository: ItemRepository,
    itemId: String? = null,  // null = add mode, non-null = edit mode
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    /**
     * STATE MANAGEMENT
     *
     * These variables hold the form data
     * They're mutable so users can type and see changes
     */
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var labels by remember { mutableStateOf("") }  // Comma-separated string
    var imageUri by remember { mutableStateOf<Uri?>(null) }  // Selected image URI
    var isLoading by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showImageOptionsDialog by remember { mutableStateOf(false) }
    var isDetectingLabels by remember { mutableStateOf(false) }  // AI detection in progress
    var suggestedLabels by remember { mutableStateOf<List<String>>(emptyList()) }  // AI-detected labels
    var audioUri by remember { mutableStateOf<Uri?>(null) }  // Recorded audio URI
    var audioTranscription by remember { mutableStateOf("") }  // Transcribed text
    var isRecording by remember { mutableStateOf(false) }
    var isTranscribing by remember { mutableStateOf(false) }
    var isPlayingAudio by remember { mutableStateOf(false) }
    var latitude by remember { mutableStateOf<Double?>(null) }  // GPS latitude
    var longitude by remember { mutableStateOf<Double?>(null) }  // GPS longitude
    var locationName by remember { mutableStateOf<String?>(null) }  // Location address
    var isFetchingLocation by remember { mutableStateOf(false) }

    /**
     * Track if we're in edit mode
     */
    val isEditMode = itemId != null

    /**
     * Coroutine scope for async operations
     */
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    /**
     * Vision API service for object detection
     */
    val visionService = remember { VisionService(context) }

    /**
     * Audio service for voice notes
     */
    val audioService = remember { AudioService(context) }

    /**
     * Location service for GPS tracking
     */
    val locationService = remember { LocationService(context) }

    /**
     * Detect labels in the selected image
     */
    fun detectLabelsInImage(uri: Uri) {
        coroutineScope.launch {
            isDetectingLabels = true
            try {
                val detectedLabels = visionService.detectLabels(uri)
                suggestedLabels = detectedLabels

                // Auto-suggest item name if empty
                if (name.isEmpty() && detectedLabels.isNotEmpty()) {
                    name = visionService.suggestItemName(detectedLabels)
                }

                // Auto-fill labels if empty
                if (labels.isEmpty() && detectedLabels.isNotEmpty()) {
                    labels = detectedLabels.joinToString(", ")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isDetectingLabels = false
            }
        }
    }

    /**
     * Camera permission state
     */
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    /**
     * Microphone permission state
     */
    val micPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    /**
     * Location permission state
     */
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    /**
     * Create a temporary file for camera capture
     */
    fun createImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(null)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    /**
     * Camera launcher - captures photo and saves to file
     * Now with AI object detection!
     */
    val cameraImageFile = remember { mutableStateOf<File?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraImageFile.value?.let { file ->
                val uri = Uri.fromFile(file)
                imageUri = uri
                // Detect labels using Vision API
                detectLabelsInImage(uri)
            }
        }
        showImageOptionsDialog = false
    }

    /**
     * Gallery launcher - picks photo from gallery
     * Now with AI object detection!
     */
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
            // Detect labels using Vision API
            detectLabelsInImage(it)
        }
        showImageOptionsDialog = false
    }

    /**
     * Load existing item data if in edit mode
     *
     * LaunchedEffect runs when itemId changes
     */
    LaunchedEffect(itemId) {
        if (itemId != null) {
            // Load the item from database
            repository.getItemById(itemId).collect { item ->
                item?.let {
                    name = it.name
                    description = it.description ?: ""
                    labels = it.labels.joinToString(", ")  // Convert list to string
                    imageUri = it.imageUrl?.let { url -> Uri.parse(url) }
                    audioUri = it.audioUrl?.let { url -> Uri.parse(url) }
                    audioTranscription = it.audioTranscription ?: ""
                    latitude = it.latitude
                    longitude = it.longitude
                    locationName = it.locationName
                }
            }
        }
    }

    /**
     * Scaffold with top app bar
     */
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) "Edit Item" else "Add Item"
                    )
                },
                navigationIcon = {
                    // Back button
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Delete button (only in edit mode)
                    if (isEditMode) {
                        IconButton(
                            onClick = { showDeleteDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        /**
         * Form content
         */
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())  // Make scrollable
        ) {
            /**
             * Name field (required)
             */
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Item Name *") },
                placeholder = { Text("e.g., Car Keys, Wallet") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            /**
             * Description field (optional)
             */
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                placeholder = { Text("Add details about this item...") },
                minLines = 3,
                maxLines = 5,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            /**
             * Labels field (optional, comma-separated)
             */
            OutlinedTextField(
                value = labels,
                onValueChange = { labels = it },
                label = { Text("Labels") },
                placeholder = { Text("e.g., important, daily, work") },
                supportingText = { Text("Separate multiple labels with commas") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            /**
             * Photo section
             */
            Text(
                text = "Photo (optional)",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // AI Detection indicator
            if (isDetectingLabels) {
                Text(
                    text = "🤖 AI is analyzing your image...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Suggested labels
            if (suggestedLabels.isNotEmpty() && !isDetectingLabels) {
                Text(
                    text = "✨ AI detected: ${suggestedLabels.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Image preview or add photo button
            if (imageUri != null) {
                // Show image with remove button
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Box {
                        // Display image using Coil
                        Image(
                            painter = rememberAsyncImagePainter(imageUri),
                            contentDescription = "Item photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Remove button overlay
                        IconButton(
                            onClick = {
                                imageUri = null
                                suggestedLabels = emptyList()
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        ) {
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.errorContainer
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove photo",
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                // Show add photo button
                OutlinedButton(
                    onClick = { showImageOptionsDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Photo")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            /**
             * Voice Note section
             */
            Text(
                text = "Voice Note (optional)",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Recording/Transcription status
            if (isRecording) {
                Text(
                    text = "🎙️ Recording...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (isTranscribing) {
                Text(
                    text = "🤖 Transcribing audio...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Audio player or record button
            if (audioUri != null) {
                // Show audio player card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Play/Stop button
                            IconButton(
                                onClick = {
                                    if (isPlayingAudio) {
                                        audioService.stopPlayback()
                                        isPlayingAudio = false
                                    } else {
                                        audioService.playAudio(audioUri!!) {
                                            isPlayingAudio = false
                                        }
                                        isPlayingAudio = true
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isPlayingAudio) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlayingAudio) "Stop" else "Play",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            Text(
                                text = if (isPlayingAudio) "Playing..." else "Audio Note",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )

                            // Delete button
                            IconButton(
                                onClick = {
                                    audioService.stopPlayback()
                                    isPlayingAudio = false
                                    audioUri = null
                                    audioTranscription = ""
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete audio",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        // Show transcription if available
                        if (audioTranscription.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Transcription:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = audioTranscription,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                // Show record button
                OutlinedButton(
                    onClick = {
                        // Check microphone permission
                        if (!micPermissionState.status.isGranted) {
                            micPermissionState.launchPermissionRequest()
                            return@OutlinedButton
                        }

                        if (isRecording) {
                            // Stop recording
                            val recordedUri = audioService.stopRecording()
                            isRecording = false
                            audioUri = recordedUri

                            // Transcribe audio
                            if (recordedUri != null) {
                                coroutineScope.launch {
                                    isTranscribing = true
                                    try {
                                        val transcription = audioService.transcribeAudio(recordedUri)
                                        audioTranscription = transcription
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    } finally {
                                        isTranscribing = false
                                    }
                                }
                            }
                        } else {
                            // Start recording
                            try {
                                audioService.startRecording()
                                isRecording = true
                            } catch (e: Exception) {
                                e.printStackTrace()
                                // TODO: Show error message
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isRecording) "Stop Recording" else "Record Voice Note")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            /**
             * GPS Location section
             */
            Text(
                text = "Location (optional)",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Location fetching status
            if (isFetchingLocation) {
                Text(
                    text = "📍 Getting your location...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Location display or get location button
            if (latitude != null && longitude != null) {
                // Show location card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = locationName ?: "Location saved",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Lat: ${"%.4f".format(latitude)}, Lon: ${"%.4f".format(longitude)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }

                        // Delete button
                        IconButton(
                            onClick = {
                                latitude = null
                                longitude = null
                                locationName = null
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remove location",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            } else {
                // Show get location button
                OutlinedButton(
                    onClick = {
                        // Check location permission
                        if (!locationPermissionState.status.isGranted) {
                            locationPermissionState.launchPermissionRequest()
                            return@OutlinedButton
                        }

                        // Get current location
                        coroutineScope.launch {
                            isFetchingLocation = true
                            try {
                                val locationData = locationService.getCurrentLocation()
                                if (locationData != null) {
                                    latitude = locationData.latitude
                                    longitude = locationData.longitude
                                    locationName = locationData.locationName
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                // TODO: Show error message
                            } finally {
                                isFetchingLocation = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Location")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            /**
             * Save button
             */
            Button(
                onClick = {
                    // Validate input
                    if (name.isBlank()) {
                        // TODO: Show error message
                        return@Button
                    }

                    isLoading = true

                    coroutineScope.launch {
                        try {
                            // Parse labels (split by comma, trim whitespace)
                            val labelsList = if (labels.isBlank()) {
                                emptyList()
                            } else {
                                labels.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            }

                            if (isEditMode && itemId != null) {
                                /**
                                 * EDIT MODE - Update existing item
                                 *
                                 * We need to get the original item first to preserve
                                 * fields like id and createdAt
                                 */
                                repository.getItemById(itemId).collect { originalItem ->
                                    originalItem?.let { item ->
                                        val updatedItem = item.copy(
                                            name = name,
                                            description = description.ifBlank { null },
                                            labels = labelsList,
                                            imageUrl = imageUri?.toString(),
                                            audioUrl = audioUri?.toString(),
                                            audioTranscription = audioTranscription.ifBlank { null },
                                            latitude = latitude,
                                            longitude = longitude,
                                            locationName = locationName
                                        )
                                        repository.updateItem(updatedItem)

                                        // Navigate back
                                        isLoading = false
                                        onSaved()
                                    }
                                }
                            } else {
                                /**
                                 * ADD MODE - Create new item
                                 */
                                repository.addItem(
                                    name = name,
                                    description = description.ifBlank { null },
                                    labels = labelsList,
                                    imageUrl = imageUri?.toString(),
                                    audioUrl = audioUri?.toString(),
                                    audioTranscription = audioTranscription.ifBlank { null },
                                    latitude = latitude,
                                    longitude = longitude,
                                    locationName = locationName
                                )

                                // Navigate back
                                isLoading = false
                                onSaved()
                            }
                        } catch (e: Exception) {
                            // Handle error
                            isLoading = false
                            // TODO: Show error message
                        }
                    }
                },
                enabled = !isLoading && name.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(text = if (isEditMode) "Save Changes" else "Add Item")
                }
            }
        }
    }

    /**
     * Image options dialog - choose camera or gallery
     */
    if (showImageOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showImageOptionsDialog = false },
            title = { Text("Add Photo") },
            text = {
                Column {
                    // Camera option
                    TextButton(
                        onClick = {
                            // Check camera permission
                            if (cameraPermissionState.status.isGranted) {
                                // Permission granted, launch camera
                                val file = createImageFile(context)
                                cameraImageFile.value = file
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.provider",
                                    file
                                )
                                cameraLauncher.launch(uri)
                            } else {
                                // Request permission
                                cameraPermissionState.launchPermissionRequest()
                                showImageOptionsDialog = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Take Photo", modifier = Modifier.weight(1f))
                    }

                    // Gallery option
                    TextButton(
                        onClick = {
                            galleryLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Choose from Gallery", modifier = Modifier.weight(1f))
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showImageOptionsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    /**
     * Delete confirmation dialog
     */
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Item?") },
            text = { Text("Are you sure you want to delete \"$name\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            if (itemId != null) {
                                repository.deleteItemById(itemId)
                                showDeleteDialog = false
                                onSaved()  // Go back after delete
                            }
                        }
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
