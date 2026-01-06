package com.memoryassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.memoryassistant.data.models.Item
import com.memoryassistant.data.services.AuthService
import com.memoryassistant.ui.components.ItemCard
import com.memoryassistant.ui.screens.LoginScreen
import com.memoryassistant.ui.theme.MemoryAssistantTheme

/**
 * MainActivity - The main entry point of your app
 *
 * Think of an Activity as a "screen" in your app.
 * This is the first screen users see when they launch Memory Assistant.
 *
 * What's happening here:
 * 1. ComponentActivity - Base class that gives us Android functionality
 * 2. onCreate - Called when Android creates this screen (like a constructor)
 * 3. setContent - This is where we define what shows on the screen using Compose
 *
 * NEW: Authentication Flow
 * - If user is NOT logged in -> show LoginScreen
 * - If user IS logged in -> show HomeScreen
 */
class MainActivity : ComponentActivity() {

    // Create AuthService instance (our authentication helper)
    private val authService = AuthService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // setContent is where we build our UI using Jetpack Compose
        // Everything inside the { } is what gets displayed on screen
        setContent {
            // MemoryAssistantTheme applies our app's colors and styling
            MemoryAssistantTheme {
                // Surface is like a "canvas" - a container for our content
                Surface(
                    modifier = Modifier.fillMaxSize(),  // Fill the entire screen
                    color = MaterialTheme.colorScheme.background  // Use theme's background color
                ) {
                    /**
                     * AuthenticationFlow - decides which screen to show
                     *
                     * This composable checks if user is logged in and shows:
                     * - LoginScreen if not logged in
                     * - HomeScreen if logged in
                     */
                    AuthenticationFlow(authService)
                }
            }
        }
    }
}

/**
 * AuthenticationFlow - Navigation between Login and Home screens
 *
 * This manages the authentication state and decides which screen to show.
 *
 * State Management:
 * - isLoggedIn: tracks whether user is authenticated
 * - When user logs in successfully, we update isLoggedIn to true
 * - This triggers recomposition and shows HomeScreen
 */
@Composable
fun AuthenticationFlow(authService: AuthService) {
    /**
     * Track login state
     *
     * Initialize with the current auth state from Firebase
     * authService.isUserLoggedIn() checks if someone is logged in
     */
    var isLoggedIn by remember { mutableStateOf(authService.isUserLoggedIn()) }

    /**
     * Conditional rendering - like if/else in React
     *
     * if (condition) { ShowThisScreen } else { ShowThatScreen }
     */
    if (isLoggedIn) {
        // User is logged in - show the home screen
        HomeScreen(
            onLogout = {
                // When user logs out, sign them out and update state
                authService.signOut()
                isLoggedIn = false
            }
        )
    } else {
        // User is NOT logged in - show login screen
        LoginScreen(
            authService = authService,
            onLoginSuccess = {
                // When login succeeds, update state to show home screen
                isLoggedIn = true
            }
        )
    }
}

/**
 * HomeScreen - The main screen showing all items
 *
 * This replaces the GreetingScreen with a list of items.
 * In Step 4, we'll load these from a database.
 * For now, we're using hardcoded dummy data.
 *
 * NEW: Added logout button in the top bar
 */
@Composable
fun HomeScreen(onLogout: () -> Unit = {}) {
    /**
     * Scaffold - Material Design 3 layout structure
     * Provides app bar, floating action button, etc.
     */
    Scaffold(
        topBar = {
            // Top app bar with title and logout button
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Memory Assistant",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // Logout button
                    TextButton(onClick = onLogout) {
                        Text(
                            text = "Logout",
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        /**
         * Get dummy data (hardcoded items)
         * Later, this will come from a database
         */
        val items = getDummyItems()

        /**
         * LazyColumn - Like RecyclerView, but simpler
         * It's "lazy" because it only renders items visible on screen
         * This is efficient for long lists
         *
         * Think of it like map() in React, but optimized for scrolling
         */
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),  // Respect the Scaffold padding
            contentPadding = PaddingValues(vertical = 8.dp)  // Padding for the list
        ) {
            /**
             * items() function - Similar to list.map() in JavaScript
             * For each item in the list, create an ItemCard
             */
            items(items) { item ->
                ItemCard(
                    item = item,
                    onClick = {
                        // TODO: Navigate to item detail screen
                        // For now, just a placeholder
                    }
                )
            }
        }
    }
}

/**
 * getDummyItems - Returns hardcoded sample items
 *
 * This simulates data we'll later get from a database.
 * We're using realistic examples of things people often misplace.
 */
fun getDummyItems(): List<Item> {
    return listOf(
        Item(
            id = "1",
            name = "Car Keys",
            description = "Toyota keys with red keychain",
            createdAt = System.currentTimeMillis() - 3600000,  // 1 hour ago
            labels = listOf("important", "daily")
        ),
        Item(
            id = "2",
            name = "Wallet",
            description = "Brown leather wallet with credit cards",
            createdAt = System.currentTimeMillis() - 7200000,  // 2 hours ago
            labels = listOf("important")
        ),
        Item(
            id = "3",
            name = "Reading Glasses",
            description = "Black frame reading glasses",
            createdAt = System.currentTimeMillis() - 86400000,  // 1 day ago
            labels = listOf("daily")
        ),
        Item(
            id = "4",
            name = "Phone Charger",
            description = "USB-C white charger cable",
            createdAt = System.currentTimeMillis() - 172800000,  // 2 days ago
        ),
        Item(
            id = "5",
            name = "Headphones",
            description = "Sony wireless headphones",
            createdAt = System.currentTimeMillis() - 259200000,  // 3 days ago
            labels = listOf("electronics")
        ),
        Item(
            id = "6",
            name = "House Keys",
            description = "Spare keys with blue keychain",
            createdAt = System.currentTimeMillis() - 604800000,  // 1 week ago
            labels = listOf("important", "backup")
        ),
        Item(
            id = "7",
            name = "Work Badge",
            description = "Office access card",
            createdAt = System.currentTimeMillis() - 1209600000,  // 2 weeks ago
            labels = listOf("work", "important")
        ),
        Item(
            id = "8",
            name = "Backpack",
            description = "Black Nike backpack",
            createdAt = System.currentTimeMillis() - 1814400000,  // 3 weeks ago
        )
    )
}

/**
 * Preview function - See the HomeScreen in Android Studio
 */
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MemoryAssistantTheme {
        HomeScreen()
    }
}
