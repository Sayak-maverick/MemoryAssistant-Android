package com.memoryassistant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.memoryassistant.data.services.AuthService
import kotlinx.coroutines.launch

/**
 * LoginScreen - User login and signup screen
 *
 * This screen allows users to:
 * 1. Enter email and password
 * 2. Choose to log in or sign up
 * 3. See error messages if something goes wrong
 *
 * What's new here:
 * - TextField - input fields for email/password
 * - remember - saves state across recompositions
 * - rememberCoroutineScope - lets us call suspend functions
 * - State management - tracking email, password, errors, loading
 */
@Composable
fun LoginScreen(
    authService: AuthService,
    onLoginSuccess: () -> Unit  // Callback when login succeeds
) {
    /**
     * STATE MANAGEMENT
     *
     * remember + mutableStateOf = React's useState
     *
     * When these values change, Compose automatically updates the UI
     * This is called "recomposition"
     */

    // Email and password fields
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Error message to display (null = no error)
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Loading state (true = showing progress indicator)
    var isLoading by remember { mutableStateOf(false) }

    // Track if we're in "Sign Up" mode or "Sign In" mode
    var isSignUpMode by remember { mutableStateOf(false) }

    /**
     * CoroutineScope - allows us to call suspend functions
     *
     * AuthService methods are suspend functions (async network calls)
     * We need a coroutine scope to call them
     *
     * rememberCoroutineScope = gets a scope tied to this Composable's lifecycle
     */
    val coroutineScope = rememberCoroutineScope()

    // UI Layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        /**
         * Title
         */
        Text(
            text = if (isSignUpMode) "Create Account" else "Welcome Back",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isSignUpMode) "Sign up to get started" else "Sign in to continue",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        /**
         * Email TextField
         *
         * TextField is like <input type="email"> in HTML
         * - value: The current text (bound to our email state)
         * - onValueChange: Called when user types (updates our state)
         * - label: The floating label "Email"
         */
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },  // Update state when user types
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email  // Shows email keyboard
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        /**
         * Password TextField
         *
         * PasswordVisualTransformation - shows dots instead of text (••••)
         */
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),  // Hide password
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        /**
         * Error message display
         *
         * This only shows if errorMessage is not null
         */
        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        /**
         * Sign In / Sign Up Button
         *
         * onClick - what happens when user taps the button
         * enabled - disable button while loading
         */
        Button(
            onClick = {
                // Clear any previous errors
                errorMessage = null

                // Validate inputs
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Please fill in all fields"
                    return@Button
                }

                if (password.length < 6) {
                    errorMessage = "Password must be at least 6 characters"
                    return@Button
                }

                // Start loading
                isLoading = true

                /**
                 * Launch a coroutine to call AuthService
                 *
                 * coroutineScope.launch { } - starts async work
                 * Similar to async/await in JavaScript
                 */
                coroutineScope.launch {
                    // Call sign up or sign in based on mode
                    val result = if (isSignUpMode) {
                        authService.signUp(email, password)
                    } else {
                        authService.signIn(email, password)
                    }

                    // Stop loading
                    isLoading = false

                    /**
                     * Handle the result
                     *
                     * Result has two states:
                     * - success: Authentication worked
                     * - failure: Authentication failed with an error
                     */
                    result.fold(
                        onSuccess = {
                            // Success! Call the callback to navigate to home
                            onLoginSuccess()
                        },
                        onFailure = { exception ->
                            // Failure - show error message
                            errorMessage = exception.message ?: "Authentication failed"
                        }
                    )
                }
            },
            enabled = !isLoading,  // Disable while loading
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                // Show spinner while loading
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                // Show button text
                Text(
                    text = if (isSignUpMode) "Sign Up" else "Sign In",
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        /**
         * Toggle between Sign In and Sign Up
         *
         * TextButton - like a link, less prominent than Button
         */
        TextButton(
            onClick = {
                isSignUpMode = !isSignUpMode
                errorMessage = null  // Clear errors when switching
            }
        ) {
            Text(
                text = if (isSignUpMode) {
                    "Already have an account? Sign In"
                } else {
                    "Don't have an account? Sign Up"
                }
            )
        }
    }
}
