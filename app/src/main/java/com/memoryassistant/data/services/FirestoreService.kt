package com.memoryassistant.data.services

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import com.memoryassistant.data.models.Item
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * FirestoreService - Cloud sync service using Firebase Firestore
 *
 * This service handles:
 * 1. Syncing items to Firestore (cloud storage)
 * 2. Real-time synchronization across devices
 * 3. Offline support with automatic sync when online
 * 4. User-specific data isolation
 *
 * How it works:
 * 1. Each user has their own collection: users/{userId}/items
 * 2. Items are synced to Firestore when created/updated/deleted
 * 3. Changes from other devices are listened to in real-time
 * 4. Firestore handles offline caching automatically
 *
 * Architecture:
 * - Local database (Room) = Source of truth for UI
 * - Firestore = Cloud backup and sync across devices
 * - Two-way sync: Local changes → Cloud, Cloud changes → Local
 */
class FirestoreService {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /**
     * Get current user ID
     * All items are stored under users/{userId}/items/{itemId}
     */
    private fun getUserId(): String? {
        return auth.currentUser?.uid
    }

    /**
     * Get reference to user's items collection
     */
    private fun getItemsCollection() = getUserId()?.let { userId ->
        firestore.collection("users").document(userId).collection("items")
    }

    /**
     * Upload/sync a single item to Firestore
     *
     * This is called when:
     * - User creates a new item
     * - User updates an existing item
     *
     * @param item - The item to sync
     */
    suspend fun syncItemToCloud(item: Item) {
        try {
            val collection = getItemsCollection() ?: return

            // Convert Item to Map for Firestore
            val itemData = hashMapOf(
                "id" to item.id,
                "name" to item.name,
                "description" to item.description,
                "createdAt" to item.createdAt,
                "imageUrl" to item.imageUrl,
                "labels" to item.labels,
                "audioUrl" to item.audioUrl,
                "audioTranscription" to item.audioTranscription,
                "latitude" to item.latitude,
                "longitude" to item.longitude,
                "locationName" to item.locationName,
                "updatedAt" to System.currentTimeMillis()  // Track last update
            )

            // Upload to Firestore (creates if not exists, updates if exists)
            collection.document(item.id).set(itemData).await()
        } catch (e: Exception) {
            e.printStackTrace()
            // Sync will retry when network is available (Firestore handles this automatically)
        }
    }

    /**
     * Delete an item from Firestore
     *
     * @param itemId - ID of the item to delete
     */
    suspend fun deleteItemFromCloud(itemId: String) {
        try {
            val collection = getItemsCollection() ?: return
            collection.document(itemId).delete().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Listen to real-time changes from Firestore
     *
     * This creates a Flow that emits whenever items change in Firestore.
     * Use this to sync changes from other devices to the local database.
     *
     * @return Flow of QuerySnapshot containing all items
     */
    fun listenToCloudChanges(): Flow<QuerySnapshot?> = callbackFlow {
        val collection = getItemsCollection()

        if (collection == null) {
            // User not logged in, send null and close
            trySend(null)
            close()
            return@callbackFlow
        }

        // Set up real-time listener
        val listener: ListenerRegistration = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                // Error occurred, but don't close the flow
                // Firestore will automatically retry
                return@addSnapshotListener
            }

            // Send snapshot to flow
            trySend(snapshot)
        }

        // Clean up listener when flow is cancelled
        awaitClose {
            listener.remove()
        }
    }

    /**
     * Sync all local items to cloud
     *
     * This is useful for:
     * - Initial sync after login
     * - Manual sync when user wants to ensure everything is backed up
     *
     * @param items - List of all local items to sync
     */
    suspend fun syncAllItemsToCloud(items: List<Item>) {
        items.forEach { item ->
            syncItemToCloud(item)
        }
    }

    /**
     * Convert Firestore document to Item
     *
     * @param data - Map from Firestore document
     * @return Item object or null if conversion fails
     */
    fun documentToItem(data: Map<String, Any>): Item? {
        return try {
            Item(
                id = data["id"] as? String ?: return null,
                name = data["name"] as? String ?: return null,
                description = data["description"] as? String,
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                imageUrl = data["imageUrl"] as? String,
                labels = (data["labels"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                audioUrl = data["audioUrl"] as? String,
                audioTranscription = data["audioTranscription"] as? String,
                latitude = (data["latitude"] as? Number)?.toDouble(),
                longitude = (data["longitude"] as? Number)?.toDouble(),
                locationName = data["locationName"] as? String
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Check if user is logged in
     */
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    /**
     * Get current user email
     */
    fun getUserEmail(): String? {
        return auth.currentUser?.email
    }
}
