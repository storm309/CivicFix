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

**CivicFix** is a production-ready Android application built with **100% Jetpack Compose** and **Material 3** that empowers citizens to report civic waste issues using AI-powered photo analysis. The app features real-time GPS tracking, interactive mapping, Firebase authentication, and an admin dashboard for moderation.

---

## ✨ Features

### Authentication
- 🔐 Email/Password sign in & sign up
- 📱 Phone OTP verification
- 🔵 Google Sign-In
- 👤 Role-based access (Admin/User)

### Waste Reporting
- 📸 Photo capture & gallery selection
- 🤖 AI-powered image analysis (Gemini 2.0 Flash)
- 📝 Auto-generated waste descriptions
- 📍 GPS location auto-detection
- 🛡️ AI safety moderation (blocks unsafe images)
- 💾 Image upload to Firebase Storage

### Reports Management
- 📋 View all submitted reports
- 🔍 Filter by status (Pending/Cleaned/All)
- 📊 Report details display
- 🗺️ Interactive Google Map
- 🎨 Color-coded markers (orange=pending, green=cleaned)
- 📍 Real-time geolocation tracking

### User & Admin
- 👤 User profile with name, age, gender
- 🛡️ Admin dashboard for moderation
- ✅ Report approval/rejection system
- 📌 Moderation status tracking

---

## 🏗️ Architecture & Tech Stack

- **UI Framework:** 100% Jetpack Compose (zero XML layouts)
- **Design System:** Material 3
- **Pattern:** MVVM (ViewModel + StateFlow + Compose State)
- **Navigation:** Jetpack Navigation Compose
- **Backend:** Firebase (Auth, Realtime Database, Storage)
- **Mapping:** Google Maps Compose
- **AI Integration:** Google Gemini 2.0 Flash API

### Screens (9 Total)
1. **SplashScreen** – Animated intro with auto-navigation
2. **LoginScreen** – Email/Phone OTP/Google sign-in
3. **SignupScreen** – Registration with profile completion
4. **CompleteProfileScreen** – User profile setup
5. **HomeScreen** – Dashboard with quick actions
6. **ReportWasteScreen** – 5-step report wizard
7. **ViewReportsScreen** – Filterable reports list
8. **MapScreen** – Interactive Google Map
9. **AdminDashboardScreen** – Report moderation queue

---

## 📂 Project Structure

```text
app/src/main/java/com/example/smartwastemanagementapp/
├── MainActivity.kt                  # Entry point (Compose)
├── model/
│   ├── User.kt
│   ├── WasteReport.kt
│   ├── AuthRole.kt
│   └── ReportModerationStatus.kt
├── navigation/
│   └── Screen.kt                    # Route definitions
├── repository/
│   └── WasteRepository.kt           # Firebase operations
├── viewmodel/
│   ├── AuthViewModel.kt             # Auth logic
│   └── WasteViewModel.kt            # AI + Reports logic
└── ui/
    ├── screens/                     # 9 @Composable screens
    └── theme/                       # Material 3 Compose theming
