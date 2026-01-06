package com.memoryassistant.data.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Converters - Type converters for Room database
 *
 * Room can only store primitive types (String, Int, Long, etc.) in the database.
 * For complex types like List<String>, we need to convert them to/from a format Room can store.
 *
 * What is a TypeConverter?
 * - A class that tells Room how to convert between complex types and primitive types
 * - We convert List<String> to JSON String (for storage)
 * - We convert JSON String back to List<String> (when reading)
 *
 * Example:
 * - labels = ["important", "daily"]
 * - Stored in DB as: "[\"important\",\"daily\"]"
 * - When read from DB, converted back to: ["important", "daily"]
 */
class Converters {

    /**
     * Gson - Google's JSON library
     *
     * This is used to convert between Kotlin objects and JSON strings.
     * Think of it like JSON.stringify() and JSON.parse() in JavaScript.
     */
    private val gson = Gson()

    /**
     * Convert List<String> to JSON String (for storing in database)
     *
     * @param list - The list to convert
     * @return JSON string representation
     *
     * Example: ["tag1", "tag2"] -> "[\"tag1\",\"tag2\"]"
     */
    @TypeConverter
    fun fromStringList(list: List<String>?): String? {
        // If list is null, return null
        if (list == null) return null

        // Convert list to JSON string using Gson
        return gson.toJson(list)
    }

    /**
     * Convert JSON String to List<String> (when reading from database)
     *
     * @param value - The JSON string from database
     * @return List<String>
     *
     * Example: "[\"tag1\",\"tag2\"]" -> ["tag1", "tag2"]
     */
    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        // If value is null, return empty list (not null)
        if (value == null) return emptyList()

        /**
         * TypeToken - tells Gson what type to convert to
         *
         * We need this because of type erasure in Java/Kotlin
         * Gson needs to know we want List<String>, not just List
         */
        val listType = object : TypeToken<List<String>>() {}.type

        // Convert JSON string back to List<String>
        return gson.fromJson(value, listType)
    }
}
