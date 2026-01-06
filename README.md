# Memory Assistant - Android App

Your personal memory assistant to help you remember where you placed things!

## 🎯 What Is This Project?

This is the **Android version** of Memory Assistant. It's built with:
- **Kotlin** - Modern programming language for Android
- **Jetpack Compose** - Modern UI toolkit (like React, but for Android)
- **Material Design 3** - Google's latest design system

## 📁 Project Structure

Here's what each folder does:

```
MemoryAssistant/
├── app/                          # The main app module
│   ├── src/
│   │   └── main/
│   │       ├── java/com/memoryassistant/
│   │       │   ├── MainActivity.kt      # First screen users see
│   │       │   └── ui/
│   │       │       └── theme/           # Colors, fonts, styles
│   │       │           ├── Color.kt     # App colors
│   │       │           ├── Theme.kt     # Light/dark themes
│   │       │           └── Type.kt      # Text styles
│   │       ├── res/                     # Resources (images, strings, XML)
│   │       │   ├── values/
│   │       │   │   ├── strings.xml      # All text strings
│   │       │   │   └── themes.xml       # XML themes
│   │       │   └── xml/
│   │       │       ├── backup_rules.xml
│   │       │       └── data_extraction_rules.xml
│   │       └── AndroidManifest.xml      # App configuration
│   └── build.gradle.kts                 # App dependencies
├── build.gradle.kts                     # Project-level build config
└── settings.gradle.kts                  # Gradle settings
```

## 🚀 How to Run

1. **Open in Android Studio**
   - File → Open → Navigate to `/Users/Maverick/MemoryAssistant`
   - Android Studio will automatically sync Gradle

2. **Set Up an Emulator**
   - Tools → Device Manager
   - Create Virtual Device
   - Choose Pixel 6 or any modern phone
   - Select Android 13 or 14 as system image

3. **Run the App**
   - Click the green "Run" ▶️ button (or press Ctrl+R)
   - Select your emulator
   - Wait for the app to build and install

## 📚 Key Concepts Explained

### What is Jetpack Compose?
Think of it like React for Android. Instead of XML layouts, you write UI code in Kotlin using composable functions:
```kotlin
@Composable
fun GreetingScreen() {
    Text("Hello World!")
}
```

### What is Material Design 3?
Google's design system with ready-made components (buttons, cards, etc.) that look professional.

### What is a ViewModel?
Holds the data and logic for a screen. Separates UI from business logic.

### What is Room?
Local database for Android (like SQLite, but easier to use).

## 🎨 Current Features (Step 1)

- ✅ Basic app structure set up
- ✅ Hello World screen with Compose
- ✅ Material Design 3 theming
- ✅ Custom colors (blue primary, green secondary)

## 📖 Next Steps

See [/Users/Maverick/.claude/plans/kind-wishing-milner.md](../.claude/plans/kind-wishing-milner.md) for the full roadmap.

Next up: **Step 2 - Display a simple list of items** 🎯
