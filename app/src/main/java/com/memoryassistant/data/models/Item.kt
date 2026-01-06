package com.memoryassistant.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.memoryassistant.data.database.Converters

/**
 * Item - Represents a physical item the user wants to remember
 *
 * This is a Kotlin DATA CLASS - a special type of class designed to hold data.
 * Data classes automatically give you:
 * - toString() - for printing (e.g., "Item(id=1, name=Keys, ...)")
 * - equals() - for comparing items
 * - copy() - for making copies with some fields changed
 * - Destructuring - val (id, name) = item
 *
 * NEW in Step 4: Room Database annotations
 * - @Entity: Marks this as a database table
 * - @PrimaryKey: Marks 'id' as the primary key (unique identifier)
 * - @TypeConverters: Tells Room how to convert List<String> to/from database format
 *
 * Think of this like a blueprint or template for items.
 * Each item will have these properties AND will be stored in the database.
 */
@Entity(tableName = "items")  // Create a table named "items"
@TypeConverters(Converters::class)  // Use our custom converter for List<String>
data class Item(
    /**
     * Unique identifier for this item
     * Using String instead of Int so we can use UUIDs later
     *
     * @PrimaryKey: This field uniquely identifies each row in the database
     */
    @PrimaryKey
    val id: String,

    /**
     * The name of the item (e.g., "Car Keys", "Wallet", "Phone")
     */
    val name: String,

    /**
     * Optional description with more details
     * The "?" means this can be null (empty)
     */
    val description: String? = null,

    /**
     * When this item was added
     * Using Long to store timestamp (milliseconds since 1970)
     */
    val createdAt: Long = System.currentTimeMillis(),

    /**
     * URL or path to the item's image
     * For now, we'll use emoji or leave it null
     */
    val imageUrl: String? = null,

    /**
     * Tags/labels for the item (e.g., ["important", "daily-use"])
     * List means it can have multiple labels
     * emptyList() means default is no labels
     *
     * Note: Room doesn't natively support List<String>, so we need a TypeConverter
     */
    val labels: List<String> = emptyList()
)

/**
 * EXAMPLE OF HOW TO CREATE AN ITEM:
 *
 * val myKeys = Item(
 *     id = "1",
 *     name = "Car Keys",
 *     description = "Toyota keys with red keychain",
 *     labels = listOf("important", "daily")
 * )
 *
 * EXAMPLE OF HOW TO ACCESS PROPERTIES:
 *
 * println(myKeys.name)  // Prints: Car Keys
 * println(myKeys.description)  // Prints: Toyota keys with red keychain
 */
