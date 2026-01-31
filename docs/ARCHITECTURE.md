# ServerDrivenUI Architecture

This document explains the dual-approach architecture used in this project, combining **Zipline/Redwood** for server-driven UI with **native KMP** for client-side features.

## Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                       Android/iOS App                            │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────┐  ┌──────────────────────────┐  │
│  │    Native KMP Screens       │  │   Zipline/Redwood UI     │  │
│  │  (Camera, QR Scanner, etc)  │  │  (Server-driven content) │  │
│  │                             │  │                          │  │
│  │  AppWithNativeOverlay.kt    │  │  TreehouseContent        │  │
│  │  AttendanceScannerScreen.kt │  │  HomeScreen.kt           │  │
│  │  QrScannerAndroid.kt        │  │  TrainingScreen.kt       │  │
│  └─────────────────────────────┘  └──────────────────────────┘  │
├─────────────────────────────────────────────────────────────────┤
│                      Shared Components                           │
│  CaliclanTheme.kt  │  SupabaseGymRepository.kt  │  DTOs          │
└─────────────────────────────────────────────────────────────────┘
```

---

## Directory Structure

```
composeApp/src/
├── commonMain/kotlin/.../
│   ├── shared/                          # Zipline/Redwood specific
│   │   ├── App.kt                       # Main app entry
│   │   ├── Protocol.kt                  # Redwood widget implementations
│   │   ├── RealGymService.kt            # Zipline service bridge
│   │   ├── SharedAppSpec.kt             # Treehouse spec
│   │   └── ...
│   │
│   ├── native/                          # Pure KMP (not Zipline)
│   │   ├── screens/                     # Native-first screens
│   │   │   ├── AttendanceScannerScreen.kt
│   │   │   └── AppWithNativeOverlay.kt
│   │   └── viewmodels/
│   │       └── AttendanceViewModel.kt
│   │
│   └── common/                          # Shared by both approaches
│       └── theme/
│           └── CaliclanTheme.kt
│
├── androidMain/kotlin/.../
│   ├── shared/                          # Android Zipline actuals
│   │   └── Platform.android.kt
│   └── native/                          # Android-specific natives
│       └── camera/
│           └── QrScannerAndroid.kt      # CameraX + ML Kit
│
└── iosMain/kotlin/.../
    ├── shared/                          # iOS Zipline actuals
    └── native/                          # iOS-specific natives
        └── camera/
            └── QrScannerIos.kt          # AVFoundation (future)

