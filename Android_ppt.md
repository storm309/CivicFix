# CivicFix Presentation

Project: CivicFix - Smart Civic Waste Management App
Audience: Students and teacher
Date: May 19, 2026

---

# Agenda

- Problem statement
- Solution overview and goals
- User and admin features
- System architecture and data flow
- Tech stack and key integrations
- Security, performance, and stability
- Roadmap and demo flow
- Presenter split

---

# Problem Statement

- Urban waste issues are underreported and slow to resolve
- Reports are scattered, language barriers reduce participation
- Authorities lack structured evidence with exact locations

---

# Solution Overview

- Mobile-first reporting for citizens
- AI-assisted photo analysis for bilingual descriptions
- GPS location capture and map visibility
- Admin moderation workflow to approve or reject reports

---

# Project Goals

- Make reporting fast, simple, and bilingual (English/Hindi)
- Provide verified, structured data to authorities
- Improve response time and transparency with a public map

---

# Core User Flow

1. Login or signup (email, phone OTP, Google)
2. Capture or upload a waste photo
3. AI generates bilingual description and safety check
4. GPS location added and report submitted
5. Approved reports appear on public map

---

# Roles and Capabilities

Citizen (User Role):
- Report issues with photos and location
- View approved hotspots on the map
- Manage profile and language preference

Authority (Admin Role):
- Review pending reports
- Approve or reject with status update
- Reward points for valid reports

---

# Feature Highlights

- 22 total features across authentication, reporting, and admin tools
- Multilingual UI with instant language switching
- AI safety moderation to block unsafe images
- Material 3 UI with Compose-only screens

---

# Tech Stack Summary

- Kotlin 2.0.21, Jetpack Compose, Material 3
- MVVM architecture with ViewModel and StateFlow
- Firebase Auth, Realtime Database, Storage
- Google Gemini AI and Google Maps SDK

---

# Architecture Overview (MVVM)

- UI (Compose screens)
- ViewModels (AuthViewModel, WasteViewModel)
- Repository (WasteRepository)
- Firebase (Auth, Realtime DB, Storage)
- External services (Gemini, Maps)

---

# Data Flow (Report Submission)

- UI collects image and location
- WasteViewModel triggers AI analysis and safety check
- WasteRepository uploads image to Storage
- Realtime Database stores report metadata
- Admin dashboard listens for pending reports

---

# Database Design (Realtime DB)

- /users/{uid}: profile, role, language, points
- /reports/{reportId}:
  - description (EN)
  - descriptionHi (HI)
  - imageUrl
  - latitude / longitude
  - moderationStatus
  - reportedBy

---

# AI Integration (Gemini)

- Bilingual description generation: EN + HI
- Safety check to ensure report relevance
- Structured output parsed into fields
- Integrated directly in WasteViewModel

---

# Maps and Location

- Google Maps Compose for interactive map
- Color-coded markers for approved vs pending
- Fused Location Provider for GPS accuracy

---

# Admin Moderation Workflow

- Pending queue with photo and AI description
- Approve or reject with status updates
- Points reward on approval
- Optional permanent delete for invalid reports

---

# UI/UX Design Principles

- Compose-only UI for smooth rendering
- Material 3 components and consistent theme
- Hindi-first layout considerations to avoid clipping

---

# Security and Risks (Current State)

- API keys were exposed in code previously
- Lint error for camera feature in manifest
- Limited automated tests

Mitigation plan:
- Use Secrets Gradle Plugin for API keys
- Add camera feature declaration
- Add unit and UI test coverage

---

# Performance and Stability

- StateFlow + Compose state for reactive updates
- Lazy loading for lists and images
- Real-time sync through Firebase listeners
- Target API 35, min API 24

---

# Roadmap (Next Steps)

Short term:
- Push notifications (FCM)
- Email verification
- User avatars and badges

Mid term:
- Comments and voting
- Analytics dashboard
- Two-factor auth

Long term:
- Offline mode with Room
- Heat maps and geofencing
- Multi-language expansion

---

# Demo Flow

1. Login
2. Report waste with photo
3. AI description and submit
4. Admin approves report
5. Report appears on map

---

# Presenter Split (3 People)

Shivam:
- Problem statement and solution overview
- User flow and features
- Demo flow

Purushotam:
- Architecture and data flow
- Tech stack and database design
- AI and maps integration

Avanish:
- Admin workflow
- Security, performance, and stability
- Roadmap and conclusion

---

# Conclusion

- CivicFix delivers a complete civic reporting pipeline
- AI and bilingual UI remove barriers for citizens
- Admin moderation ensures verified, high-quality reports

---

