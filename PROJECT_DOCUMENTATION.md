# CivicFix: Smart Civic Waste Management App Documentation

Welcome to the comprehensive documentation for **CivicFix**. This document is designed to provide a clear and deep understanding of the project, from its purpose and features to its internal technical structure.

---

## 1. Project Overview
### What is the purpose of this project?
**CivicFix** is a mobile application designed to empower citizens to participate actively in keeping their cities clean. It provides a platform where users can report civic waste issues (like garbage piles, overflowing bins, etc.) directly to the authorities.

### What problem does it solve?
In many urban areas, reporting waste issues is a cumbersome process involving phone calls or physical visits to municipal offices, which often results in slow responses. CivicFix solves this by:
- Providing an **instant, digital reporting tool**.
- Using **AI (Google Gemini)** to automatically analyze photos and generate descriptions.
- Using **GPS** to pinpoint the exact location of the issue.
- Providing a **Moderation Dashboard** for admins to review and act on these reports.

---

## 2. Tech Stack
The project is built using modern Android development practices:

- **Language:** Kotlin (100%)
- **UI Framework:** Jetpack Compose (Modern declarative UI)
- **Asynchronous Programming:** Kotlin Coroutines & Flow
- **Architecture:** MVVM (Model-View-ViewModel)
- **Backend/Database:** Firebase
    - **Firebase Auth:** User authentication (Email, Google, Phone/OTP).
    - **Realtime Database:** Storing user profiles and waste reports.
    - **Firebase Storage:** Storing images of reported waste.
- **AI Integration:** Google Gemini (via `generativeai` SDK) for image analysis and safety moderation.
- **Maps:** Google Maps SDK for Android with Maps Compose library.
- **Image Loading:** Coil (Compose-first image loading).
- **Dependency Management:** Gradle (Kotlin DSL).

---

## 3. Project Structure
The project is organized into logical packages:

- **`com.example.smartwastemanagementapp`**
    - **`MainActivity.kt`**: The single activity that hosts the entire application navigation.
    - **`navigation/`**: Contains `Screen.kt` which defines all routes in the app.
    - **`model/`**: Data classes representing the app's entities.
        - `User.kt`: User profile data.
        - `WasteReport.kt`: Details of a waste issue report.
        - `AuthRole.kt`: Enum for User/Admin roles.
        - `ReportModerationStatus.kt`: Enum for Pending/Approved/Rejected states.
    - **`viewmodel/`**: Logic layers that handle data for the UI.
        - `AuthViewModel.kt`: Handles login, signup, and profile management.
        - `WasteViewModel.kt`: Handles report submission, fetching, and AI analysis.
    - **`repository/`**: Data access layer.
        - `WasteRepository.kt`: Direct interaction with Firebase (Database & Storage).
    - **`ui/`**
        - **`screens/`**: All the different pages of the app (Login, Home, Report, etc.).
        - **`theme/`**: Styling, colors, typography, and Material 3 theme configuration.

---

## 4. Application Flow
### Step-by-Step Working:
1.  **Splash Screen:** The app opens with an animated splash screen for 2.5 seconds.
2.  **Auth Check:** The app checks if a user is logged in.
    - If not logged in -> **Login Screen**.
    - If logged in but profile incomplete -> **Complete Profile Screen**.
    - If logged in and profile complete -> **Home Screen** (or **Admin Dashboard** if admin).
3.  **Reporting:** From Home, a user can click "Report Issue".
    - Capture/Select Photo -> AI Analyzes (Gemini) -> Location is pinned -> Submit.
4.  **Moderation:** Admin logs in and sees pending reports in the **Admin Console**.
    - Admin Approves/Rejects.
5.  **Visibility:** Once approved, the report appears on the **Waste Map** for everyone to see.

---

## 5. Feature Breakdown

### 🔐 Authentication System
- **Email/Password:** Standard login and signup.
- **Google Sign-In:** One-tap login using Google accounts.
- **Phone/OTP:** Login using mobile number and SMS verification.
- **Role-Based Access:** Admins and Users see different interfaces.

### 📸 Smart Reporting (AI-Powered)
- **Gemini AI Integration:** When a user picks a photo, the app uses Google's Gemini model to:
    - Automatically write a description of the waste.
    - Perform a "Safety Check" to ensure the image is relevant to waste and not abusive.
- **GPS Pinning:** Uses Google Play Services to get high-accuracy location and Geocoding to find the address.

### 🗺️ Waste Map
- Uses Google Maps to display "Hotspots" (Approved reports).
- Users can tap markers to see details and photos of the reported issue.

### 👑 Admin Console
- A dedicated screen for authorities to:
    - View a "Pending Queue".
    - Approve reports (which pins them on the map).
    - Reject reports with a reason note.

---

## 6. Backend & Database
### Firebase Realtime Database
Data is structured in two main nodes:
- `users/$uid`: Stores name, email, phone, age, and **role** ("user" or "admin").
- `reports/$reportId`: Stores description, imageUrl, coordinates, reporter ID, and **moderationStatus**.

### Firebase Storage
- Path: `waste_images/`. Stores the high-quality JPG images uploaded by users.

### Security
- Firebase rules are configured to require authentication for writing reports.
- Admin roles are determined by a list of `ADMIN_EMAILS` defined in the app's configuration.

---

## 7. Code Explanation
### Key Classes:
- **`AuthViewModel`**: Manages the `userProfile` state. It uses `syncRoleFlags()` to check if the current user is an admin based on their email or database role.
- **`WasteViewModel`**: Contains the `analyzeWasteImage()` function. This is where the magic happens—it sends the image to Gemini AI and parses the response for a description and a safety score.
- **`WasteRepository`**: Encapsulates the Firebase logic. It handles the two-step process of uploading an image to Storage first, then saving the resulting URL and metadata to the Realtime Database.

---

## 8. UI/UX Design
- **Material 3:** The app follows the latest Android design guidelines with rounded corners, elevated cards, and expressive typography.
- **Declarative UI:** Built with Jetpack Compose, making the UI highly reactive to state changes (e.g., showing a loading spinner while the AI is thinking).
- **Animations:** Uses `AnimatedVisibility` and `animateFloatAsState` for smooth transitions between screens and elements.

---

## 9. Dependencies
- `androidx.compose.*`: For the modern UI.
- `com.google.firebase:*`: For backend services.
- `com.google.android.gms:play-services-maps/location`: For mapping and GPS.
- `com.google.ai.client.generativeai`: To communicate with Gemini AI.
- `io.coil-kt:coil-compose`: For efficient image loading and caching.

---

## 10. Current Working State
✅ Splash & Animations
✅ Multi-factor Authentication (Email, Google, Phone)
✅ Profile Management
✅ AI Image Analysis & Moderation
✅ GPS Location & Geocoding
✅ Report Submission & Firebase Sync
✅ Admin Moderation Dashboard
✅ Public Waste Map

---

## 11. Improvement Suggestions
1.  **Offline Support:** Use Room database to cache reports so users can view them without internet.
2.  **Push Notifications:** Notify users when their report is approved or when an admin rejects it.
3.  **Community Social Features:** Allow users to "upvote" a report to show its urgency.
4.  **Leaderboard:** Gamify the experience by giving "Cleanliness Points" to active reporters.

---

## 12. Summary
**CivicFix** is a robust, end-to-end solution for civic engagement. By combining **Jetpack Compose** for a beautiful UI, **Firebase** for a scalable backend, and **Google Gemini AI** for smart automation, it transforms the way citizens interact with municipal authorities. It's not just a reporting tool; it's a smart bridge between the public and city management.

---
*Documentation generated for Project CivicFix.*
