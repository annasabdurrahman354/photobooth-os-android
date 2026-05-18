# Photobooth OS — Android Client

Native Android kiosk application for photobooth sessions. CameraX-powered photo capture with a WebView-based rendering pipeline that communicates with the Photobooth OS web platform.

## Overview

This is the on-device client used at photobooth stations. It connects to the same Supabase backend as the web platform, manages the full session lifecycle (select booth → select template → capture photos → render → share), and offloads final image compositing to a WebView that loads the web app's Render Page.

```
┌─────────────────────────────────────────────────────┐
│  Android Kiosk App                                  │
│                                                     │
│  Login → Booth Select → Template Select → Session   │
│                                                     │
│  Session Flow:                                      │
│  ┌──────┐ ┌─────────┐ ┌────────┐ ┌──────────────┐  │
│  │ Idle │→│ Shooting │→│ Review │→│ Rendering    │  │
│  │      │ │ (CameraX)│ │        │ │ (WebView)    │  │
│  └──────┘ └─────────┘ └────────┘ └──────┬───────┘  │
│                                          │          │
│                               ┌──────────▼────────┐ │
│                               │    Done           │ │
│                               │ (QR + Share Token)│ │
│                               └───────────────────┘ │
└─────────────────────────────────────────────────────┘
            │                          │
            ▼                          ▼
    ┌──────────────┐         ┌──────────────────┐
    │   Supabase   │         │  Web RenderPage  │
    │ (Auth/DB/    │         │  (in WebView)    │
    │  Storage)    │         │                  │
    └──────────────┘         └──────────────────┘
```

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material3 |
| Architecture | MVVM (ViewModel + StateFlow) |
| Navigation | Navigation Compose |
| Camera | CameraX 1.3.1 |
| Backend | Supabase Kotlin SDK 2.5.4 (Auth, Postgrest, Storage) |
| Image Loading | Coil 2.6.0 |
| Networking | Ktor OkHttp + OkHttp 4.12.0 |
| Serialization | kotlinx-serialization |
| Design | Neo-brutalist (custom theme) |

## Requirements

- Android SDK 26+ (Android 8.0)
- Target SDK 35
- Camera hardware (`android.hardware.camera` required)
- Internet connection

## Project Structure

```
app/src/main/java/com/askara/photobooth/
├── MainActivity.kt                 # Single activity, immersive kiosk mode
├── PhotoBoothApp.kt                # Application class (Supabase init)
├── data/
│   ├── model/Models.kt             # @Serializable data classes
│   ├── remote/SupabaseClientProvider.kt  # Supabase singleton
│   └── repository/
│       ├── AuthRepository.kt       # Sign in/out, profile lookup
│       ├── BoothRepository.kt      # Booths + templates queries
│       └── SessionRepository.kt    # Session CRUD, capture upload, status sync
├── navigation/NavGraph.kt          # Route definitions + argument extraction
├── ui/
│   ├── components/BrutalComponents.kt
│   ├── screen/
│   │   ├── LoginScreen.kt          # Email/password auth
│   │   ├── BoothSelectScreen.kt    # Booth grid with status badges
│   │   ├── TemplateSelectScreen.kt # Template grid with thumbnails
│   │   └── SessionScreen.kt        # Full session flow (all states)
│   └── theme/
│       ├── BrutalStyle.kt          # Design tokens (shapes, borders, shadows)
│       ├── Color.kt                # Slate scale + accent palette
│       ├── Theme.kt                # PhotoBoothTheme (light/dark)
│       └── Type.kt                 # BrutalTypography
└── viewmodel/
    ├── AuthViewModel.kt
    ├── BoothSelectViewModel.kt
    ├── TemplateSelectViewModel.kt
    └── SessionViewModel.kt         # Session state machine + status sync
```

## Build Configuration

### BuildConfig Fields

| Field | Default | Description |
|-------|---------|-------------|
| `SUPABASE_URL` | `https://lrurcbsbauspjnafnoys.supabase.co` | Supabase project URL |
| `SUPABASE_KEY` | (anon key) | Supabase publishable key |
| `RENDER_SERVER_URL` | `http://10.0.2.2:3001` | Puppeteer render server (fallback) |
| `WEB_APP_URL` | `https://photobooth-os.vercel.app` | Web platform URL for RenderPage |

Change `WEB_APP_URL` to `http://10.0.2.2:5173` for local web dev (emulator → host machine).

### Build

```bash
cd android
./gradlew assembleDebug
```

## Session Flow

### 1. Login
`AuthViewModel.signIn()` → Supabase `signInWithPassword` → loads profile from `profiles` table → navigates to `booths/{tenantId}`

### 2. Booth Select
`BoothSelectViewModel.loadBooths()` → queries `booths` table filtered by `tenant_id` → shows grid with status badges (online / in_session / offline)

