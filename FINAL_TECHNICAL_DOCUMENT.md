# CivicFix – Complete Technical Documentation

> Single-source technical explanation generated from the actual codebase.

---

## 1. Project Overview

**Project Name:** CivicFix – Smart Civic Waste Management App

**Purpose of the Project:**
Enable citizens to report civic waste issues with photos, AI-assisted description, and GPS location; allow admins to moderate and approve reports.

**Problem Statement:**
Municipal waste issues are underreported, unstructured, and slow to resolve due to poor visibility and lack of centralized tracking.

**Main Objective:**
Create a mobile-first civic reporting system with authentication, geolocation, AI assistance, and admin moderation.

**Key Features (from code + README):**
- Email/password, phone OTP, and Google Sign‑In authentication.
- AI-powered waste image analysis with bilingual (English/Hindi) descriptions.
- GPS capture + manual address entry.
- Firebase Realtime Database for reports and profiles.
- Firebase Storage for images.
- Admin moderation (approve/reject) and reward points.
- Google Maps view with markers for approved reports.
- Multilingual UI toggle (English/Hindi).

**Real‑world Use Case:**
Citizens report local waste piles; authorities verify reports and track cleanup status; community sees approved reports on a map.

**Target Users:**
- Citizens (reporting and tracking)
- Municipal/admin moderators (verification and triage)

---

## 2. Complete Tech Stack Analysis

> All technologies are confirmed from `app/build.gradle.kts`, `gradle/libs.versions.toml`, and code usage.

### Core Platform
| Technology | What it is | Why used | Where used | Advantages | Integration |
|---|---|---|---|---|---|
| Kotlin 2.0.21 | Android language | Modern Android dev | Entire app | Null safety, coroutines | Works with Jetpack + Firebase |
| Android SDK (min 24, target 35) | Platform SDK | Device compatibility | `app/build.gradle.kts` | Modern APIs | Compose + Play Services |

### UI / Frontend
| Tech | What it is | Why used | Where used | Advantages | Integration |
|---|---|---|---|---|---|
| Jetpack Compose | Declarative UI | 100% UI layer | `ui/screens/*`, `MainActivity.kt` | Faster UI iteration | Works with ViewModel state |
| Material 3 | Design system | Consistent UI | `ui/theme/*`, `MaterialTheme` | Modern look | Compose components |
| Navigation Compose | In-app navigation | Screen routing | `MainActivity.kt`, `navigation/Screen.kt` | Simple routing | Compose destinations |

### State Management / Architecture
| Tech | What it is | Why used | Where used | Advantages | Integration |
|---|---|---|---|---|---|
| ViewModel | Lifecycle-aware state | Separates UI & logic | `AuthViewModel.kt`, `WasteViewModel.kt` | Survives config changes | Compose state + flows |
| MutableState / StateFlow | State holders | UI reactivity | ViewModels + UI | Reactive updates | Compose `collectAsState()` |
| MVVM | Architecture pattern | Clean separation | Whole app | Testable, modular | UI ↔ ViewModel ↔ Repository |

### Backend / Cloud
| Tech | What it is | Why used | Where used | Advantages | Integration |
|---|---|---|---|---|---|
| Firebase Auth | Auth service | Email/phone/Google auth | `AuthViewModel.kt` | Secure, scalable | OAuth + OTP |
| Firebase Realtime Database | NoSQL DB | Store users/reports | `AuthViewModel.kt`, `WasteRepository.kt` | Real-time updates | `ValueEventListener` |
| Firebase Storage | File storage | Store report images | `WasteRepository.kt` | CDN + scalability | Upload via `putBytes` |

### AI / ML
| Tech | What it is | Why used | Where used | Advantages | Integration |
|---|---|---|---|---|---|
| Gemini API (generativeai) | Google Generative AI SDK | Auto-descriptions + safety | `WasteViewModel.kt` | AI insights | Uses `GenerativeModel` |

### Maps / Location
| Tech | What it is | Why used | Where used | Advantages | Integration |
|---|---|---|---|---|---|
| Google Maps SDK | Map display | Visual report map | `MapScreen.kt` | Familiar UX | `maps-compose` |
| Fused Location Provider | Location API | GPS capture | `ReportWasteScreen.kt` | Accurate location | `LocationServices` |

### Media / Images
| Tech | What it is | Why used | Where used | Advantages | Integration |
|---|---|---|---|---|---|
| Coil | Image loader | Display report photos | `AdminDashboardScreen.kt`, `ReportWasteScreen.kt` | Cached, Compose-ready | `rememberAsyncImagePainter` |

