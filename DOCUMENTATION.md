# AI Assistant 2.0 – Production-Ready Technical & Architecture Documentation

AI Assistant 2.0 is an advanced, offline-capable, voice-first personal Android assistant. Crafted natively in Kotlin, Jetpack Compose, Material Design 3, and Room DB, it allows users to fully control their mobile devices using spoken commands in English, Hindi, and Hinglish. It behaves as a smart personal assistant, a diagnostic trouble-shooter, and a patient voice teacher.

---

## 🏛️ System Architecture

The application adopts the **MVVM (Model-View-ViewModel)** architectural pattern. It segregates logic cleanly into presentation, domain, and data layers to maximize modularity, offline operation speed, and testability.

```
                  [ USER VOICE COMMAND ]
                            │
                            ▼
               [ AssistantSpeechEngine ] ◄──(TTS Voice Feedback)
                 │ (Speech-to-Text Flow)
                 ▼
             [ MainActivity / MainViewModel ]
               │
               ▼
         [ AssistantRepository ] 
          /                 \
         /                   \
        ▼                     ▼
[ AssistantNlpEngine ]     [ AssistantDatabase (Room) ]
 (Intent Parsing &          (Command History, Preferences,
  Memory System)             and Local Voice Logs)
        │
        ▼
[ SecurityManager ] ───(Needs approval?)───► [ Jetpack Compose Prompt Screen ]
        │ (Approved)                                    │
        ▼                                               ▼
[ SystemActionDispatcher ] ◄────────────────────(Approved by User)
        │ (Performs Action via System Intents)
        ▼
[ Android System / Target Apps ]
```

---

## 📂 Project Structure Directory Map

```
/app/src/main/java/com/example/
│
├── MainActivity.kt                  # Main Entry point, sets up Edge-to-Edge UI & permissions
│
├── data/                            # Core Data Layer
│   ├── model/
│   │   ├── ActionType.kt            # Enum of all system, dangerous, and utility action types
│   │   └── AssistantAction.kt       # Data model representing an assistant action
│   ├── db/
│   │   ├── AssistantDao.kt          # Room Data Access Object for local queries
│   │   ├── AssistantDatabase.kt     # Room database definition
│   │   └── CommandHistoryEntity.kt  # Database Entity for action logs and memory storage
│   └── repository/
│       └── AssistantRepository.kt   # Single source of truth managing NLP, DB, & Memory state
│
├── engine/                          # Core Voice and NLP Engines
│   ├── AssistantNlpEngine.kt        # Multi-lingual parser for Hindi/Hinglish/English commands
│   ├── AssistantSpeechEngine.kt     # Continuous SpeechRecognizer and Text-To-Speech manager
│   ├── SecurityManager.kt           # Intent verification and high-risk action interceptor
│   └── SystemActionDispatcher.kt    # Low-level dispatcher executing system Intents
│
└── ui/                              # UI Layer
    ├── MainViewModel.kt             # Orchestrates Speech Engine, Repository, and state
    ├── components/
    │   ├── ActionCard.kt            # Visual card representing recognized actions
    │   ├── ConfirmationDialog.kt    # Secure M3 popup modal for high-risk operations
    │   └── VoiceWaveformVisualizer.kt # M3 canvas-based real-time voice ripple animation
    ├── screens/
    │   ├── DashboardScreen.kt       # Voice-first HUD with live waveforms & custom quick triggers
    │   ├── ContactsScreen.kt        # Live contact management interface
    │   ├── HistoryScreen.kt         # Database logs of past voice interactions
    │   ├── SettingsScreen.kt        # System options & Text-to-Speech speech speed controllers
    │   └── SystemAppsScreen.kt      # Interactive list of installed utility applications
    └── theme/
        ├── Color.kt                 # Material 3 Color definitions
        ├── Theme.kt                 # Custom M3 Light & Dark adaptive themes
        └── Type.kt                  # Plus Jakarta Sans and Playfair Display typography
```

---

## 🚀 Key Features & Detailed Commands

### 1. Advanced Voice Interaction (Voice-First Design)
- **Continuous Voice Engine**: Direct visual feedback via a custom Material 3 canvas-based real-time sound waveform visualizer that reacts to user voice amplitude (RMS dB).
- **Text-to-Speech Response**: The assistant speaks back to the user in high-quality local Text-to-Speech voices in their preferred dialect (Hindi/English).

### 2. Multi-lingual Natural Language Processing (NLP)
The assistant natively processes natural sentences, understanding casual Indian colloquial phrasing (Hinglish):

| Feature Category | Sample Command (English / Hindi / Hinglish) | Expected Assistant Action |
|---|---|---|
| **System Settings** | *"WiFi settings kholo"* / *"Bluetooth settings open karo"* | Launches System Settings Pane |
| **Direct Calling** | *"Papa ko call karo"* / *"Call Rahul"* | Opens dialer or initiates phone call |
| **YouTube Media** | *"Arijit Singh ke gaane chalao"* / *"Open YouTube"* | Searches or plays requested keyword on YouTube |
| **Navigation** | *"Google Maps me Bilaspur search karo"* | Routes location directly on Google Maps |
| **Utilities** | *"Kal subah 7 baje ka alarm lagao"* | Opens the clock app with a pre-configured alarm |
| **WhatsApp Chat** | *"WhatsApp me Rahul ko message bhejo"* | Opens the target WhatsApp contact thread |
| **Camera** | *"Camera kholo"* / *"Photo click karo"* | Launches Android Native Camera app |

