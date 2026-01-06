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
import com.memoryassistant.data.database.AppDatabase
import com.memoryassistant.data.models.Item
import com.memoryassistant.data.repository.ItemRepository
import com.memoryassistant.data.services.AuthService
import com.memoryassistant.ui.components.ItemCard
import com.memoryassistant.ui.screens.LoginScreen
import com.memoryassistant.ui.theme.MemoryAssistantTheme
import kotlinx.coroutines.launch

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

    // Create database and repository instances
    private lateinit var database: AppDatabase
    private lateinit var repository: ItemRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * Initialize Room database
         *
         * AppDatabase.getDatabase() - returns the singleton database instance
         * itemDao() - gets the DAO (Data Access Object) for items
         * ItemRepository - wraps the DAO with convenient methods
         */
        database = AppDatabase.getDatabase(applicationContext)
        repository = ItemRepository(database.itemDao())

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
                     *
                     * Now we pass the repository to access database
                     */
                    AuthenticationFlow(authService, repository)
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
fun AuthenticationFlow(authService: AuthService, repository: ItemRepository) {
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
            repository = repository,
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
 * NOW USING ROOM DATABASE!
 * - Items are loaded from the local SQLite database
 * - Data persists across app restarts
 * - Uses Flow to automatically update when data changes
 *
 * NEW: Added logout button in the top bar
 */
@Composable
fun HomeScreen(repository: ItemRepository, onLogout: () -> Unit = {}) {
    /**
     * Collect items from the database as State
     *
     * repository.getAllItems() - returns Flow<List<Item>>
     * collectAsState() - converts Flow to Compose State
     *
     * This is REACTIVE - when database changes, UI automatically updates!
     * Similar to observing LiveData in traditional Android
     */
    val items by repository.getAllItems().collectAsState(initial = emptyList())

    /**
     * Coroutine scope for launching async operations
     *
     * rememberCoroutineScope - gets a scope tied to this composable
     * We'll use this to insert dummy data
     */
    val coroutineScope = rememberCoroutineScope()

    /**
     * Insert dummy data on first load (if database is empty)
     *
     * LaunchedEffect - runs when the composable is first created
     * key = Unit means it only runs once (doesn't re-run on recomposition)
     */
    LaunchedEffect(Unit) {
        // If database is empty, add dummy data
        if (items.isEmpty()) {
            repository.insertDummyData()
        }
    }

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
         * Show loading or empty state if no items
         */
        if (items.isEmpty()) {
            // Show centered text while loading or if truly empty
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Loading items...",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
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
}

/**
 * NOTE: getDummyItems() has been removed!
 *
 * We now use Room database with repository.insertDummyData()
 * The data is stored in the database and persists across app restarts
 */
