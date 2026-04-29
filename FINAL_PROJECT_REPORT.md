# 📄 CivicFix: Final Project Analysis & Feature Report

## 1. Project Overview
**CivicFix** is a high-performance, multilingual Android application built to streamline civic engagement and waste management. It acts as a digital bridge between citizens (Nagriks) and city authorities (Admins), allowing for real-time reporting of waste issues with AI-assisted verification.

- **Primary Goal**: Empower citizens to keep their city clean using modern technology.
- **Key Impact**: Reduces the time between spotting a problem and reporting it, while providing authorities with verified visual evidence and exact GPS locations.

---

## 2. Feature Breakdown

### 👤 Citizen Features (User Role)
- **Smart Waste Reporting**: Snap or upload a photo, and the app auto-generates descriptions.
- **Multilingual UI**: Switch instantly between **English** and **Hindi**.
- **Interactive Waste Map**: View approved "Hotspots" of waste issues pinned on Google Maps.
- **Profile Management**: Update name, age, and contact info to track your contribution.
- **Multi-Factor Auth**: Secure login via Email, Google Sign-In, or Phone/OTP.

### 👑 Authority Features (Admin Role)
- **Moderation Queue**: A specialized dashboard to review pending reports.
- **Visual Verification**: Direct access to high-quality photos submitted by users.
- **Bilingual Action**: Admins see the report in their selected language (EN/HI).
- **Approve/Reject Logic**: One-tap approval to pin a report on the public map or rejection with notes.

---

## 3. AI Integration (Google Gemini)
The app leverages the **Google Gemini 1.5/2.0 Flash** model to provide "Intelligence-as-a-Service":

- **Bilingual Description Generation**: When a photo is uploaded, Gemini writes a professional description in both English and Hindi simultaneously.
- **Automated Image Moderation**: The AI performs a safety check to ensure the image is actually related to waste and not inappropriate or fake.
- **Prompt Logic**:
  ```kotlin
  "Respond strictly in this format: EN: <desc> HI: <desc> SAFETY:<score>|<label>|<reason>"
  ```

---

## 4. Backend & Database Architecture
The application uses **Firebase** for a scalable, serverless backend.

### Realtime Database Structure
- **`users/$uid`**: Stores profiles, roles (Admin/User), and login history.
- **`reports/$reportId`**:
  - `description`: English text.
  - `descriptionHi`: Hindi text.
  - `imageUrl`: Link to Firebase Storage.
  - `latitude/longitude`: High-accuracy coordinates.
  - `moderationStatus`: Enum (Pending, Approved, Rejected).

### Firebase Storage
- Path: `waste_images/` - Stores high-resolution JPGs with unique UUIDs.

---

## 5. Technical Deep Dive (Important Code)

### A. Dynamic Localization (`LanguageManager.kt`)
Uses the modern Android **AppCompatDelegate** to switch languages at runtime without refreshing the entire activity stack.
```kotlin
fun setLanguage(context: Context, languageCode: String) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putString(KEY_LANGUAGE, languageCode).apply()
    val appLocale = LocaleListCompat.forLanguageTags(languageCode)
    AppCompatDelegate.setApplicationLocales(appLocale)
}
```

### B. Session Management (`AuthViewModel.kt`)
Ensures clean handovers between different user sessions by clearing all state and forcing a Google sign-out.
```kotlin
fun logout() {
    auth.signOut() // Firebase Signout
    googleSignInClient.signOut() // Prevent auto-login of same user
    _userProfile.value = null
    _isLoggedIn.value = false
    _isAdmin.value = false
}
```

---

## 6. UI/UX Design Philosophy
- **Jetpack Compose**: 100% Declarative UI ensures a smooth, non-laggy experience.
- **Material 3**: Uses the latest "You" components with rounded corners and soft elevations.
- **Hindi-First Scaling**: Layouts use dynamic constraints so that Hindi text (which is usually longer than English) doesn't clip or overlap.

---

## 7. Current Stability State
- **Crashes**: 0 identified (Fixed `AppCompat` theme dependency).
- **Network**: Robust error handling for slow internet or Firebase timeout.
- **Sync**: Instant data synchronization between user submission and admin dashboard.

---

## 8. Future Roadmap & Plans
1.  **AI Voice Reporting**: Allow users to speak in Hindi/English; the AI will transcribe it into a written report.
2.  **Reward System**: Give users "Green Points" for approved reports, which can be redeemed for local benefits.
3.  **Authority Route Optimization**: Use the Waste Map data to generate the most efficient cleaning route for municipal trucks.
4.  **Community Social Feed**: Let users "Upvote" urgent reports to prioritize them for the Admin.
5.  **Offline Support**: Use Room DB to cache reports so they can be viewed without an internet connection.

---
**Report Generated for Project CivicFix v2.1**