### 3. Voice Teacher Mode
Designed to onboard new or non-tech-savvy users by giving voice tutorials on how to operate system apps step-by-step:
- *"Phone kaise chalaye sikhao"* -> Assistant speaks complete audio guide steps on operating dialers.
- *"WhatsApp kaise chalaye sikhao"* -> Gives a guided auditory overview of navigating chat messages.

### 4. Diagnostic Mobile Problem Solver
Troubleshoots common smartphone issues offline, walking the user through stepwise guides aloud:
- *"Battery jaldi khatam ho rahi hai problem solution"* -> Explains battery saving practices.
- *"Storage full ho gaya space problem"* -> Explains how to clear cache and manage storage.
- *"Wi-Fi net nahi chal raha problem"* -> Gives troubleshooting tips for connectivity.

### 5. Personal Memory System
Stores user preferences locally in a secure SQLite database to personalize interactions:
- *"Mera favourite song Arijit Singh hai yaad rakho"* -> Saves favorite preference in SQLite DB.
- *"Mujhe kya yaad hai mera preference batao"* -> Recalls preferences from Personal Memory.

### 6. Security Guardrail Interception
- Prevents accidental major changes. High-risk intents like formatting storage or deleting files trigger an interactive confirmation dialog, requiring active biometric/touch validation.

---

## 🔒 Runtime Permissions Matrix

To prevent application crashes and strictly respect Google Play Store security standards, permissions are declared dynamically and checked before calling system actions:

1. `android.permission.RECORD_AUDIO`: Required for continuous speech recognition and microphone amplitude visualization.
2. `android.permission.CALL_PHONE`: Required to initiate calls directly from voice commands.
3. `android.permission.READ_CONTACTS`: Required to search and resolve contact names for voice calling.

The app uses Jetpack Compose dynamic runtime request flows to ask for these only when the voice feature is invoked.

---

## 🛠️ Build, Compilation, and Release Guides

### 1. Prerequisites
- **Operating System**: Windows, macOS, or Linux.
- **Java Development Kit**: JDK 17 or modern JDK 21.
- **Android SDK Platform**: SDK Level 35 (and backward compatible to SDK Level 26 / Android 8.0).

### 2. Standard Compilation Commands
Always run your commands in the project root directory.

- **Check Code Syntax & Lint**:
  ```bash
  gradle lint
  ```
- **Run JVM Local Robolectric Tests**:
  ```bash
  gradle :app:testDebugUnitTest
  ```
- **Compile Debug APK**:
  ```bash
  gradle assembleDebug
  ```

### 3. Generate Signed Release APK & Android App Bundle (AAB)

To prepare a production release for Google Play Store upload:

- **Generate Release APK**:
  ```bash
  gradle assembleRelease
  ```
- **Generate Android App Bundle (AAB)**:
  ```bash
  gradle bundleRelease
  ```

The generated binaries will be compiled under:
- APK: `app/build/outputs/apk/release/app-release.apk`
- AAB: `app/build/outputs/bundle/release/app-release.aab`

---

## 🔑 Keystore Setup & Release Signing Instructions

To distribute your app on the Google Play Store or install the Release build on a physical device, you must sign it with a secure keystore.

### 1. Generate a New Keystore
You can generate a cryptographically secure release keystore using Java's command-line `keytool`:

```bash
keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key-alias
```
*Note: Keep your keystore password and key alias private.*

### 2. Configure Gradle Signing
To automatically sign your Release builds, open `app/build.gradle.kts` and add a signing configuration inside the `android` block:

```kotlin
android {
    ...
    signingConfigs {
        create("release") {
            storeFile = file("my-release-key.jks")
            storePassword = System.getenv("RELEASE_STORE_PASSWORD") ?: "your_store_password"
            keyAlias = System.getenv("RELEASE_KEY_ALIAS") ?: "my-key-alias"
            keyPassword = System.getenv("RELEASE_KEY_PASSWORD") ?: "your_key_password"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

---

## 🔮 Future System Upgrades

1. **Gemini Nano On-Device Inference**: Migrate from simple pattern matching to Google AI Client SDK with on-device Large Language Models (LLM) to parse arbitrary voice intents completely offline.
2. **Background Always-On Wake Word**: Integrate a lightweight keyword spotter (such as "Okay Google" or "Hey Assistant") running in a highly optimized foreground service with minimal RAM overhead.
3. **Task Automation Pipelines**: Allow users to chain multiple system commands together in a single voice sentence (e.g. *"Main so raha hu, alarm lagao aur Wi-Fi band karo"*).
