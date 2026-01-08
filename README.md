# Memory Assistant - Android App

A comprehensive Android application designed to help individuals with memory loss track and remember their personal items. Built with modern Android technologies including Jetpack Compose, Room Database, and Firebase.

## 🎯 Features

### Core Functionality
- ✅ **Item Management**: Add, edit, delete, and search items
- 📸 **Photo Capture**: Take photos or select from gallery
- 🤖 **AI Object Detection**: Automatic label suggestions using Google Cloud Vision API
- 🎙️ **Voice Notes**: Record voice notes with automatic transcription
- 🗣️ **Multi-language Support**: Speech-to-text in 15+ languages
- 📍 **GPS Location Tracking**: Save and display where items were last seen
- ☁️ **Cloud Sync**: Real-time synchronization across devices using Firebase Firestore
- 🔐 **User Authentication**: Secure login with Firebase Authentication
- 💾 **Offline-first**: Works offline with automatic sync when connection returns

### Technical Features
- Material Design 3 UI with dynamic theming
- Room Database for local storage
- Real-time updates with Kotlin Flow
- Offline-first architecture
- Automatic cloud backup
- Permission handling with Accompanist

## 🏗️ Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Design System**: Material Design 3
- **Local Database**: Room (SQLite wrapper)
- **Authentication**: Firebase Authentication
- **Cloud Storage**: Firebase Firestore
- **Camera**: CameraX
- **Image Loading**: Coil
- **Location**: Google Play Services Location
- **AI Services**:
  - Google Cloud Vision API (Object Detection)
  - Google Cloud Speech-to-Text API (Voice Transcription)
- **Permissions**: Accompanist Permissions
- **Async Operations**: Kotlin Coroutines + Flow
- **Architecture**: MVVM with Repository Pattern

## 📋 Prerequisites