### 3. Template Select
`TemplateSelectViewModel.loadTemplates()` → queries `templates` table → grid with Coil-loaded thumbnails → extracts photo slot count from `layout_json`

### 4. Session — CameraX Capture
```
IDLE ──[touch]──→ SHOOTING ──[all captured]──→ REVIEW
                    ↑                              │
                    └──────[retake all]─────────────┘
```

- CameraX `LifecycleCameraController` with front-facing camera
- 3-second countdown with flash effect
- Photos compressed to JPEG (~500KB target)
- Captures stored as `ByteArray` in ViewModel state

### 5. Session — Upload & Render
```
REVIEW ──[Looks Great!]──→ confirmAndUpload() → RENDERING (WebView)
                                                      │
                              ┌────────────────────────┘
                              ▼
                    WebView loads /render/{sessionId}?token={accessToken}
                              │
                    RenderPage renders template + uploads final image
                              │
                    window.Android.onRenderComplete(url)
                              │
                              ▼
                           DONE
```

## WebView Render Integration

Instead of an external Puppeteer server, the Android app embeds the web platform's **Render Page** in a WebView. This offloads template compositing (html2canvas) to the web stack while keeping the native camera experience.

### Communication Protocol

The WebView and Android app communicate via a `JavascriptInterface`:

```kotlin
// Android side — injected into WebView
addJavascriptInterface(object {
    @JavascriptInterface
    fun onRenderComplete(finalImageUrl: String, shareToken: String) {
        mainHandler.post { viewModel.onRenderComplete(finalImageUrl) }
    }

    @JavascriptInterface
    fun onRenderError(errorMessage: String) {
        mainHandler.post { viewModel.onRenderError(errorMessage) }
    }
}, "Android")
```

```typescript
// Web side — RenderPage.tsx
declare global {
  interface Window {
    Android?: {
      onRenderComplete(finalImageUrl: string, shareToken: string): void;
      onRenderError(error: string): void;
    };
  }
}

// Called when render finishes
window.Android?.onRenderComplete(url, shareToken);
```

### Auth Token Passing

The WebView doesn't share the Android app's Supabase session. The access token is passed via URL parameter:

```
https://photobooth-os.vercel.app/render/{sessionId}?token={supabaseAccessToken}
```

The Render Page reads the token and calls `supabase.auth.setSession()` to authenticate its Supabase calls, satisfying RLS policies.

### Thread Safety

`@JavascriptInterface` methods execute on a WebView background thread. All state updates are dispatched to the main thread via `Handler(Looper.getMainLooper())` to trigger Compose recomposition.

### Error Handling

| Scenario | Handler |
|----------|---------|
| WebView page load failure | `WebViewClient.onReceivedError()` → `viewModel.onRenderError()` |
| Render failure (JS) | `window.Android.onRenderError(msg)` → error overlay with RETRY |
| Upload failure (JS) | Same — error propagated to Android |

Error overlay shows the error message with **RETRY** (reloads WebView) and **BACK TO REVIEW** buttons.

## Session Status Sync

Every state transition syncs the session status to Supabase:

| Local State | DB `status` |
|-------------|-------------|
| Session created | `idle` |
| `startShooting()` | `shooting` |
| `goToReview()` | `review` |
| `confirmAndUpload()` | `uploading` |
| `setState(RENDERING)` | `rendering` |
| `onRenderComplete()` | `completed` |

Status updates are fire-and-forget (non-blocking).

## Design System

Neo-brutalist aesthetic matching the web platform:

| Token | Value |
|-------|-------|
| Card shape | 16dp rounded corners |
| Card border | 4dp solid Slate950 |
| Card shadow | 8dp black offset |
| Button shape | 8dp rounded corners |
| Button shadow | 4dp black offset |
| Primary accent | Blue600 |
| Secondary accent | Yellow400 |
| Success | Emerald400 |
| Error | Red500 |

All interactive elements have a tactile press-down animation (shadow reduces to 0dp on press).

## Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| Compose BOM | 2024.02.00 | Jetpack Compose UI toolkit |
| CameraX | 1.3.1 | Camera preview + capture |
| Supabase Kotlin | 2.5.4 | Auth, Postgrest, Storage |
| Coil | 2.6.0 | Async image loading |
| Navigation Compose | 2.7.7 | Screen navigation |
| OkHttp | 4.12.0 | HTTP client (render server fallback) |
| Ktor | 3.0.1 | Supabase SDK transport |
| kotlinx-serialization | — | JSON serialization for Supabase models |

## Related

- **Web Platform:** [askarabooth-web](https://github.com/your-org/askarabooth-web) — Dashboard, template editor, render page, share gallery
- **Render Page:** `https://your-app.com/render/{sessionId}` — Client-side template compositing via html2canvas

## License

Proprietary — AskaRa Entertainment
