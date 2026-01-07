package com.memoryassistant.ui.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.memoryassistant.data.models.Item
import com.memoryassistant.ui.theme.MemoryAssistantTheme
import java.text.SimpleDateFormat
import java.util.*

/**
 * ItemCard - A reusable component that displays a single item
 *
 * This is a COMPOSABLE FUNCTION - it creates UI elements.
 * Think of it like a React component.
 *
 * @param item - The Item object to display
 * @param modifier - Optional styling/layout modifications
 * @param onClick - Optional function to call when card is clicked
 *
 * USAGE EXAMPLE:
 * ItemCard(
 *     item = Item(id = "1", name = "Car Keys"),
 *     onClick = { /* do something */ }
 * )
 */
@Composable
fun ItemCard(
    item: Item,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    /**
     * Card - Material Design 3 component
     * Provides elevation (shadow), rounded corners, and background
     */
    Card(
        modifier = modifier
            .fillMaxWidth()  // Take up full width available
            .padding(horizontal = 16.dp, vertical = 8.dp),  // Space around card
        shape = RoundedCornerShape(12.dp),  // Rounded corners with 12dp radius
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface  // Use theme's surface color
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp  // Subtle shadow
        ),
        onClick = onClick  // Make the card clickable
    ) {
        /**
         * Row - Arranges children horizontally (side-by-side)
         * We'll use this to show emoji icon + text
         */
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),  // Padding inside the card
            verticalAlignment = Alignment.CenterVertically  // Center items vertically
        ) {
            /**
             * Icon/Emoji Circle OR Image
             * If item has imageUrl, show the image, otherwise show emoji
             */
            if (item.imageUrl != null) {
                // Show image thumbnail
                Image(
                    painter = rememberAsyncImagePainter(Uri.parse(item.imageUrl)),
                    contentDescription = item.name,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp)),  // Slightly rounded corners for images
                    contentScale = ContentScale.Crop  // Crop to fill the space
                )
            } else {
                // Show emoji icon
                Box(
                    modifier = Modifier
                        .size(48.dp)  // 48x48 dp circle
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(24.dp)  // Circle (half of size)
                        ),
                    contentAlignment = Alignment.Center  // Center the emoji
                ) {
                    Text(
                        text = getEmojiForItem(item.name),  // Get emoji based on item name
                        fontSize = 24.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))  // Space between icon and text

            /**
             * Column - Arranges children vertically (stacked)
             * We'll show name, description, and timestamp
             */
            Column(
                modifier = Modifier.weight(1f)  // Take up remaining space
            ) {
                // Item name (bold, larger)
                Text(
                    text = item.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,  // Don't wrap to multiple lines
                    overflow = TextOverflow.Ellipsis  // Show "..." if text is too long
                )

                // Description (if available)
                if (item.description != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),  // Slightly transparent
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Timestamp
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTimestamp(item.createdAt),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)  // More transparent
                )

                // Labels/Tags (if any)
                if (item.labels.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)  // 4dp space between tags
                    ) {
                        item.labels.take(3).forEach { label ->  // Show max 3 labels
                            LabelChip(label)
                        }
                    }
                }
            }
        }
    }
}

/**
 * LabelChip - Small rounded pill showing a label/tag
 */
@Composable
fun LabelChip(label: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.padding(0.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

/**
 * Helper function: Get emoji based on item name
 * This is just for fun - later we'll use real images
 */
fun getEmojiForItem(name: String): String {
    return when {
        name.contains("key", ignoreCase = true) -> "🔑"
        name.contains("wallet", ignoreCase = true) -> "💼"
        name.contains("phone", ignoreCase = true) -> "📱"
        name.contains("glass", ignoreCase = true) -> "👓"
        name.contains("watch", ignoreCase = true) -> "⌚"
        name.contains("bag", ignoreCase = true) -> "🎒"
        name.contains("book", ignoreCase = true) -> "📚"
        name.contains("headphone", ignoreCase = true) -> "🎧"
        else -> "📦"  // Default box emoji
    }
}

/**
 * Helper function: Format timestamp to readable string
 * Converts milliseconds to "2 hours ago" or "Jan 5, 2:30 PM"
 */
fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "Just now"  // Less than 1 minute
        diff < 3600_000 -> "${diff / 60_000} min ago"  // Less than 1 hour
        diff < 86400_000 -> "${diff / 3600_000} hours ago"  // Less than 1 day
        else -> {
            // Show date and time
            val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}

/**
 * PREVIEW - This lets you see the component in Android Studio
 * without running the whole app!
 */
@Preview(showBackground = true)
@Composable
fun ItemCardPreview() {
    MemoryAssistantTheme {
        Column {
            ItemCard(
                item = Item(
                    id = "1",
                    name = "Car Keys",
                    description = "Toyota keys with red keychain",
                    labels = listOf("important", "daily")
                )
            )

            ItemCard(
                item = Item(
                    id = "2",
                    name = "Wallet",
                    description = "Brown leather wallet"
                )
            )

            ItemCard(
                item = Item(
                    id = "3",
                    name = "Phone Charger"
                )
            )
        }
    }
}
