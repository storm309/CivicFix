# CivicFix

A modern Android application designed to streamline and improve waste management processes using real-time data and location-based services.

## 🚀 Features

- **User Authentication**: Secure Login and Sign-up system powered by Firebase Auth.
- **Real-time Tracking**: Integrated Google Maps to locate waste collection points and track services.
- **Cloud Storage**: Efficient data management using Firebase Firestore and Storage.
- **Modern UI/UX**: Built entirely with Jetpack Compose and Material 3 for a fluid and responsive experience.
- **Image Support**: Integration with Coil for efficient image loading.
- **Navigation**: Seamless screen transitions using Jetpack Compose Navigation.

## 🛠 Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose)
- **Design System**: [Material 3](https://m3.material.io/)
- **Backend**: [Firebase](https://firebase.google.com/) (Authentication, Firestore, Storage)
- **Maps**: [Google Maps SDK for Android](https://developers.google.com/maps/documentation/android-sdk/overview) & [Maps Compose](https://github.com/googlemaps/android-maps-compose)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Dependency Management**: Gradle Version Catalog (libs.versions.toml)

## 📸 Screenshots

*(Add your screenshots here later)*

## ⚙️ Setup Instructions

1.  **Clone the repository**:
    ```bash
    git clone https://github.com/storm309/CivicFix.git
    ```
2.  **Firebase Setup**:
    - Create a new project in the [Firebase Console](https://console.firebase.google.com/).
    - Add an Android app with the package name `com.example.smartwastemanagementapp`.
    - Download the `google-services.json` file and place it in the `app/` directory.
    - Enable Authentication (Email/Password) and Firestore.
3.  **Google Maps API Key**:
    - Obtain an API key from the [Google Cloud Console](https://console.cloud.google.com/).
    - Add the key to your `local.properties` or `AndroidManifest.xml` (depending on your setup).
4.  **Build and Run**:
    - Open the project in Android Studio.
    - Sync Gradle and run the app on an emulator or physical device.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
