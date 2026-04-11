# 🌿 CivicFix – Smart Waste Management App

<p align="center">
  <img src="app/src/main/res/drawable/app_logo.png" width="120" alt="CivicFix Logo"/>
</p>

<p align="center">
  <b>Report. Track. Clean.</b><br/>
  A modern Android app to report and manage waste in your community.
</p>

---

## 📱 Features

| Feature | Description |
|---|---|
| 🔐 Authentication | Secure sign-up & login with Firebase Auth |
| 📸 Waste Reporting | Capture a photo, add a description, auto-detect location |
| 📋 View Reports | Browse all submitted waste reports with status |
| 🗺️ Map View | Visualise waste report locations on a map |
| 👤 User Profiles | Personalised profile stored in Firestore |

---

## 🛠️ Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Architecture:** MVVM (ViewModel + StateFlow)
- **Backend:** Firebase Auth · Firestore · Firebase Storage
- **Navigation:** Jetpack Navigation Compose
- **Image Loading:** Coil
- **Location:** Google Play Services FusedLocationProvider
- **Build:** Gradle (KTS) · AGP 8.7

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (or later)
- Android SDK 24+
- A Firebase project with **Auth**, **Firestore** and **Storage** enabled

### Setup

1. **Clone the repo**
   ```bash
   git clone https://github.com/your-username/SmartWasteManagementApp.git
   cd SmartWasteManagementApp
   ```

2. **Add Firebase config**
   Download `google-services.json` from your Firebase Console and place it in `app/`.

3. **Enable Firebase services** in your Firebase Console:
   - Authentication → Email/Password
   - Firestore Database → Start in test mode
   - Storage → Start in test mode

4. **Build & Run**
   ```bash
   # Build APK
   ./gradlew assembleDebug

   # Install directly on connected device / emulator
   ./gradlew installDebug
   ```

---

## 📂 Project Structure

```
app/src/main/java/com/example/smartwastemanagementapp/
├── MainActivity.kt          # Entry point, NavHost setup
├── model/
│   ├── User.kt              # User data class
│   └── WasteReport.kt       # Waste report data class
├── navigation/
│   └── Screen.kt            # Route definitions
├── repository/
│   └── WasteRepository.kt   # Firestore data operations
├── ui/
│   ├── screens/
│   │   ├── SplashScreen.kt
│   │   ├── LoginScreen.kt
│   │   ├── SignupScreen.kt
│   │   ├── HomeScreen.kt
│   │   ├── ReportWasteScreen.kt
│   │   ├── ViewReportsScreen.kt
│   │   └── MapScreen.kt
│   └── theme/
│       ├── Color.kt         # Eco-green brand palette
│       ├── Theme.kt         # Material 3 theme
│       └── Type.kt          # Typography
└── viewmodel/
    ├── AuthViewModel.kt     # Login, signup, logout
    └── WasteViewModel.kt    # Report submission & fetching
```

---

## 🔐 Firestore Collections

| Collection | Purpose |
|---|---|
| `users/{uid}` | User profile (name, email, age, phone, gender) |
| `reports/{id}` | Waste reports (description, imageUrl, lat, lng, status) |

---

## 🙏 Credits

Developed by **Shivam Pandey**

---

## 📄 License

```
MIT License – feel free to use, modify and distribute.
```
