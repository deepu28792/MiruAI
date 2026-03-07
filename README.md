# Miru AI 🎬✨

**Transform text & images into stunning AI-generated videos**

---

## Features

- **Text → Video** — Describe any scene and generate a video with AI
- **Image → Video** — Upload any photo and animate it with motion
- **6 Visual Styles** — Cinematic, Anime, Realistic, Abstract, 3D Render, Watercolor
- **Variable Duration** — 3s, 5s, or 7s output
- **Motion Strength Control** — Subtle to Dynamic animations
- **Video Playback** — Full in-app preview player
- **Save to Gallery** — Export videos with one tap
- **Share** — Share directly from the app
- **Colorful Vibrant UI** — Dark theme with purple/pink/cyan gradient branding

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| Architecture | MVVM + LiveData |
| Navigation | Jetpack Navigation Component |
| Networking | Retrofit2 + OkHttp3 |
| Image Loading | Glide |
| AI API | Stability AI (Stable Video Diffusion) |
| UI | Material Components + ConstraintLayout |

---

## Setup

### 1. Get a Stability AI API Key
1. Visit [platform.stability.ai](https://platform.stability.ai)
2. Sign up / log in
3. Go to **Account → API Keys**
4. Create a new key

### 2. Add API Key in App
1. Launch **Miru AI**
2. Tap the ⚙️ settings icon on the home screen
3. Enter your `sk-...` API key
4. Tap **Save**

---

## Project Structure

```
app/
├── data/
│   ├── api/
│   │   ├── StabilityAiApi.kt       # Retrofit API interface
│   │   └── RetrofitClient.kt       # HTTP client setup
│   └── VideoRepository.kt          # Data layer + polling logic
├── ui/
│   ├── SplashActivity.kt           # Animated splash screen
│   ├── MainActivity.kt             # Bottom nav host
│   ├── home/
│   │   └── HomeFragment.kt         # Dashboard + API key settings
│   ├── texttovideo/
│   │   ├── TextToVideoFragment.kt  # Text → Video UI
│   │   ├── TextToVideoViewModel.kt # Generation logic
│   │   └── StylesAdapter.kt        # Style chip RecyclerView
│   ├── imagetovideo/
│   │   ├── ImageToVideoFragment.kt # Image → Video UI
│   │   └── ImageToVideoViewModel.kt
│   └── preview/
│       └── VideoPreviewActivity.kt # Full playback + save/share
└── util/
    └── PreferencesManager.kt       # SharedPreferences wrapper
```

---

## How It Works

### Text → Video
1. User enters a description (up to 1000 chars)
2. Selects a style & duration
3. App sends to Stability AI's `/v2beta/stable-video-diffusion`
4. Polls for result every 4 seconds (up to 30 attempts)
5. Downloads and caches the video
6. Opens video preview player

### Image → Video
1. User picks an image from gallery
2. Sets motion strength (1–100)
3. Optionally adds a motion description
4. App resizes image and sends to `/v2beta/image-to-video`
5. Same polling → preview flow

---

## Requirements

- Android 8.0 (API 26) or higher
- Internet connection
- Stability AI API key (with credits)

---

## Build & Run

```bash
# Clone or extract the project
cd MiruAI

# Open in Android Studio
# Sync Gradle
# Run on emulator or device
```

---

## Notes

- Video generation typically takes **30–120 seconds**
- Stability AI charges credits per generation
- Videos are cached in app's internal cache directory
- Saved videos go to `Movies/MiruAI/` in gallery

---

*Built with ❤️ using Kotlin & Stability AI*
