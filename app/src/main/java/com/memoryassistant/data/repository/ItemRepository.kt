package com.memoryassistant.data.repository

import com.memoryassistant.data.database.ItemDao
import com.memoryassistant.data.models.Item
import com.memoryassistant.data.services.FirestoreService
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * ItemRepository - Central place for managing Item data
 *
 * Repository Pattern - A design pattern that:
 * 1. Abstracts data sources (database, network, cache)
 * 2. Provides a clean API for the UI layer
 * 3. Handles data logic (like generating IDs)
 *
 * Why use a Repository?
 * - UI doesn't need to know where data comes from (database, network, etc.)
 * - Easy to add new data sources later (e.g., cloud sync)
 * - Centralizes data logic
 * - Makes testing easier
 *
 * Think of it as a "manager" that handles all Item-related data operations.
 *
 * NEW in Step 10: Cloud Sync
 * - Automatically syncs items to Firebase Firestore
 * - Local database (Room) remains the source of truth
 * - Cloud acts as backup and cross-device sync
 */
class ItemRepository(
    private val itemDao: ItemDao,
    private val firestoreService: FirestoreService = FirestoreService()
) {

    /**
     * Get all items from the database
     *
     * @return Flow<List<Item>> - Live list that updates automatically
     *
     * The UI can observe this Flow and automatically update when data changes
     */
    fun getAllItems(): Flow<List<Item>> {
        return itemDao.getAllItems()
    }

    /**
     * Get a single item by ID
     *
     * @param id - The item's unique identifier
     * @return Flow<Item?> - The item, or null if not found
     */
    fun getItemById(id: String): Flow<Item?> {
        return itemDao.getItemById(id)
    }

    /**
     * Add a new item to the database
     *
     * @param name - Item name
     * @param description - Item description (optional)
     * @param imageUrl - Image URL (optional)
     * @param labels - List of labels (optional)
     * @param audioUrl - Audio file URI (optional)
     * @param audioTranscription - Transcribed text from audio (optional)
     * @param latitude - GPS latitude (optional)
     * @param longitude - GPS longitude (optional)
     * @param locationName - Location address (optional)
     *
     * This is a convenience method that:
     * 1. Generates a unique ID
     * 2. Creates the Item object
     * 3. Inserts it into the database
     */
    suspend fun addItem(
        name: String,
        description: String? = null,
        imageUrl: String? = null,
        labels: List<String> = emptyList(),
        audioUrl: String? = null,
        audioTranscription: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        locationName: String? = null
    ) {
        /**
         * Generate a unique ID using UUID
         *
         * UUID (Universally Unique Identifier) - a 128-bit unique value
         * Example: "550e8400-e29b-41d4-a716-446655440000"
         *
         * Why UUID?
         * - Guaranteed to be unique (no collisions)
         * - Can be generated offline (no need for server)
         * - Works great for distributed systems
         */
        val id = UUID.randomUUID().toString()

        val item = Item(
            id = id,
            name = name,
            description = description,
            createdAt = System.currentTimeMillis(),
            imageUrl = imageUrl,
            labels = labels,
            audioUrl = audioUrl,
            audioTranscription = audioTranscription,
            latitude = latitude,
            longitude = longitude,
            locationName = locationName
        )

        itemDao.insertItem(item)

        // Sync to cloud
        firestoreService.syncItemToCloud(item)
    }

    /**
     * Update an existing item
     *
     * @param item - The item with updated data
     */
    suspend fun updateItem(item: Item) {
        itemDao.updateItem(item)

        // Sync to cloud
        firestoreService.syncItemToCloud(item)
    }

    /**
     * Delete an item
     *
     * @param item - The item to delete
     */
    suspend fun deleteItem(item: Item) {
        itemDao.deleteItem(item)

        // Delete from cloud
        firestoreService.deleteItemFromCloud(item.id)
    }

    /**
     * Delete an item by ID
     *
     * @param id - The ID of the item to delete
     */
    suspend fun deleteItemById(id: String) {
        itemDao.deleteItemById(id)

        // Delete from cloud
        firestoreService.deleteItemFromCloud(id)
    }

    /**
     * Delete all items (clear database)
     */
    suspend fun deleteAllItems() {
        itemDao.deleteAllItems()
    }

    /**
     * Search items by name or description
     *
     * @param query - The search text
     * @return Flow<List<Item>> - Matching items
     */
    fun searchItems(query: String): Flow<List<Item>> {
        return itemDao.searchItems(query)
    }

    /**
     * Get items with a specific label
     *
     * @param label - The label to filter by
     * @return Flow<List<Item>> - Items with this label
     */
    fun getItemsByLabel(label: String): Flow<List<Item>> {
        return itemDao.getItemsByLabel(label)
    }

    /**
     * Insert dummy data for testing
     *
     * This is useful for development and testing
     * We'll call this once to populate the database
     */
    suspend fun insertDummyData() {
        val now = System.currentTimeMillis()

        val dummyItems = listOf(
            Item(
                id = UUID.randomUUID().toString(),
                name = "Car Keys",
                description = "Toyota keys with red keychain",
                createdAt = now - 3600000,  // 1 hour ago
                labels = listOf("important", "daily")
            ),
            Item(
                id = UUID.randomUUID().toString(),
                name = "Wallet",
                description = "Brown leather wallet with credit cards",
                createdAt = now - 7200000,  // 2 hours ago
                labels = listOf("important")
            ),
            Item(
                id = UUID.randomUUID().toString(),
                name = "Reading Glasses",
                description = "Black frame reading glasses",
                createdAt = now - 86400000,  // 1 day ago
                labels = listOf("daily")
            ),
            Item(
                id = UUID.randomUUID().toString(),
                name = "Phone Charger",
                description = "USB-C white charger cable",
                createdAt = now - 172800000,  // 2 days ago
                labels = emptyList()
            ),
            Item(
                id = UUID.randomUUID().toString(),
                name = "Headphones",
                description = "Sony wireless headphones",
                createdAt = now - 259200000,  // 3 days ago
                labels = listOf("electronics")
            ),
            Item(
                id = UUID.randomUUID().toString(),
                name = "House Keys",
                description = "Spare keys with blue keychain",
                createdAt = now - 604800000,  // 1 week ago
                labels = listOf("important", "backup")
            ),
            Item(
                id = UUID.randomUUID().toString(),
                name = "Work Badge",
                description = "Office access card",
                createdAt = now - 1209600000,  // 2 weeks ago
                labels = listOf("work", "important")
            ),
            Item(
                id = UUID.randomUUID().toString(),
                name = "Backpack",
                description = "Black Nike backpack",
                createdAt = now - 1814400000,  // 3 weeks ago
                labels = emptyList()
            )
        )

        itemDao.insertAll(dummyItems)
    }
}
