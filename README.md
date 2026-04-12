# 🌿 CivicFix – Smart Civic Waste Management App

<p align="center">
  <img src="app/src/main/res/drawable/app_logo.png" width="120" alt="CivicFix Logo"/>
</p>

<p align="center">
  <strong>Report. Track. Fix. Make your city cleaner.</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Android-Material%203-green?style=flat-square&logo=android"/>
  <img src="https://img.shields.io/badge/Kotlin-Jetpack%20Compose-blue?style=flat-square&logo=kotlin"/>
  <img src="https://img.shields.io/badge/Firebase-Realtime%20DB%20%7C%20Auth%20%7C%20Storage-orange?style=flat-square&logo=firebase"/>
  <img src="https://img.shields.io/badge/Gemini%20AI-2.0%20Flash-purple?style=flat-square"/>
  <img src="https://img.shields.io/badge/Google%20Maps-Compose-red?style=flat-square"/>
</p>

---

## 📱 About

**CivicFix** is a production-ready Android application that empowers citizens to report, track, and resolve civic waste issues in their city. Built with **Jetpack Compose** and **Material 3**, it delivers a premium, intuitive experience that makes civic participation effortless.

---

## ✨ Features

| Feature | Description |
|---|---|
| 🔐 **Firebase Auth** | Secure email/password login & registration |
| 📸 **Photo Reporting** | Capture or select waste photos from camera/gallery |
| 🤖 **Gemini AI Analysis** | Auto-generate issue descriptions using Google Gemini 2.0 Flash |
| 📍 **GPS Location** | Auto-detect and pin your exact report location |
| 🗺️ **Interactive Map** | View all reported locations with color-coded markers |
| 📋 **Reports Dashboard** | Filter reports by status (All / Pending / Cleaned) |
| 👤 **User Profiles** | Name, gender, age, and +91 phone number with secure storage |
| 🌙 **Dark Mode** | Full light & dark theme support |

---

## 🎨 Design System

### Color Palette

| Token | Color | Hex |
|---|---|---|
| Primary | Forest Green | `#006D41` |
| Secondary | Deep Teal | `#006B5E` |
| Tertiary | Amber Orange | `#B45300` |
| Background | Mint White | `#F4FEF7` |
| Pending Status | Deep Orange | `#E65100` |
| Cleaned Status | Forest Green | `#1B5E20` |

### Architecture
- **Pattern:** MVVM (ViewModel + StateFlow + Compose State)
- **Navigation:** Jetpack Navigation Compose
- **Backend:** Firebase Realtime Database, Firebase Auth, Firebase Storage

---

## 📂 Project Structure

```
app/src/main/java/
├── MainActivity.kt              # Entry point, navigation host
├── model/
│   ├── User.kt
│   └── WasteReport.kt
├── navigation/
│   └── Screen.kt
├── repository/
│   └── WasteRepository.kt       # Firebase CRUD operations
├── viewmodel/
│   ├── AuthViewModel.kt
│   └── WasteViewModel.kt        # Gemini AI + Firebase logic
└── ui/
    ├── theme/
    │   ├── Color.kt             # Premium eco-tech palette
    │   ├── Theme.kt             # Material 3 color schemes
    │   └── Type.kt              # Full typography scale
    └── screens/
        ├── SplashScreen.kt
        ├── LoginScreen.kt
        ├── SignupScreen.kt
        ├── HomeScreen.kt
        ├── ReportWasteScreen.kt
        ├── ViewReportsScreen.kt
        └── MapScreen.kt
```

---

## 🚀 Setup & Run

### Prerequisites
- Android Studio Hedgehog or newer
- Android device / emulator (API 24+)
- Google account for Firebase

### Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-username/CivicFix.git
   ```

2. **Firebase Setup**
   - Create a project at [console.firebase.google.com](https://console.firebase.google.com)
   - Enable **Authentication** (Email/Password)
   - Enable **Realtime Database** and **Storage**
   - Download `google-services.json` → place in `app/`

3. **Run**
   ```bash
   ./gradlew assembleDebug
   ./gradlew installDebug
   ```

---

## 🛠️ Tech Stack

| Technology | Purpose |
|---|---|
| Kotlin + Jetpack Compose | UI & logic |
| Material 3 | Design system |
| Firebase Auth | User authentication |
| Firebase Realtime Database | Reports & user data storage |
| Firebase Storage | Image uploads |
| Google Maps Compose | Interactive map with markers |
| Google Gemini 2.0 Flash | AI-powered waste image analysis |
| Coil | Async image loading |
| Android Location Services | GPS detection |

---

## 📸 Screens Overview

- **Splash Screen** – Animated logo with gradient background
- **Login / Signup** – Floating card design with gradient header
- **Home** – Hero banner, quick action cards, civic tips
- **Report Waste** – Numbered steps: Photo → AI Describe → GPS → Submit
- **View Reports** – Filter chips, status badges, timestamps
- **Map** – Color-coded pins (🟠 Pending · 🟢 Cleaned)

---

## 👨‍💻 Developer

Built with ❤️ by **Shivam Pandey**

---

*Making cities cleaner, one report at a time.*
