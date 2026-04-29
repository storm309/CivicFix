# CivicFix: Smart Civic Waste Management App Documentation

Welcome to the comprehensive documentation for **CivicFix**. This document is designed to provide a clear and deep understanding of the project, from its purpose and features to its internal technical structure.

---

## 1. Project Overview
### What is the purpose of this project?
**CivicFix** is a multilingual mobile application designed to empower citizens to participate actively in keeping their cities clean. It provides a platform where users can report civic waste issues (like garbage piles, overflowing bins, etc.) directly to the authorities in their preferred language.

### What problem does it solve?
In many urban areas, reporting waste issues is slow and often limited by language barriers. CivicFix solves this by:
- Providing an **instant, digital reporting tool** in both English and Hindi.
- Using **AI (Google Gemini)** to automatically analyze photos and generate bilingual descriptions.
- Using **GPS** to pinpoint the exact location of the issue.
- Providing a **Moderation Dashboard** for admins to review and act on these reports in real-time.

---

## 2. Tech Stack
The project is built using modern Android development practices:

- **Language:** Kotlin (100%)
- **UI Framework:** Jetpack Compose (Modern declarative UI)
- **Asynchronous Programming:** Kotlin Coroutines & Flow
- **Architecture:** MVVM (Model-View-ViewModel)
- **Localization:** AppCompatDelegate for dynamic English/Hindi switching.
- **Backend/Database:** Firebase
    - **Firebase Auth:** User authentication (Email, Google, Phone/OTP).
    - **Realtime Database:** Storing user profiles and bilingual waste reports.
    - **Firebase Storage:** Storing images of reported waste.
- **AI Integration:** Google Gemini (via `generativeai` SDK) for bilingual image analysis and safety moderation.
- **Maps:** Google Maps SDK for Android with Maps Compose library.
- **Image Loading:** Coil (Compose-first image loading).

---

## 3. Project Structure
The project is organized into logical packages:

- **`com.example.smartwastemanagementapp`**
    - **`MainActivity.kt`**: The single activity that hosts the entire application and manages global locale settings.
    - **`navigation/`**: Contains `Screen.kt` which defines all routes in the app.
    - **`model/`**: Data classes representing the app's entities.
        - `User.kt`: User profile data.
        - `WasteReport.kt`: Details of a waste issue report (stores both EN and HI descriptions).
        - `AuthRole.kt`: Enum for User/Admin roles.
        - `ReportModerationStatus.kt`: Enum for Pending/Approved/Rejected states.
    - **`viewmodel/`**: Logic layers that handle data for the UI.
        - `AuthViewModel.kt`: Handles login, signup, and profile management.
        - `WasteViewModel.kt`: Handles report submission, fetching, and bilingual AI analysis.
    - **`repository/`**: Data access layer.
        - `WasteRepository.kt`: Direct interaction with Firebase (Database & Storage).
    - **`util/`**
        - **`LanguageManager.kt`**: Centralized utility for dynamic language switching and persistence.
    - **`ui/`**
        - **`screens/`**: All localized pages (Login, Home, Report, Admin, etc.).
        - **`theme/`**: Styling, colors, typography, and Material 3 theme configuration.

---

## 4. Application Flow
### Step-by-Step Working:
1.  **Splash Screen:** The app opens with an animated splash screen and detects the system language.
2.  **Auth Check:** The app checks if a user is logged in.
    - If not logged in -> **Login Screen** (with language toggle).
    - If logged in but profile incomplete -> **Complete Profile Screen**.
    - If logged in and profile complete -> **Home Screen** (or **Admin Dashboard** if admin).
3.  **Reporting:** From Home, a user clicks "Report Issue".
    - Capture Photo -> **Gemini AI** generates EN + HI descriptions -> Location pinned -> Submit.
4.  **Moderation:** Admin logs in and sees pending reports in the **Admin Console**.
    - Admin can switch language to see the report description in Hindi or English.
    - Admin Approves/Rejects.
5.  **Visibility:** Once approved, the report appears on the **Waste Map** for everyone to see.

---

## 5. Feature Breakdown

### 🌐 Multilingual Support (English + Hindi)
- **Dynamic Switching**: Users can switch between English and Hindi instantly from the Login or Home screens without restarting the app.
- **Persistence**: The app remembers the user's language preference using `SharedPreferences`.
- **System Detection**: Automatically selects Hindi if the phone's system language is Hindi.

### 🔐 Authentication System
- **Email/Password:** Standard login and signup.
- **Google Sign-In:** One-tap login using Google accounts.
- **Phone/OTP:** Login using mobile number and SMS verification.
- **Role-Based Access:** Admins and Users see different interfaces.

### 📸 localized Smart Reporting (AI-Powered)
- **Gemini AI Integration:** When a user picks a photo, Gemini:
    - Writes a description in **English** AND **Hindi**.
    - Performs a "Safety Check" to ensure the image is relevant to waste.
- **GPS Pinning:** Uses Google Play Services for high-accuracy location and Geocoding for addresses.

### 🗺️ Waste Map
- Uses Google Maps to display "Hotspots" (Approved reports).
- Users can tap markers to see localized details and photos of the reported issue.

### 👑 Admin Console
- A dedicated screen for authorities to:
    - View a "Pending Queue".
    - Approve/Reject reports.
    - View descriptions in their preferred language.

---

## 6. Backend & Database
### Firebase Realtime Database
Data is structured to support localization:
- `users/$uid`: Stores name, email, role, and language preference.
- `reports/$reportId`: Stores `description` (EN), `descriptionHi` (HI), imageUrl, coordinates, and **moderationStatus**.

---

## 7. Code Explanation
### Key Classes:
- **`LanguageManager`**: Uses `AppCompatDelegate.setApplicationLocales()` to change the app's language at runtime.
- **`WasteViewModel`**: The `analyzeWasteImage()` function uses a custom prompt that forces Gemini to respond in a structured bilingual format (EN: ... HI: ...).
- **`MainActivity`**: Extends `AppCompatActivity` to support the modern Android 13+ per-app language settings API.

---

## 8. UI/UX Design
- **Material 3:** Follows the latest Android design guidelines with expressive typography that scales for Hindi scripts.
- **Localized UI**: 100% of strings are moved to `strings.xml` files, ensuring no hardcoded text in the code.
- **Responsive Layouts**: Designed to prevent text clipping for longer Hindi translations.

---

## 9. Dependencies
- `androidx.appcompat`: For localized locale management.
- `com.google.firebase:*`: For backend services.
- `com.google.ai.client.generativeai`: For Gemini AI features.
- `com.google.android.gms:play-services-maps`: For mapping and GPS.

---

## 10. Current Working State
✅ Full Multilingual Support (EN/HI)
✅ Instant Language Switching
✅ Bilingual AI Image Analysis
✅ Splash & Animations
✅ Multi-factor Authentication
✅ Admin Moderation Dashboard
✅ Public localized Waste Map

---

## 11. Improvement Suggestions
1.  **Voice Reporting:** Allow users to record a voice message in Hindi/English, which the AI then converts to a text report.
2.  **More Languages:** Add support for regional Indian languages like Marathi, Tamil, or Bengali.
3.  **Push Notifications:** Send localized alerts when a report status changes.

---

## 12. Summary
**CivicFix** is a future-ready, inclusive solution for civic engagement. By bridging the language gap with **English/Hindi support** and leveraging **Gemini AI**, it makes waste reporting accessible to everyone. It's more than a tool; it's a bilingual bridge between the public and city management.

---
*Documentation updated for CivicFix v2.0.*
