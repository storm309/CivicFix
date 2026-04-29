# CivicFix - Smart Waste Management System

CivicFix is a modern Android application designed to streamline waste management reporting and resolution using Gemini 2.0 AI and Google Maps.

## 🚀 Core Features

### 1. Smart Authentication & Roles
*   **Google Sign-In**: Secure and fast login using Google accounts.
*   **Role-Based Access**: 
    *   **Normal Users**: Citizens who report waste and track their status.
    *   **Admins**: Authority figures who moderate and resolve reports.
    *   **Admin Access**: Automatically granted to the email `shivamkumarp447@gmail.com`.
*   **Profile Management**: Users can manage their name, age, gender, and **phone number** directly in the app.

### 2. AI-Powered Waste Reporting (Gemini 2.0)
*   **AI Auto-Description**: When a user uploads a photo, the AI automatically analyzes the image and writes a concise description of the waste for them.
*   **Smart Moderation**: AI checks every image to ensure it's actual waste evidence and blocks irrelevant or unsafe photos.
*   **Real-time Feedback**: Users see a live status ("AI is analyzing...", "Submitting...") while reporting.

### 3. Location & Mapping
*   **GPS Sync**: Automatically captures the precise coordinates of the waste.
*   **Waste Map**: Approved reports appear on a global map, helping the community see identified hotspots.

---

## 📖 User Guides

### 👤 Normal User Workflow
1.  **Login**: Sign in with any Google or Email account.
2.  **Home Dashboard**: View your stats (Reports/Pending/Fixed) and quick actions.
3.  **Report Issue**: 
    *   Click "Report Issue".
    *   Take/Upload a photo. 
    *   **AI magic**: Wait for AI to write the description and check safety.
    *   Submit.
4.  **Track Progress**: Go to "View All Reports" to see if the Admin has approved your complaint.

### 👮 Admin Workflow
1.  **Login**: Must login using `shivamkumarp447@gmail.com`.
2.  **Admin Console**: You will automatically see the Admin Dashboard with a "Pending Queue".
3.  **Moderate**: Review reports submitted by citizens.
    *   **Approve**: The report moves to the public "Waste Map".
    *   **Reject**: The report is hidden from the map and marked as rejected.
4.  **Real-Time Sync**: Any action you take is instantly reflected on every citizen's app.

---

## 🛠 Technical Architecture
*   **UI**: Jetpack Compose (Material 3).
*   **Backend**: Firebase (Auth, Realtime Database, Storage).
*   **AI**: Gemini 2.0 Flash SDK.
*   **Maps**: Google Maps SDK for Android.

---

## 📁 Key Files
*   `AuthViewModel.kt`: Manages roles and user state.
*   `WasteViewModel.kt`: Handles Gemini AI analysis and submissions.
*   `HomeScreen.kt`: Personalized hub for citizens.
*   `AdminDashboardScreen.kt`: The moderation engine for authorities.
*   `ReportWasteScreen.kt`: The smart reporting interface with AI auto-fill.
