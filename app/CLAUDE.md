# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

NetMusicAndroid — a Kotlin Android music streaming app with a Node.js backend (`../server/`). The app plays songs, browses playlists/singers, posts comments, and manages user auth.

## Build & Test

```bash
# Build debug APK
./gradlew assembleDebug

# Run unit tests (JUnit 4, local JVM)
./gradlew test

# Run instrumentation tests (requires emulator/device)
./gradlew connectedAndroidTest

# Clean build artifacts
./gradlew clean

# The backend server runs separately from ../server/
cd ../server && npm run dev
```

- **Gradle**: Kotlin DSL, version catalog at `../gradle/libs.versions.toml`
- **KSP**: used only for Room annotation processing (no KAPT)
- **Compile SDK**: 37, **Min SDK**: 24, **Target SDK**: 36
- **Java**: 11 source/target
- **ViewBinding** enabled, Compose disabled

## Architecture

### MVVM + Repository

```
Activity/Fragment → ViewModel → Repository → ApiService (Retrofit)
                                   ↓
                               Room DAO
```

- **ApiClient** (`data/api/ApiClient.kt`): Singleton Retrofit wrapper. OkHttp interceptor injects `Authorization: Bearer <token>` on all requests except `/auth/login` and `/auth/refresh`. On 401, the interceptor automatically calls refresh, retries the request, and if that fails, logs the user out.
- **ApiResponse<T>** (`data/model/ApiResponse.kt`): All API responses follow `{ code: Int, message: String?, data: T? }`.
- **Repositories** return `Result<T>` — callers handle `.onSuccess {}` / `.onFailure {}`.
- **Token storage**: Access/refresh tokens live in Room (`UserEntity`). SharedPreferences (`SpManager`) stores only login status, current email, and user ID.
- **AuthRepository** is a singleton initialized in `MinMusicApp.onCreate()`. All other code gets it via `AuthRepository.getInstance()` (no-arg).

### Activity & Navigation

- **LoginActivity** is the launcher activity (`MAIN` intent filter in AndroidManifest).
- **BaseActivity** hosts a `BottomNavigationView` with 3 tabs: Home (`HomeFragment`), Player (`PlayerFragment`), Mine (`MineFragment`). It exposes a shared `MainViewModel` (current song + play state) for cross-fragment communication.
- Other activities (Playlist, Singer, Comment, Setting, PlaylistDetail, Register) are stand-alone, started via `startActivity`.

### Key Singletons (initialized in `MinMusicApp.onCreate()`)

| Singleton | Purpose |
|---|---|
| `AppDatabase` | Room DB, exposes `globalUserDao` for global access |
| `AuthRepository` | Login/register/logout/token-refresh |
| `ApiClient` | Retrofit + OkHttp with auth interceptor |
| `SpManager` | SharedPreferences for login state metadata |
| `MusicPlayerManager` | MediaPlayer wrapper: play, pause, seek, stop |

### Data Flow: Playing a Song

1. `HomeFragment` → `SongRepository.fetchSongs()` → `SongApiService.getSongs()`
2. User taps a song → `MainViewModel.playSong(song)` → `BaseActivity.navigateToPlayer()`
3. `PlayerFragment` observes `MainViewModel.currentSong` and calls `MusicPlayerManager.play(url)`

### API Base URL

Defined in `ApiConst.kt`: `http://10.0.2.2:3000/api/v1/` (Android emulator's alias for host `localhost:3000`). The backend runs on port 3000. Static assets (song files, covers) are served from the same host.

### SpManager vs SpUtil

- **`SpManager`** (canonical): Uses `MinMusicApp.globalContext` (no Context parameter needed). Stores login email, user ID, and login-status flag.
- **`SpUtil`** (legacy): Requires a `Context` parameter. Stores token and user-info JSON. Prefer `SpManager` for new code; tokens should go through Room.

## Backend

The Node.js backend lives at `../server/`. Check `../server/.env.example` for configuration. The backend provides REST API endpoints under `/api/v1/`:
- `auth/` — register, login, refresh
- `songs/` — list, detail, comments, play URL
- `playlists/` — list, detail, create
- `singers/` — list, detail

## Key Conventions

- Network calls are `suspend` functions called from `viewLifecycleOwner.lifecycleScope` or `ViewModel.viewModelScope`.
- Adapters accept a lambda `onItemClick` callback for item tap handling.
- Room `UserEntity` uses email as `@PrimaryKey`; `OnConflictStrategy.REPLACE` on insert means re-login overwrites previous token data for the same email.
- The OkHttp token refresh uses a global `Mutex` to prevent concurrent refresh calls.
- `BaseActivity.isAppForeground()` guards UI operations (like navigating to login) from running while the app is in the background.
