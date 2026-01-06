package com.memoryassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
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
 */
class MainActivity : ComponentActivity() {

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
                    // Show our greeting screen
                    GreetingScreen()
                }
            }
        }
    }
}

/**
 * GreetingScreen - Our first custom screen component
 *
 * @Composable means this function creates UI elements
 * It's like a React component - it describes what should be on screen
 */
@Composable
fun GreetingScreen() {
    // Column arranges its children vertically (one on top of another)
    Column(
        modifier = Modifier
            .fillMaxSize()      // Take up full screen
            .padding(24.dp),    // Add padding around edges (dp = density-independent pixels)

        // Center everything vertically and horizontally
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // First text element - the title
        Text(
            text = "Memory Assistant",
            fontSize = 32.sp,              // sp = scalable pixels (for text)
            fontWeight = FontWeight.Bold,   // Make it bold
            color = MaterialTheme.colorScheme.primary  // Use primary theme color
        )

        // Second text element - subtitle
        Text(
            text = "Hello World! 👋",
            fontSize = 20.sp,
            modifier = Modifier.padding(top = 16.dp),  // Add space above
            color = MaterialTheme.colorScheme.onBackground
        )

        // Third text element - description
        Text(
            text = "Your personal memory assistant is ready!",
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)  // Slightly transparent
        )
    }
}

/**
 * Preview function - This lets you see your UI in Android Studio without running the app!
 * It's super useful for quick development
 */
@Preview(showBackground = true)
@Composable
fun GreetingScreenPreview() {
    MemoryAssistantTheme {
        GreetingScreen()
    }
}
