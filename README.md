# 📥 YouTube Downloader App (Android)

An Android app that allows users to download YouTube videos and MP3s. Built using a combination of networking, JavaScript evaluation, background tasks, local storage, and Firebase analytics.

⚠️ **Disclaimer:** This project is for educational purposes only. Downloading content from YouTube may violate its [Terms of Service](https://www.youtube.com/t/terms).

## ✨ Features

- Download YouTube videos in MP4 format
- Extract and download audio as MP3
- View download history (saved via Room database)
- Background download support using coroutines
- Firebase Analytics and Crashlytics integration

## 🛠 Tech Stack

- **Language**: Kotlin
- **Networking**:
  - Retrofit
  - Ktor (for advanced network operations)
- **Data Parsing**:
  - Gson Converter (Retrofit)
  - kotlinx.serialization-json
- **JS Parsing**:
  - JS Evaluator (used for extracting streaming URLs)
- **Persistence**:
  - Room (with KTX and coroutines support)
- **Background Tasks**:
  - Kotlin Coroutines
- **Firebase**:
  - Firebase Analytics
  - Firebase Crashlytics

## 📦 Dependencies

```kotlin
// Retrofit
implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.squareup.retrofit2:converter-gson:2.9.0'

// JS Evaluator
implementation 'com.github.evgenyneu:js-evaluator-for-android:v4.0.0'

// JSON Parsing
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.0")

// Room
implementation("androidx.room:room-runtime:$roomVersion")
annotationProcessor("androidx.room:room-compiler:$roomVersion")
implementation("androidx.room:room-ktx:$roomVersion")
ksp("androidx.room:room-compiler:$roomVersion")

// Ktor
implementation "io.ktor:ktor-client-android:1.2.5"

// Coroutines
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3'
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.3'

// Firebase
implementation platform('com.google.firebase:firebase-bom:29.0.0')
implementation 'com.google.firebase:firebase-analytics-ktx'
implementation 'com.google.firebase:firebase-crashlytics-ktx'
```
# Legal Notice
This app is for educational use only. Downloading copyrighted content from YouTube without permission is against YouTube’s terms and potentially illegal in some jurisdictions. Use responsibly.
# License
This project is open-source and available under the MIT License.
