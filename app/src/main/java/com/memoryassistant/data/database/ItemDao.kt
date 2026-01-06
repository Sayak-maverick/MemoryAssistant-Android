package com.memoryassistant.data.database

import androidx.room.*
import com.memoryassistant.data.models.Item
import kotlinx.coroutines.flow.Flow

/**
 * ItemDao - Data Access Object for Item operations
 *
 * DAO (Data Access Object) - A pattern for accessing database data
 * This interface defines all the database operations we can perform on Items.
 *
 * What is a DAO?
 * - Think of it as a "contract" or "API" for database operations
 * - Room will automatically implement these methods for us (no need to write SQL!)
 * - We just declare what we want to do, Room handles the rest
 *
 * Room automatically generates the implementation based on the annotations.
 */
@Dao
interface ItemDao {

    /**
     * Get all items, ordered by creation date (newest first)
     *
     * @return Flow<List<Item>> - Observable list that updates automatically
     *
     * What is Flow?
     * - Like LiveData, but more powerful
     * - Automatically notifies observers when data changes
     * - Similar to Observables in RxJS (web)
     * - Works with Kotlin Coroutines
     *
     * @Query - annotation that defines the SQL query to run
     */
    @Query("SELECT * FROM items ORDER BY createdAt DESC")
    fun getAllItems(): Flow<List<Item>>

    /**
     * Get a single item by ID
     *
     * @param id - The item's unique identifier
     * @return Flow<Item?> - The item, or null if not found
     */
    @Query("SELECT * FROM items WHERE id = :id")
    fun getItemById(id: String): Flow<Item?>

    /**
     * Insert a new item into the database
     *
     * @param item - The item to insert
     *
     * @Insert - Room generates INSERT SQL for us
     * onConflict = OnConflictStrategy.REPLACE means:
     * - If an item with this ID already exists, replace it
     * - This is useful for updating items
     *
     * suspend - this is a coroutine function (async operation)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: Item)

    /**
     * Insert multiple items at once
     *
     * @param items - List of items to insert
     *
     * Useful for bulk operations (like initial data loading)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<Item>)

    /**
     * Update an existing item
     *
     * @param item - The item with updated data
     *
     * @Update - Room generates UPDATE SQL
     * Matches items by primary key (id)
     */
    @Update
    suspend fun updateItem(item: Item)

    /**
     * Delete an item from the database
     *
     * @param item - The item to delete
     *
     * @Delete - Room generates DELETE SQL
     * Matches items by primary key (id)
     */
    @Delete
    suspend fun deleteItem(item: Item)

    /**
     * Delete an item by ID
     *
     * @param id - The ID of the item to delete
     *
     * Custom query for deleting by ID
     */
    @Query("DELETE FROM items WHERE id = :id")
    suspend fun deleteItemById(id: String)

    /**
     * Delete all items (clear the entire table)
     *
     * Useful for testing or resetting the app
     */
    @Query("DELETE FROM items")
    suspend fun deleteAllItems()

    /**
     * Search items by name or description
     *
     * @param searchQuery - The text to search for
     * @return Flow<List<Item>> - Matching items
     *
     * LIKE operator with % wildcards:
     * - %query% = matches text anywhere in the string
     * - Example: "key" matches "Car Keys", "Keychain", "Turkey"
     */
    @Query("SELECT * FROM items WHERE name LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%' ORDER BY createdAt DESC")
    fun searchItems(searchQuery: String): Flow<List<Item>>

    /**
     * Get items with a specific label
     *
     * @param label - The label to filter by
     * @return Flow<List<Item>> - Items with this label
     *
     * Note: Since labels is stored as JSON string, we use LIKE to search within it
     * This isn't perfect but works for our needs
     */
    @Query("SELECT * FROM items WHERE labels LIKE '%' || :label || '%' ORDER BY createdAt DESC")
    fun getItemsByLabel(label: String): Flow<List<Item>>
}