1. **Android Studio**: Arctic Fox or later
2. **Minimum SDK**: API 26 (Android 8.0)
3. **Target SDK**: API 34 (Android 14)
4. **Firebase Project**: Set up at [Firebase Console](https://console.firebase.com)
5. **Google Cloud Project**: For Vision and Speech-to-Text APIs

## 🚀 Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/Sayak-maverick/MemoryAssistant-Android.git
cd MemoryAssistant
```

### 2. Firebase Setup

1. Create a Firebase project at [Firebase Console](https://console.firebase.com)
2. Add an Android app to your Firebase project
3. Download `google-services.json`
4. Place it in `app/` directory
5. Enable Firebase Authentication (Email/Password)
6. Enable Cloud Firestore
7. Set Firestore security rules:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId}/{document=**} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

### 3. Google Cloud APIs Setup

#### Enable APIs
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Enable **Cloud Vision API**
3. Enable **Cloud Speech-to-Text API**

#### Create Service Account
1. Go to **APIs & Services > Credentials**
2. Click **Create Credentials > Service Account**
3. Download the JSON key file
4. Rename it to `vision_credentials.json`
5. Place it in `app/src/main/res/raw/` directory

⚠️ **Security**: The `vision_credentials.json` file is already in `.gitignore`. Never commit API credentials!

### 4. Build and Run

1. Open the project in Android Studio
2. Sync Gradle files
3. Connect an Android device or start an emulator
4. Click **Run** (or press Shift + F10)

## 📁 Project Structure

```
app/
├── src/main/
│   ├── java/com/memoryassistant/
│   │   ├── data/
│   │   │   ├── database/        # Room database setup
│   │   │   ├── models/          # Data classes
│   │   │   ├── repository/      # Repository pattern implementation
│   │   │   └── services/        # Firebase, Vision, Audio, Location services
│   │   ├── ui/
│   │   │   ├── auth/           # Authentication screens
│   │   │   ├── components/     # Reusable UI components
│   │   │   └── screens/        # Main app screens
│   │   └── MainActivity.kt     # App entry point
│   ├── res/
│   │   ├── raw/               # vision_credentials.json (git-ignored)
│   │   └── xml/               # FileProvider paths
│   └── AndroidManifest.xml
├── build.gradle.kts
└── google-services.json       # Firebase config (git-ignored)
```

## 🔑 Required Permissions

The app requires the following permissions:

- **INTERNET**: For Firebase and Google Cloud APIs
- **CAMERA**: To take photos of items
- **RECORD_AUDIO**: For voice notes
- **ACCESS_FINE_LOCATION**: For GPS location tracking
- **ACCESS_COARSE_LOCATION**: For approximate location
- **READ_EXTERNAL_STORAGE**: To select photos from gallery
- **WRITE_EXTERNAL_STORAGE**: For camera file storage (older devices)

All permissions are requested at runtime with proper user explanations.

## 🎨 Key Features Explained

### Item Management
- Add items with name, description, and labels
- Edit existing items
- Delete with confirmation dialog
- Search and filter items

### Photo & AI Detection
- Capture photos using CameraX
- Select from gallery
- AI automatically detects objects with >70% confidence
- Auto-suggests item names and labels

### Voice Notes (15 Languages)
- Record audio in .3gp format
- Automatic speech-to-text transcription
- Supports: English, Spanish, French, German, Italian, Portuguese, Chinese, Japanese, Korean, Arabic, Hindi, Russian, Dutch, Polish, Turkish, Vietnamese
- Automatic language detection
- Playback with MediaPlayer

### GPS Location
- Captures current GPS coordinates
- Reverse geocoding to human-readable address
- Shows: "123 Main St, New York, NY" or coordinates
- Uses Android Geocoder API

### Cloud Sync
- Automatic sync on add/update/delete
- Real-time updates from other devices
- Offline support with pending sync queue
- User-specific data isolation
- Firestore path: `users/{userId}/items/{itemId}`

## 🧪 Testing

### Manual Testing Checklist

1. **Authentication**
   - [ ] Register new account
   - [ ] Login with existing account
   - [ ] Logout

2. **Item Operations**
   - [ ] Add new item
   - [ ] Edit item
   - [ ] Delete item
   - [ ] Search items

3. **Photo Features**
   - [ ] Take photo with camera
   - [ ] Select from gallery
   - [ ] AI label detection
   - [ ] Photo display

4. **Voice Notes**
   - [ ] Record audio
   - [ ] Play audio
   - [ ] View transcription
   - [ ] Delete audio
   - [ ] Test multiple languages

5. **Location**
   - [ ] Add GPS location
   - [ ] View location name
   - [ ] Remove location

6. **Cloud Sync**
   - [ ] Create item on device 1
   - [ ] Verify sync on device 2
   - [ ] Update item on device 2
   - [ ] Verify update on device 1
   - [ ] Delete item on device 1
   - [ ] Verify deletion on device 2

7. **Offline Mode**
   - [ ] Disable internet
   - [ ] Add/edit items
   - [ ] Enable internet
   - [ ] Verify auto-sync

## 🐛 Troubleshooting

### Build Errors

**Issue**: "google-services.json not found"
- **Solution**: Download from Firebase Console and place in `app/` directory

**Issue**: "vision_credentials.json not found"
- **Solution**: Create the file from Google Cloud Console and place in `app/src/main/res/raw/`

### Runtime Errors

**Issue**: Camera not working
- **Solution**: Ensure CAMERA permission is granted in app settings

**Issue**: Voice recording fails
- **Solution**: Check RECORD_AUDIO permission is granted

**Issue**: Cloud sync not working
- **Solution**:
  1. Verify Firebase Authentication is enabled
  2. Check Firestore rules allow user access
  3. Ensure internet connection

**Issue**: AI detection returns empty
- **Solution**:
  1. Verify Vision API is enabled in Google Cloud Console
  2. Check `vision_credentials.json` is correctly placed
  3. Ensure image has detectable objects

## 📱 Minimum Requirements

- Android 8.0 (API 26) or higher
- 100 MB free storage
- Camera (optional, for photo features)
- Microphone (optional, for voice notes)
- GPS (optional, for location tracking)
- Internet connection (for sync and AI features)

## 🔒 Security & Privacy

- All user data is encrypted in transit
- Firebase Authentication for secure login
- User-specific Firestore rules prevent unauthorized access
- API credentials never committed to Git
- Local database protected by Android's app sandboxing

## 🚦 Performance

- Offline-first architecture ensures instant UI updates
- Background sync doesn't block UI
- Images compressed before upload
- Efficient Room database queries with indexes
- Flow-based reactive updates minimize unnecessary recompositions

## 📄 License

This project was built as a tutorial application with beginner-friendly comments and documentation.

## 🤝 Contributing

This is a tutorial project. Feel free to:
- Report bugs
- Suggest features
- Submit pull requests

## 📞 Support

For issues or questions:
1. Check the troubleshooting section
2. Review Firebase and Google Cloud Console settings
3. Ensure all dependencies are up to date

## 🎓 Learning Resources

This codebase includes extensive comments explaining:
- Kotlin basics and syntax
- Jetpack Compose fundamentals
- Room Database operations
- Firebase integration
- Coroutines and Flow
- Repository pattern
- MVVM architecture

Perfect for learning modern Android development!

---

**Built with ❤️ using Claude Code**

🤖 Generated with [Claude Code](https://claude.com/claude-code)
