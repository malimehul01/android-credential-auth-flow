# Modern Credential Auth Flow (Android)

A robust, production-grade Android authentication workflow demonstrating Google Sign-In with the **Google Credential Manager API**, **Phone Number Hint API**, and **Firebase (Auth & Firestore)**. Built strictly adhering to **Clean Architecture** and **MVVM** principles using **Jetpack Compose**.

---

## Features

- **Google Credential Manager API**: Seamless, passwordless authentication using Google's modern Identity APIs.
- **Phone Number Hint API**: Zero-permission instant mobile number selection via Google Play Services.
- **Hardware & SIM Verification**: Hardware-level detection of SIM card presence with smooth fallback to manual input.
- **Firebase Auth & Firestore Integration**: Persists user profiles and active device sessions in real time.
- **Clean Architecture + MVVM**: Strict layer separation (`domain`, `data`, `presentation`, `utils`) to prevent tight coupling.
- **Unidirectional Data Flow (UDF)**: UI state management powered by Kotlin `StateFlow` and Coroutines.
- **Edge-to-Edge Material 3 Design**: Fully dynamic theming with complete system bar insets management.

---

## Tech Stack & Architecture

### Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: Clean Architecture + MVVM
- **Authentication**: Google Credential Manager API, Firebase Auth
- **Database**: Cloud Firestore
- **Hardware/System APIs**: Google Identity Phone Hint API, TelephonyManager
- **Asynchronous Execution**: Kotlin Coroutines & StateFlow
- **Navigation**: Jetpack Navigation Compose

---

## Project Structure

```text
com.mddevlabs.credentialauth_flow/
│
├── data/                         <-- Data Layer (Implementation & Data Sources)
│   ├── local/                    <-- Local Preferences & DataStore
│   │   └── SessionPreferences.kt
│   ├── remote/                   <-- APIs & Third-Party SDKs (Credential Manager, Google APIs)
│   │   └── GoogleAuthClient.kt
│   └── repository/               <-- Repository implementations
│       └── AuthRepositoryImpl.kt
│
├── domain/                       <-- Domain Layer (Pure Business Logic & Contracts)
│   ├── model/                    <-- Business models (No third-party framework dependencies)
│   │   ├── SignInResult.kt
│   │   └── UserData.kt
│   └── repository/               <-- Abstract repository contracts/interfaces
│       └── AuthRepository.kt
│
├── presentation/                 <-- Presentation Layer (UI & State Management)
│   ├── auth/                     <-- Authentication & onboarding flows
│   │   ├── AuthViewModel.kt
│   │   ├── LoginScreen.kt
│   │   └── PhoneNumberEntryScreen.kt
│   ├── home/                     <-- Dashboard & user profile management
│   │   ├── HomeScreen.kt
│   │   └── HomeViewModel.kt
│   └── ui/theme/                 <-- Typography, Colors, and Compose Themes
│
└── utils/                        <-- Common helpers and system hardware utilities
    └── DeviceUtils.kt
```

---

## Getting Started

### Prerequisites
- Android Studio Ladybug (2024.2+) or newer
- JDK 17+
- Android Device or Emulator with **Google Play Services** enabled
- Active Firebase Project with:
  - **Firebase Authentication** (Google Sign-In provider enabled)
  - **Cloud Firestore Database** enabled

---

### Setup Instructions

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/malimehul01/android-credential-auth-flow.git
   cd android-credential-auth-flow
   ```

2. **Add Firebase Configuration**:
   - Download `google-services.json` from your Firebase Console project settings.
   - Place it inside the `app/` module:
     ```text
     app/google-services.json
     ```

3. **Configure Web Client ID**:
   - Open `data/remote/GoogleAuthClient.kt`.
   - Update `setServerClientId(...)` with your OAuth 2.0 Web Client ID generated in Google Cloud Console / Firebase Console:
     ```kotlin
     .setServerClientId("YOUR_SERVER_WEB_CLIENT_ID.apps.googleusercontent.com")
     ```

4. **Build & Run**:
   - Sync the Gradle project and launch it on a physical device or Google Play emulator to test the Google Credential Manager and Phone Hint flows.

---

## Architecture Highlights

- **Domain Isolation**: Zero Android framework dependencies in the `domain` module for high testability and clean boundaries.
- **Repository Pattern**: `AuthRepositoryImpl` abstracts all data synchronization between remote endpoints (Credential Manager, Firestore) and local session storage.
- **Unidirectional State Flow**: Composable screens observe immutable `StateFlow` streams exposed by ViewModels, preventing recomposition glitches and state leaks across configuration changes.

---

## License

```text
Copyright 2026 MD DevLabs

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    [http://www.apache.org/licenses/LICENSE-2.0]

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
