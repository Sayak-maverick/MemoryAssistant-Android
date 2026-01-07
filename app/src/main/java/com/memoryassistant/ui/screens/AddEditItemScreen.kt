package com.memoryassistant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.memoryassistant.data.models.Item
import com.memoryassistant.data.repository.ItemRepository
import kotlinx.coroutines.launch

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
 */
@OptIn(ExperimentalMaterial3Api::class)
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
    var isLoading by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    /**
     * Track if we're in edit mode
     */
    val isEditMode = itemId != null

    /**
     * Coroutine scope for async operations
     */
    val coroutineScope = rememberCoroutineScope()

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
                                            labels = labelsList
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
                                    labels = labelsList
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