### Local Storage / Preferences
| Tech | What it is | Why used | Where used | Advantages | Integration |
|---|---|---|---|---|---|
| SharedPreferences | Simple key-value | Language selection | `LanguageManager.kt` | Lightweight | `AppCompatDelegate` |

---

## 3. Folder & File Structure Explanation

**Root:**
- `build.gradle.kts`, `settings.gradle.kts`, `gradle/libs.versions.toml`: Build configuration and dependency versions.
- `README.md`: Feature overview and architecture summary.

**App module (`app/src/main/java/com/example/smartwastemanagementapp`):**
- `MainActivity.kt`: App entry point and navigation graph.
- `navigation/Screen.kt`: Route definitions.
- `model/`: Data models (`User`, `WasteReport`, `AuthRole`, `ReportModerationStatus`).
- `repository/WasteRepository.kt`: Firebase data access + storage uploads.
- `viewmodel/`: Business logic & state (`AuthViewModel`, `WasteViewModel`).
- `ui/screens/`: Compose UI screens (login, signup, report, map, admin, etc.).
- `ui/theme/`: Colors, typography, and Material 3 theme.
- `util/LanguageManager.kt`: Locale handling.

**Data flow between modules:**
```
UI (Compose) -> ViewModel (state + logic) -> Repository (Firebase) -> Firebase DB/Storage
UI observes ViewModel state (State/StateFlow) and updates automatically.
```

---

## 4. Complete App Workflow

**App Start:**
1. `MainActivity.kt` sets Compose content and navigation.
2. `SplashScreen` decides route based on `AuthViewModel` state.

**Navigation Flow:**
- Splash → Login
- Login → Complete Profile (if incomplete)
- Login → Home or Admin Dashboard (based on role)

**User Interaction Flow (Report Waste):**
1. User opens `ReportWasteScreen`.
2. Captures/chooses image (optional).
3. AI analysis generates description + safety score (`WasteViewModel.analyzeWasteImage`).
4. GPS location fetch + manual address.
5. Submit report → `WasteRepository.submitReport` (Storage + Realtime DB).

**API Communication Flow:**
- Firebase SDK handles requests internally.
- Gemini API called via `GenerativeModel`.

**Database Operations:**
- Users stored in `/users/{uid}` (Realtime DB).
- Reports stored in `/reports/{reportId}`.

**Authentication Flow:**
- Email/password: `FirebaseAuth.signInWithEmailAndPassword`.
- Phone OTP: `PhoneAuthProvider.verifyPhoneNumber`.
- Google Sign-In: `GoogleSignIn` + `GoogleAuthProvider`.

**Data Storage Flow:**
- Images: Firebase Storage `waste_images/`.
- Metadata: Realtime Database.

---

## 5. Architecture Analysis

**Pattern:** MVVM
- UI (Compose) observes ViewModel state.
- ViewModels call Repository for data.

**Repository Pattern:**
- `WasteRepository.kt` centralizes Firebase operations.

**Singleton Usage:**
- FirebaseAuth, FirebaseDatabase, FirebaseStorage singletons.
- `LanguageManager` object.

**Observer Pattern:**
- Compose `State` + `StateFlow` + Firebase listeners.

**Scalability:**
- Clear separation for adding new features (notifications, analytics).

---

## 6. Database Analysis

**Database Type:** Firebase Realtime Database (NoSQL)

**Collections (Paths):**
- `/users/{uid}` → `User` model
- `/reports/{reportId}` → `WasteReport` model

**Models:**
- `User`: name, email, age, phone, gender, role, points.
- `WasteReport`: description, imageUrl, geo coords, moderation status, AI safety, timestamps.

**Relationships:**
- Reports reference `reportedBy` (user UID).

**CRUD Operations:**
- Create: `submitReport()`
- Read: `getAllReports()`, `getPendingModerationReports()`
- Update: `updateModerationStatus()` and `upvoteReport()`
- Delete: Not implemented.

---

## 7. API & Backend Layer

**APIs Used:**
- Firebase Auth / Database / Storage SDK
- Gemini Generative AI
- Google Maps SDK

**Request/Response Handling:**
- Firebase uses SDK callbacks + `await()` for coroutines.
- Gemini responses parsed for `EN`, `HI`, `SAFETY` lines.

**Authentication Methods:**
- Email/Password, Phone OTP, Google OAuth token.

**Error Handling:**
- ViewModels map errors into user-friendly strings.
- Repository catches Firebase errors and returns `Result`.

---

## 8. UI/UX Analysis

**UI Components:**
- Compose `Scaffold`, `TopAppBar`, `Card`, `OutlinedTextField`, `LazyColumn`.
- Animated transitions (e.g., `AnimatedContent`).

