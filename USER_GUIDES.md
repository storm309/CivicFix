# CivicFix - Final User & Admin Guides

This document explains the real-time working of the CivicFix app for both Normal Users and Administrators.

---

## 👤 Normal User Guide (The Citizen)

**1. Secure Access**
*   Login using your **Google account** (using the new official Gmail button) or standard Email/Password.
*   Upon first login, complete your profile by adding your age, gender, and **phone number**. You can edit these details later via the "My Profile" action card or the edit icon on the home screen.

**2. Smart Reporting (AI-Powered)**
*   Navigate to **"Report Issue"**.
*   Capture or upload a photo of the waste.
*   **The AI Magic**: The app uses **Gemini 2.5 Flash** (the latest available model for your key). It will:
    *   Automatically generate a professional 1-2 sentence description.
    *   Verify the image safety and relevance.
*   The GPS coordinates are captured automatically for precision.

**3. Tracking Progress**
*   Go to **"View All Reports"** to see your personal history.
*   Reports have statuses: **Pending Approval** (Submitted but not reviewed), **Approved** (Verified by Admin), or **Rejected**.
*   Once approved, your report appears on the global **Waste Map**.

---

## 👮 Administrator Guide (The Authority)

**1. Admin Identification**
*   Login using the designated admin email: `shivamkumarp447@gmail.com`.
*   The app automatically detects this email and grants access to the **Admin Console**.

**2. Moderation Queue**
*   The Admin Console shows a real-time list of **Pending Reports** from all users.
*   For each report, you can view the AI-generated description and the location.
*   **Approval**: Clicking "Approve" immediately validates the report and adds it to the public Waste Map.
*   **Rejection**: Clicking "Reject" hides the report from public view.

**3. Real-Time Management**
*   All actions (Approve/Reject) are synchronized instantly via Firebase. 
*   Admins can switch to the **User View** at any time to see the global map or test the reporting flow.

---

## ⚙️ Technical Configuration (Gemini 2.5)
The AI engine is now configured to try the following models in order:
1.  **gemini-2.5-flash** (Primary - verified as available on your key)
2.  **gemini-2.0-flash** (Backup)
3.  **gemini-1.5-flash** (Legacy backup)

This ensures maximum uptime and the most accurate AI descriptions possible.
