package com.memoryassistant.data.models

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
 * Think of this like a blueprint or template for items.
 * Each item will have these properties.
 */
data class Item(
    /**
     * Unique identifier for this item
     * Using String instead of Int so we can use UUIDs later
     */
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
