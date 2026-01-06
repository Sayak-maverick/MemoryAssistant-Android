package com.memoryassistant.data.services

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

/**
 * AuthService - Handles all authentication operations
 *
 * This class manages user login, signup, and logout using Firebase Authentication.
 *
 * What is a Service?
 * - Think of it as a "helper class" that does specific tasks
 * - AuthService handles everything related to user authentication
 * - We use it from our UI screens (like LoginScreen)
 *
 * What is Firebase Auth?
 * - Firebase handles the complex authentication logic for us
 * - It securely stores user passwords (hashed, not plain text)
 * - It gives us methods like createUserWithEmailAndPassword()
 */
class AuthService {

    /**
     * FirebaseAuth instance - our connection to Firebase Authentication
     *
     * This is a SINGLETON - there's only one instance shared across the entire app
     * Think of it like a "manager" that handles all auth requests
     */
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * Get the currently logged-in user
     *
     * Returns:
     * - FirebaseUser object if someone is logged in
     * - null if no one is logged in
     *
     * This is useful for checking "Is someone logged in?"
     */
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    /**
     * Check if a user is currently logged in
     *
     * Returns true if logged in, false otherwise
     */
    fun isUserLoggedIn(): Boolean {
        return currentUser != null
    }

    /**
     * Sign up a new user with email and password
     *
     * This is a SUSPEND function - it performs async work (network call to Firebase)
     * We use "suspend" so we can call it with Kotlin Coroutines
     *
     * @param email - The user's email address
     * @param password - The user's password (must be at least 6 characters)
     * @return Result<FirebaseUser> - Success with user, or Failure with error
     *
     * How Result works:
     * - Result.success(user) = Sign up worked! Here's the user
     * - Result.failure(exception) = Sign up failed, here's why
     */
    suspend fun signUp(email: String, password: String): Result<FirebaseUser> {
        return try {
            /**
             * createUserWithEmailAndPassword - Firebase method to create account
             *
             * This does a lot behind the scenes:
             * 1. Checks if email is already registered
             * 2. Validates password strength (6+ characters)
             * 3. Creates account in Firebase
             * 4. Automatically logs the user in
             *
             * .await() - converts Firebase's callback to a suspend function
             * This lets us write cleaner code without nested callbacks
             */
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()

            // Get the user from the result
            val user = authResult.user

            if (user != null) {
                // Success! Return the user
                Result.success(user)
            } else {
                // Something went wrong (shouldn't happen, but good to check)
                Result.failure(Exception("User creation failed"))
            }
        } catch (e: Exception) {
            // Catch any errors (invalid email, weak password, network issues, etc.)
            Result.failure(e)
        }
    }

    /**
     * Sign in an existing user with email and password
     *
     * @param email - The user's email address
     * @param password - The user's password
     * @return Result<FirebaseUser> - Success with user, or Failure with error
     */
    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            /**
             * signInWithEmailAndPassword - Firebase method to log in
             *
             * This:
             * 1. Checks if the email exists
             * 2. Verifies the password is correct
             * 3. Logs the user in
             * 4. Creates a session (user stays logged in)
             */
            val authResult = auth.signInWithEmailAndPassword(email, password).await()

            val user = authResult.user

            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Sign in failed"))
            }
        } catch (e: Exception) {
            // Catch errors (wrong password, user not found, network issues, etc.)
            Result.failure(e)
        }
    }

    /**
     * Sign out the current user
     *
     * This is simple - just tell Firebase to log the user out
     * No network call needed, so not a suspend function
     */
    fun signOut() {
        auth.signOut()
    }
}