presenter/                               # Zipline bundle (runs in JS)
├── src/jsMain/kotlin/.../presenter/
│   ├── screens/                         # Server-driven screens
│   │   ├── HomeScreen.kt
│   │   ├── TrainingScreen.kt
│   │   └── ...
│   └── components/                      # Reusable UI components
│       └── ...
```

---

## When to Use Which Approach

### Use Zipline/Redwood (Server-Driven) When:

| Scenario | Example |
|----------|---------|
| UI changes frequently | Marketing banners, promotions |
| A/B testing is needed | Different layouts for users |
| Content is dynamic | News feeds, user-specific data |
| No platform-specific APIs needed | Lists, cards, forms |
| Over-the-air updates are valuable | Bug fixes without app update |

**Location:** `presenter/src/jsMain/kotlin/.../presenter/`

### Use Native KMP When:

| Scenario | Example |
|----------|---------|
| Platform APIs required | Camera, Bluetooth, NFC |
| Real-time performance critical | QR scanning, animations |
| Offline-first functionality | Local data sync |
| Complex gestures/interactions | Drawing, drag-and-drop |
| Deep OS integration | Widgets, notifications |

**Location:** `composeApp/src/commonMain/kotlin/.../native/`

---

## How to Add New Features

### Adding a Zipline/Redwood Screen

1. **Create screen in presenter:**
   ```kotlin
   // presenter/src/jsMain/kotlin/.../presenter/screens/NewScreen.kt
   @Composable
   fun NewScreen(gymService: GymServiceProvider) {
       ScrollableColumn(padding = 16) {
           HeaderText(text = "New Screen", size = "large")
           // Use schema widgets: FlexColumn, FlexRow, StyledText, etc.
       }
   }
   ```

2. **Add to navigation:**
   ```kotlin
   // presenter/src/jsMain/kotlin/.../presenter/screens/MainNavigationShell.kt
   when (route) {
       "new" -> NewScreen(gymService)
   }
   ```

3. **Rebuild Zipline bundle:**
   ```bash
   ./gradlew :presenter:compileDevelopmentExecutableKotlinJsZipline
   ```

### Adding a Native KMP Screen

1. **Create screen in commonMain/native:**
   ```kotlin
   // composeApp/src/commonMain/kotlin/.../native/screens/NewNativeScreen.kt
   @Composable
   fun NewNativeScreen(
       onDismiss: () -> Unit
   ) {
       // Use standard Compose with CaliclanTheme
       Surface(color = CaliclanTheme.Background) {
           Column {
               Text("Native Screen", color = CaliclanTheme.TextPrimary)
           }
       }
   }
   ```

2. **Create ViewModel if needed:**
   ```kotlin
   // composeApp/src/commonMain/kotlin/.../native/viewmodels/NewViewModel.kt
   class NewViewModel {
       private val _state = MutableStateFlow<State>(State.Idle)
       val state: StateFlow<State> = _state.asStateFlow()
   }
   ```

3. **Add platform-specific code (if needed):**
   ```kotlin
   // composeApp/src/androidMain/kotlin/.../native/MyFeatureAndroid.kt
   @Composable
   actual fun PlatformSpecificComponent() {
       // Android-specific implementation
   }
   ```

4. **Integrate in MainActivity:**
   ```kotlin
   // androidApp/src/main/kotlin/.../MainActivity.kt
   AppWithNativeOverlay(
       // Add navigation trigger for new screen
   ) {
       App(treehouseApp = app)
   }
   ```

5. **Rebuild Android app:**
   ```bash
   ./gradlew :androidApp:installDebug
   ```

---

## Network Calls

### Zipline/Redwood Screens

Use `GymServiceProvider` passed from the host:

```kotlin
@Composable
fun MyScreen(gymService: GymServiceProvider) {
    val data = remember { mutableStateOf<Data?>(null) }
    
    LaunchedEffect(Unit) {
        data.value = gymService.repo.fetchData()
    }
}
```

### Native KMP Screens

Use repository directly or create dedicated API client:

```kotlin
// Option 1: Use existing repository
class MyViewModel(private val repo: SupabaseGymRepository) {
    suspend fun loadData() = repo.fetchData()
}

// Option 2: Direct HTTP calls
class MyViewModel {
    private val client = HttpClient { /* config */ }
    
    suspend fun callApi() {
        val response = client.get("https://api.example.com/endpoint")
    }
}
```

---

## Shared Resources

### Theme (CaliclanTheme)

Both approaches share the same theme:

```kotlin
import com.example.serverdrivenui.common.theme.CaliclanTheme

// In Zipline/Redwood (Protocol.kt)
val bgColor = CaliclanTheme.Surface

// In Native KMP
Surface(color = CaliclanTheme.Background) { ... }
```

### DTOs

Define in `core-data` module for sharing:

```
core-data/src/commonMain/kotlin/.../dto/
├── GymDto.kt
├── AttendanceDto.kt
└── ...
```

---

## Database Migrations

SQL migrations are stored in `docs/migrations/`:

```
docs/migrations/
├── 001_qr_attendance.sql
└── ...
```

Run migrations in Supabase SQL Editor.

---

## Build Commands

| Command | Description |
|---------|-------------|
| `./gradlew :presenter:compileDevelopmentExecutableKotlinJsZipline` | Build Zipline bundle |
| `./gradlew :androidApp:installDebug` | Build & install Android app |
| `./gradlew :composeApp:iosSimulatorArm64MainBinaries` | Build iOS framework |

---

## Summary

| Aspect | Zipline/Redwood | Native KMP |
|--------|-----------------|------------|
| **Location** | `presenter/` | `composeApp/.../native/` |
| **UI Toolkit** | Schema widgets | Compose Multiplatform |
| **Updates** | OTA via Zipline | App update required |
| **Platform APIs** | ❌ Limited | ✅ Full access |
| **Performance** | Good | Best |
| **Offline** | Limited | Full support |
