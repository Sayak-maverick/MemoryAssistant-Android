package com.memoryassistant.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.memoryassistant.data.models.Item

/**
 * AppDatabase - The main Room database for our app
 *
 * This is the database holder class.
 * It defines:
 * 1. Which entities (tables) are in the database
 * 2. The database version (for migrations)
 * 3. How to access DAOs
 *
 * What is Room Database?
 * - Room is an abstraction layer over SQLite
 * - SQLite is a local database that stores data on the device
 * - Room makes it easier to work with SQLite (no raw SQL needed)
 *
 * Singleton Pattern:
 * - We only want ONE instance of the database
 * - Multiple instances would cause conflicts
 * - We use INSTANCE variable and synchronized block to ensure this
 */
@Database(
    entities = [Item::class],  // List of all entity classes (tables)
    version = 1,  // Database version - increment when schema changes
    exportSchema = false  // Don't export schema (not needed for this app)
)
@TypeConverters(Converters::class)  // Register our custom type converters
abstract class AppDatabase : RoomDatabase() {

    /**
     * Abstract function to get the ItemDao
     *
     * Room will automatically implement this
     * We just declare it, Room generates the code
     */
    abstract fun itemDao(): ItemDao

    /**
     * Companion object - like static members in Java
     *
     * These are shared across all instances of the class
     * Perfect for the Singleton pattern
     */
    companion object {
        /**
         * Volatile - ensures INSTANCE is always up-to-date across threads
         *
         * In multi-threaded scenarios, this prevents caching issues
         */
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Get the database instance (Singleton pattern)
         *
         * @param context - Android context (needed to create database)
         * @return AppDatabase - The single database instance
         *
         * How this works:
         * 1. Check if INSTANCE exists -> return it
         * 2. If null, create it (synchronized to prevent multiple threads from creating it)
         * 3. Return the newly created instance
         */
        fun getDatabase(context: Context): AppDatabase {
            /**
             * If INSTANCE is not null, return it
             * Elvis operator ?: - "if null, do this instead"
             */
            return INSTANCE ?: synchronized(this) {
                /**
                 * Double-check inside synchronized block
                 * Another thread might have created it while we were waiting
                 */
                val instance = INSTANCE ?: buildDatabase(context).also {
                    INSTANCE = it
                }
                instance
            }
        }

        /**
         * Build the database
         *
         * @param context - Android context
         * @return AppDatabase - The created database
         */
        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,  // Use app context (lives as long as app)
                AppDatabase::class.java,  // Database class
                "memory_assistant_database"  // Database file name
            )
                /**
                 * fallbackToDestructiveMigration - for development
                 *
                 * If database schema changes (version increases):
                 * - This deletes the old database and creates a new one
                 * - In production, you'd write proper migrations instead
                 * - But for now, this is fine (we don't have important data yet)
                 */
                .fallbackToDestructiveMigration()
                .build()
        }

        /**
         * Optional: Method to close the database (cleanup)
         *
         * You'd call this when the app is destroyed
         * Usually not needed, but good practice for testing
         */
        fun closeDatabase() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}