**Theme/Colors:**
- Custom palette in `ui/theme/Color.kt`.
- Material 3 theme in `Theme.kt`.

**Navigation Design:**
- Central navigation in `MainActivity`.
- Role-based routing for admin.

**Responsive Behavior:**
- Uses Compose layouts and `verticalScroll`.

**Animations:**
- Tab transitions, AnimatedContent for OTP vs Email.

---

## 9. Security Features

**Authentication:** Firebase Auth.

**Authorization:** Admin access determined by email list in `BuildConfig.ADMIN_EMAILS`.

**API Key Handling:**
- API keys loaded from `local.properties` into `BuildConfig` (see `app/build.gradle.kts`).
- `AndroidManifest.xml` uses `${MAPS_API_KEY}` placeholder.

**Secure Storage:**
- Keys not hardcoded in code (loaded via Gradle). Keys should NOT be committed.

**Permissions:**
- `INTERNET`, `CAMERA`, `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`.

---

## 10. AI/ML Features

**AI Model:** Google Gemini via `generativeai` SDK.

**Prompt Flow:**
- Image + structured prompt in `WasteViewModel.analyzeWasteImage`.
- Response format:
  - `EN:` English description
  - `HI:` Hindi description
  - `SAFETY:` safety score + label

**Input/Output Handling:**
- Bitmap input → AI output parsed → descriptions + moderation result.

---

## 11. Performance Optimization

**Techniques Used:**
- Lazy loading: `LazyColumn`.
- Image caching: Coil.
- Coroutine usage: `viewModelScope` + `await()`.
- Simple error mapping to reduce crashes.

**Opportunities:**
- Add pagination for reports.
- Cache map markers.

---

## 12. Challenges & Solutions

**Challenges (from code evidence):**
- AI model failures: handled with fallback model list and error mapping.
- Image moderation: blocked unsafe images via AI safety score.
- Location accuracy: stored and visible to user.

**Solutions:**
- `WasteViewModel` tries multiple Gemini models.
- `WasteRepository` logs and returns structured `Result`.

---

## 13. Future Improvements

- Add push notifications (FCM).
- Add offline caching for reports.
- Advanced analytics dashboard for admins.
- Add test coverage (unit + UI).

---

## 14. Important Code Explanation

**MainActivity (`MainActivity.kt`):**
- Sets theme and navigation graph.
- Role-based routing to Admin vs Home.

**AuthViewModel (`AuthViewModel.kt`):**
- Handles login, signup, OTP, Google Sign-In.
- Tracks `isLoggedIn`, `isAdmin`, `userProfile`.

**WasteViewModel (`WasteViewModel.kt`):**
- Fetches reports, pending reports.
- Submits reports with AI moderation.
- Manages approval/rejection.

**WasteRepository (`WasteRepository.kt`):**
- Uploads images to Firebase Storage.
- Writes report objects to Realtime DB.

---

## 15. PPT Preparation Section

**Slide Titles + Content:**
1. **Project Introduction** – CivicFix purpose, goals.
2. **Problem Statement** – Waste reporting gaps.
3. **Solution Overview** – App flow summary.
4. **Tech Stack** – Kotlin, Compose, Firebase, Gemini, Maps.
5. **Architecture (MVVM)** – Diagram and layers.
6. **Authentication Flow** – Email/OTP/Google.
7. **Waste Report Flow** – Capture → AI → Submit.
8. **Admin Moderation** – Approve/reject + points.
9. **Database Design** – User/report schemas.
10. **Maps & Location** – Marker display.
11. **AI Integration** – Prompt flow + output.
12. **Security & Permissions** – Keys, auth, rules.
13. **Performance** – State management + caching.
14. **Future Scope** – FCM, offline, analytics.
15. **Demo Flow** – Login → Report → Admin → Map.

---

## 16. Final Project Summary

**Key Highlights:**
- End-to-end civic reporting pipeline.
- AI‑assisted, bilingual report generation.
- Role-based admin moderation.

**Innovation Points:**
- AI safety moderation embedded in report flow.
- Integrated maps + real-time database.

**Technical Uniqueness:**
- Fully Compose UI with MVVM + Firebase + AI.

**Conclusion:**
CivicFix is a production-ready civic waste reporting platform combining modern Android UI, Firebase backend, and Gemini AI for fast, structured issue reporting and moderation.

---

## Text Diagram (Architecture)
```
[Compose UI Screens]
        |
        v
[ViewModels: AuthViewModel, WasteViewModel]
        |
        v
[Repository: WasteRepository]
        |
        v
[Firebase: Auth / Realtime DB / Storage]
        |
        v
[External: Gemini API, Google Maps]
```

