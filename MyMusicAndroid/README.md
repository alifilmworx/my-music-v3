# My Music — Android (native)

This is a real Kotlin + Jetpack Compose Android Studio project, not the web app wrapped
in anything. It uses:

- **MediaStore** for scanning — the whole reason for this rewrite. On first launch it asks
  once for audio access (`READ_MEDIA_AUDIO` on Android 13+, `READ_EXTERNAL_STORAGE` below
  that), and after that it never asks again — every audio file already on the device shows
  up automatically, and a newly downloaded song appears the next time the app is opened or
  resumed, with no folder to pick, ever.
- **Media3 (ExoPlayer) + MediaSession** for playback — this gives real lock-screen and
  notification controls for free, the native equivalent of the web app's Media Session API
  wiring, with far less code.
- **Room** for liked songs and custom playlists, replacing what was `localStorage` in the
  web version.
- **Jetpack Compose** for the UI, matching the same orange (#FE5721), the same screens
  (Home, Library with all six tabs including Folders, Now Playing with the blurred-backdrop
  art card), and the same core interactions.

## Important: this has never been compiled

I don't have Android Studio, the Android SDK, or a Kotlin compiler available to me, and no
internet access to fetch either — so unlike the web app (where every single change was
tested before being sent to you), none of this Kotlin code has actually been built or run.
It's written carefully, following standard, well-established Android patterns, but you
should expect to hit real compile errors the first time you open it — that's completely
normal for a project this size, not a sign something went fundamentally wrong. If you paste
me the exact error Android Studio shows, I can help fix it.

## What's simplified compared to the polished web version

Getting the web app to its current state took dozens of rounds of iteration I could verify
myself. Reaching the same level of polish here — the exact swipe-transition animations, the
Telegram-style undo countdown ring, the exact parallax scrolling, drag-to-seek gestures —
would take the same kind of iteration, but through you reporting back what you see after
each build instead of me testing directly. What's here now is a genuine, working skeleton
with the real architecture in place (native scanning, real playback, real persistence, the
right screens) — not yet a pixel-for-pixel port of every animation.

## Building this without installing anything (recommended if you don't want local software)

This project includes a GitHub Actions workflow (`.github/workflows/build-apk.yml`) that
builds the APK entirely on GitHub's own servers - you only ever use a web browser.

1. Go to **github.com** and create a free account if you don't have one.
2. Click the **+** in the top right → **New repository**. Name it anything (e.g.
   `my-music-android`), leave it Public, and create it.
3. On the new repo's page, click **uploading an existing file** (or drag-and-drop). Select
   **all the files and folders inside this `MyMusicAndroid` folder** (including the hidden
   `.github` folder - if your file picker hides dot-folders, drag the whole extracted
   `MyMusicAndroid` folder in at once rather than picking files one by one) and commit them.
4. Click the **Actions** tab at the top of the repo. You should see a workflow run called
   "Build APK" already running (it starts automatically on upload). Click it to watch the
   progress - it takes a few minutes.
5. If it finishes with a green checkmark, scroll down to **Artifacts** and download
   `my-music-apk` - that's a zip containing your actual installable `app-debug.apk`.
6. If it finishes with a red X, click into the failed step to see the error log, and paste
   that text to me - I'll help fix the code, you re-upload the corrected file, and it
   rebuilds automatically.
7. Transfer the APK to your phone (email it to yourself, Google Drive, or USB) and install
   it - Android will ask permission to install from this source the first time, which is
   normal for any APK from outside the Play Store.

This is a real back-and-forth loop, same as it would be with Android Studio - the
difference is nothing at all gets installed on your computer, only used through the browser.

## Opening this project locally instead (if you change your mind)

1. Install **Android Studio** (the free official IDE — this is different from the Android
   *phone* app; you install this on your computer): https://developer.android.com/studio
2. Open Android Studio → **Open** → select the `MyMusicAndroid` folder (the one containing
   `settings.gradle.kts`).
3. Let it sync — it will download Gradle and all the dependencies listed in
   `app/build.gradle.kts` automatically. This needs an internet connection and can take a
   few minutes the first time.
4. If it reports errors, they'll show in the **Build** panel at the bottom — copy the exact
   text and send it to me.
5. Once it builds, either run it straight onto your phone (USB cable + "Run" button, with
   Developer Options + USB debugging turned on), or **Build → Generate Signed Bundle / APK**
   to produce an installable `.apk` file you can transfer over like any other app.

## Project layout

- `data/` — Track model, MediaStore scanner, Room database/entities/DAO, repository
- `playback/` — the ExoPlayer + MediaSession service (lock screen controls)
- `viewmodel/` — MainViewModel, holding playback state and tying everything together
- `ui/theme/` — colors, typography, matching the web app exactly
- `ui/screens/` — HomeScreen, LibraryScreen, NowPlayingScreen
