# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

NetMusicAndroid — a Kotlin Android music streaming app with a Node.js/TypeScript backend (`../server/`). The app plays songs, browses playlists/singers, posts comments, and manages user auth.

## Build & Test

```bash
# Build debug APK (run from repo root, where gradlew lives)
cd .. && ./gradlew assembleDebug

# Run unit tests (JUnit 4, local JVM)
cd .. && ./gradlew test

# Run instrumentation tests (requires emulator/device)
cd .. && ./gradlew connectedAndroidTest

# Clean build artifacts
cd .. && ./gradlew clean

# Start the backend server
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

- **ApiClient** (`data/api/ApiClient.kt`): Singleton Retrofit wrapper. OkHttp interceptor injects `Authorization: Bearer <token>` on all requests except `/auth/login` and `/auth/refresh`. On 401, the interceptor automatically calls refresh (guarded by a global `Mutex`), retries the request, and if that fails, logs the user out.
- **ApiResponse<T>** (`data/model/ApiResponse.kt`): All API responses follow `{ code: Int, message: String?, data: T? }`.
- **Repositories** return `Result<T>` — callers handle `.onSuccess {}` / `.onFailure {}`.
- **Token storage**: Access/refresh tokens live in Room (`UserEntity`). SharedPreferences (`SpManager`) stores only login status, current email, and user ID.
- **AuthRepository** is a singleton initialized in `MinMusicApp.onCreate()`. All other code gets it via `AuthRepository.getInstance()` (no-arg).

### Activity & Navigation

- **LoginActivity** is the launcher activity (`MAIN` intent filter in AndroidManifest).
- **BaseActivity** hosts a `BottomNavigationView` with 3 tabs: Home (`HomeFragment`), Player (`PlayerFragment`), Mine (`MineFragment`).
- Other activities (Playlist, Singer, Comment, Setting, PlaylistDetail, Register, Search, Favorites, etc.) are stand-alone, started via `startActivity`.
- **`BaseActivity.navigateToPlayerFrom(ctx)`** (static): external activities use this to return to BaseActivity and open the full-screen player tab. Uses `FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP`.
- **`BaseActivity.globalGoLogin()`** (static): logs out and navigates to LoginActivity with `FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_NEW_TASK`.
- **`BaseActivity.isAppForeground()`**: guards UI operations (like navigating to login) from running while the app is in the background.

### Central Playback: BottomPlayerViewModel

**`BottomPlayerViewModel`** (merged from former MainViewModel + BottomPlayerViewModel) is the single ViewModel for all playback state. Fragments access it via `activityViewModels()` so they share one instance scoped to BaseActivity:

```kotlin
private val bottomVm: BottomPlayerViewModel by activityViewModels()
```

It exposes:
- **Play control**: `playSong(song)`, `togglePlayPause()`, `playNext()`, `playPrev()`
- **UI state LiveData**: `songName`, `singerName`, `coverUrl`, `isPlaying`, `hasCurrentSong`, `toastMsg`
- **Full-player LiveData**: `currentSong` (SongDetail), `isLiked`
- **User Flow**: `currentUserFlow: Flow<UserEntity?>` (delegates to `AuthRepository.observeCurrentLoginUser()` — a Room Flow that auto-emits on DB changes)

Play queue is managed by `PlayQueueRepository` (global singleton, Room-backed). Next/prev support wrap-around: last→first, first→last.

### MusicPlayerManager

`MusicPlayerManager` (object singleton) wraps Android `MediaPlayer`. Uses **multi-listener** pattern — callbacks are stored in `MutableList` fields so multiple ViewModels/subscribers can observe the same player without overwriting each other. Register with `addOnStateChangedListener`, `addOnCompletionListener`, etc., and remove in `ViewModel.onCleared()`.

`resolveUrl(path)` converts relative paths to full HTTP URLs using the base from `ApiConst`.

### Key Singletons (initialized in `MinMusicApp.onCreate()`)

| Singleton | Purpose |
|---|---|
| `AppDatabase` | Room DB, exposes `globalUserDao`, `globalPlayQueueDao`, `globalRecentPlayDao` |
| `AuthRepository` | Login/register/logout/token-refresh, user CRUD, avatar upload |
| `PlayQueueRepository` | Play queue persistence (getInstance) |
| `RecentPlayRepository` | Recent-play history persistence (getInstance) |
| `ApiClient` | Retrofit + OkHttp with auth interceptor |
| `SpManager` | SharedPreferences for login state metadata |
| `MusicPlayerManager` | MediaPlayer wrapper: play, pause, seek, stop |

### Data Flow: Playing a Song

1. `HomeFragment` → `SongRepository.fetchSongs()` → `SongApiService.getSongs()`
2. User taps a song → `bottomVm.playSong(song)` (writes to PlayQueueRepository, records to RecentPlayRepository, calls `MusicPlayerManager.play()`)
3. `PlayerFragment` observes `bottomVm.currentSong` and renders full-screen UI
4. Bottom mini-player (in HomeFragment, MineFragment) observes `bottomVm.songName`/`singerName`/`coverUrl`/`isPlaying`

### API Base URL

Defined in `ApiConst.kt`: `http://10.0.2.2:3000/api/v1/` (Android emulator's alias for host `localhost:3000`). The backend runs on port 3000. Static assets (song files, covers, avatars) are served from the same host. `ApiConst.STATIC_BASE` holds an alternative LAN IP for physical device testing.

### SpManager vs SpUtil

- **`SpManager`** (canonical): Uses `MinMusicApp.globalContext` (no Context parameter needed). Stores login email, user ID, and login-status flag.
- **`SpUtil`** (legacy): Requires a `Context` parameter. Stores token and user-info JSON. Prefer `SpManager` for new code; tokens should go through Room.

## Backend

The Node.js backend lives at `../server/`. It uses **Express 5 + TypeScript (CommonJS)** with **Prisma 7.8 + Turso/libSQL** for the database.

```bash
cd ../server && npm run dev     # ts-node-dev with hot reload
cd ../server && npm test        # Jest + supertest
cd ../server && npx prisma db push   # Sync schema to Turso
cd ../server && npx prisma db seed   # Seed sample data
```

See `../server/README.md` for full backend documentation including environment setup and deployment.

REST API endpoints under `/api/v1/`:
- `auth/` — register, login, refresh
- `songs/` — list, detail, comments, upload, delete, favorites, my-songs
- `playlists/` — list, detail, create, delete, add/remove songs
- `singers/` — list, detail
- `search/` — songs, singers, playlists
- `users/me` — get/update profile, upload avatar

File uploads use **multer**; avatars land in `../server/static/avatars/`. API validation uses **Zod**. Authentication is JWT-based with access/refresh token pairs (15-min access, configurable refresh).

## Key Conventions

- Network calls are `suspend` functions called from `viewLifecycleOwner.lifecycleScope` or `ViewModel.viewModelScope`.
- When operations must be sequential (e.g. upload avatar then update username), use `suspend` functions and `await` them in order — avoid fire-and-forget `viewModelScope.launch` wrappers that introduce race conditions on Room writes.
- Adapters accept a lambda `onItemClick` callback for item tap handling.
- Room `UserEntity` uses email as `@PrimaryKey`; `OnConflictStrategy.REPLACE` on insert means re-login overwrites previous token data for the same email.
- The OkHttp token refresh uses a global `Mutex` to prevent concurrent refresh calls.
- Glide loads for user avatars should use `DiskCacheStrategy.NONE` — the avatar file may change on the server even if the URL path stays the same.
