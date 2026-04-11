# 🌍 CivicFix - Waste Management App

> A modern Android application for smart waste management with real-time tracking, cloud storage, and location-based services.

**Status**: ✅ **Production-Ready** | **Version**: 1.0 | **Last Updated**: April 2026

---

## 🚀 Quick Start

```bash
# 1. Clone repository
git clone https://github.com/storm309/CivicFix.git
cd SmartWasteManagementApp

# 2. Build & run
./gradlew :app:installDebug

# 3. Done! App will run on your device/emulator
```

---

## ✨ Key Features

✅ **User Authentication** - Secure Firebase Auth with email validation  
✅ **Report Waste** - Capture photos + location data  
✅ **View Reports** - Browse all submissions with details  
✅ **Real-time Database** - Instant sync across devices  
✅ **Cloud Storage** - Reliable image hosting  
✅ **Material 3 Design** - Modern UI with dark mode  
✅ **Responsive UI** - Mobile & tablet optimized  
✅ **Accessibility** - Full a11y compliance  

---

## 🛠 Tech Stack

| Component | Technology |
|-----------|------------|
| **Language** | Kotlin 2.0.21 |
| **UI** | Jetpack Compose + Material 3 |
| **Backend** | Firebase (Auth, Realtime DB, Storage) |
| **Architecture** | MVVM |
| **Build** | Gradle 8.7.3 |
| **Min SDK** | 24 (Android 7.0) |
| **Target SDK** | 35 |

---

## 📋 Setup Instructions

### Prerequisites
- Android Studio (latest)
- Java 11+
- Firebase account

### Step 1: Firebase Configuration

1. Create project at [Firebase Console](https://console.firebase.google.com/)
2. Add Android app: `com.example.smartwastemanagementapp`
3. Download `google-services.json` → place in `app/` folder
4. Enable: Authentication (Email/Password), Realtime Database, Cloud Storage

### Step 2: Database Rules

**Database Rules** (Firebase Console → Database → Rules):
```json
{
  "rules": {
    "users": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    },
    "reports": {
      ".read": "auth != null",
      ".write": "auth != null"
    }
  }
}
```

**Storage Rules** (Firebase Console → Storage → Rules):
```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /waste_images/{allPaths=**} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.resource.size < 10 * 1024 * 1024;
    }
  }
}
```

### Step 3: Build & Run

```bash
./gradlew sync
./gradlew :app:installDebug
```

---

## 📁 Project Structure

```
app/src/main/java/com/example/smartwastemanagementapp/
├── MainActivity.kt              # App entry point
├── viewmodel/
│   ├── AuthViewModel            # Authentication logic
│   └── WasteViewModel           # Report management
├── repository/
│   └── WasteRepository          # Firebase operations
├── model/
│   ├── User.kt                  # User data
│   └── WasteReport.kt           # Report data
├── ui/screens/                  # All screen UIs
└── ui/theme/                    # Colors & typography
```

---

## 🔌 Available Commands

```bash
# Build
./gradlew :app:build                  # Build debug
./gradlew clean :app:build            # Clean build

# Run
./gradlew :app:installDebug           # Install & run on device
./gradlew :app:assembleDebug          # Build APK only

# Test
./gradlew :app:testDebugUnitTest      # Unit tests
./gradlew :app:connectedAndroidTest   # Device tests

# Release
./gradlew :app:bundleRelease          # Build for Play Store
./gradlew :app:assembleRelease        # Build release APK

# View logs
adb logcat -s "CivicFix"
```

---

## 🏛️ Architecture Overview

### MVVM Pattern
- **ViewModel**: Manages UI state (auth, reports, errors)
- **Repository**: Handles Firebase operations
- **Model**: Data classes (User, WasteReport)
- **UI**: Compose screens

### Navigation Flow
```
Splash (2s) → Home (logged in) / Login (not logged in)
                    ↓
            Report / View / Map / Logout
```

### State Management
- Single `AuthViewModel` + `WasteViewModel` in MainActivity
- Error states cleared on new operations
- Guards against duplicate network calls

---

## 🎨 Design Features

✅ **Material 3** - Modern design system  
✅ **Green Palette** - Civic environmental branding  
✅ **Dark Mode** - Full dark theme support  
✅ **Responsive** - Mobile (360dp) & tablet (600dp+)  
✅ **Animations** - Smooth error transitions  
✅ **Accessibility** - Content descriptions, 56dp buttons  

---

## 🔒 Security

✅ Email regex validation  
✅ Password strength check (6+ chars)  
✅ Firebase Auth enabled  
✅ Database rules lock to authenticated users  
✅ No hardcoded API keys  
✅ Input trimming (injection prevention)  

**Before Release**: Enable ProGuard minification

---


---

## 🐛 Troubleshooting

| Issue | Solution |
|-------|----------|
| Gradle sync fails | `./gradlew clean && ./gradlew sync` |
| App crashes on start | Check `adb logcat \| grep -i crash` |
| Firebase not working | Verify `google-services.json` in `app/` |
| Maps not showing | Add Google Maps API key (optional) |

See **AGENTS.md** for detailed troubleshooting.

---

## 📄 License

MIT License - see [LICENSE](LICENSE) file

---

## 🤝 Contributing

```bash
git checkout -b feature/your-feature
git commit -m "Add feature description"
git push origin feature/your-feature
```

Then open a Pull Request on GitHub.

---

## 👨‍💻 Created by

**Shivam Pandey** - Full-stack Android Developer

---

**Built with ❤️ for better waste management**


