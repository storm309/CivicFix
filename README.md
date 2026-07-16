# 🌿 CivicFix – Smart Civic Waste Management App

<p align="center">
  <img src="app/src/main/res/drawable/app_logo.png" width="120" alt="CivicFix Logo"/>
</p>

<p align="center">
  <strong>Report. Track. Fix. Make Your City Cleaner.</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Android-Material%203-green?style=flat-square&logo=android"/>
  <img src="https://img.shields.io/badge/Kotlin-Jetpack%20Compose-blue?style=flat-square&logo=kotlin"/>
  <img src="https://img.shields.io/badge/Firebase-Authentication%20%7C%20Database%20%7C%20Storage-orange?style=flat-square&logo=firebase"/>
  <img src="https://img.shields.io/badge/Gemini-AI-purple?style=flat-square"/>
  <img src="https://img.shields.io/badge/Google%20Maps-API-red?style=flat-square"/>
</p>

---

## 📖 About

**CivicFix** is an AI-powered Android application that enables citizens to report waste-related issues in their locality. Users can capture images, analyze them using Google Gemini AI, automatically detect location, and submit reports to local authorities. Administrators can review, manage, and update report statuses through an integrated dashboard.

The application is built completely with **Jetpack Compose** following the **MVVM architecture** and uses **Firebase** as the backend.

---

# ✨ Features

### 🔐 Authentication
- Email & Password Login
- Phone OTP Authentication
- Google Sign-In
- Role-Based Access (User/Admin)

### ♻️ Waste Reporting
- Capture Image using Camera
- Select Image from Gallery
- AI Waste Detection using Gemini
- Automatic Waste Description
- GPS Location Detection
- Firebase Storage Upload

### 📍 Reports
- Submit Waste Reports
- View All Reports
- Filter Reports by Status
- Google Maps Integration
- Real-Time Location
- Color-Coded Map Markers

### 👨‍💼 Admin
- Admin Dashboard
- Report Approval/Rejection
- Moderation System
- Status Tracking

---

# 🏗️ Architecture

- MVVM Architecture
- Jetpack Compose
- StateFlow
- ViewModel
- Repository Pattern
- Firebase Backend

---

# 📱 Screens

- Splash Screen
- Login
- Signup
- Complete Profile
- Home
- Report Waste
- View Reports
- Google Map
- Admin Dashboard

---

# 🛠 Tech Stack

## Android
- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- ViewModel
- Coroutines

## Backend
- Firebase Authentication
- Firebase Realtime Database
- Firebase Storage

## AI
- Google Gemini API

## Maps & Location
- Google Maps Compose
- Fused Location Provider

## Libraries
- Coil
- Firebase SDK
- Google Play Services
- Credential Manager

---

# 📂 Project Structure

```text
app/
│
├── model/
├── repository/
├── navigation/
├── viewmodel/
├── ui/
│   ├── screens/
│   └── theme/
│
└── MainActivity.kt
```

---

# 🚀 Getting Started

## Prerequisites

- Android Studio Hedgehog+
- Android SDK 35
- Java 11+
- Kotlin 2.x

---

## Firebase Setup

1. Create a Firebase Project.
2. Enable:
   - Authentication
   - Realtime Database
   - Storage
3. Download `google-services.json`
4. Place it inside:

```text
app/
```

5. Add your API keys inside `local.properties`

```properties
GEMINI_API_KEY=YOUR_API_KEY
MAPS_API_KEY=YOUR_API_KEY
```

---

# ▶️ Run the Project

```bash
git clone https://github.com/yourusername/CivicFix.git

cd CivicFix

./gradlew assembleDebug
```

Install on device

```bash
./gradlew installDebug
```

---

# 🎯 Future Improvements

- Push Notifications
- Offline Mode
- Email Verification
- User Profile
- Comments & Discussion
- Multi-language Support
- Heat Maps
- Analytics Dashboard

---

# 📊 Project Status

| Module | Status |
|---------|--------|
| Authentication | ✅ |
| AI Integration | ✅ |
| Firebase | ✅ |
| Google Maps | ✅ |
| Admin Panel | ✅ |
| MVVM Architecture | ✅ |
| Material 3 UI | ✅ |

---

# 🤝 Contributing

Contributions are welcome.

1. Fork the repository
2. Create a feature branch

```bash
git checkout -b feature-name
```

3. Commit changes

```bash
git commit -m "Add new feature"
```

4. Push

```bash
git push origin feature-name
```

5. Open a Pull Request.

---

# 📄 License

This project is intended for educational and civic innovation purposes.

---

# 👨‍💻 Developer

**Shivam Pandey**

Made with ❤️ using Kotlin, Jetpack Compose, Firebase & Google Gemini AI.

---

## ⭐ If you like this project, don't forget to star the repository!
