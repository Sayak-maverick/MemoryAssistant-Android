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
import androidx.compose.material.icons.filled.PhotoLibrary
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
                                            imageUrl = imageUri?.toString()
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
                                    imageUrl = imageUri?.toString()
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
