# BalanceTube - Android App (MVP)

BalanceTube is an Android application that analyzes your YouTube watch history and provides balanced content recommendations across 6 different interest categories.

## Features

- **Google OAuth Login** - Secure authentication with Google Sign-In
- **Watch History Analysis** - Automatically fetches and categorizes your YouTube watch history
- **6-Category Balance** - Analyzes content across:
  - Knowledge (Education, Science, History)
  - Entertainment (Comedy, Gaming, Shows)
  - Lifestyle (Cooking, Travel, DIY)
  - Arts & Music (Music, Art, Performance)
  - Self-Improvement (Productivity, Personal Development)
  - Social / Creator (Vlogs, Community Content)
- **Radar Chart Visualization** - Visual representation of your content balance
- **Smart Recommendations** - Get video suggestions for under-consumed categories
- **Local Storage** - All data stored securely on your device
- **Privacy First** - No server uploads, complete data deletion available

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Architecture:** MVVM with Clean Architecture
- **Async:** Kotlin Coroutines + Flow
- **DI:** Hilt
- **Networking:** Retrofit + OkHttp + Moshi
- **Database:** Room
- **Security:** EncryptedSharedPreferences
- **Auth:** Google Sign-In (play-services-auth)
- **API:** YouTube Data API v3

## Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 28 (Android 9.0) or higher
- JDK 17
- Google Cloud Project with YouTube Data API v3 enabled

## Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/parktaejun-dev/balance-tube.git
cd balance-tube
```

### 2. Configure Google Cloud Project

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Enable **YouTube Data API v3**
4. Create OAuth 2.0 credentials:
   - Go to "Credentials" → "Create Credentials" → "OAuth Client ID"
   - Select "Android" as application type
   - Package name: `com.balancetube`
   - Get SHA-1 certificate fingerprint:
     ```bash
     keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
     ```
   - Copy the SHA-1 fingerprint
5. Also create "Web application" OAuth Client ID for server auth code
6. Download the `google-services.json` file

### 3. Add google-services.json

Place the downloaded `google-services.json` file in the `app/` directory.

### 4. Update Configuration

**app/build.gradle:**
- Update `resValue "string", "default_web_client_id"` with your actual Web Client ID from Google Cloud Console

**di/NetworkModule.kt:**
- Replace `YOUR_API_KEY_HERE` with your YouTube Data API v3 key

### 5. Build and Run

1. Open the project in Android Studio
2. Sync Gradle files
3. Build the project
4. Run on an emulator or physical device (Android 9.0+)

## Project Structure

```
com.balancetube/
├── data/
│   ├── local/           # Room database entities and DAOs
│   ├── remote/          # API services and models
│   └── repository/      # Data repository layer
├── domain/
│   └── model/           # Domain models
├── ui/
│   ├── screen/          # Compose screens
│   ├── component/       # Reusable UI components
│   ├── theme/           # App theme
│   └── navigation/      # Navigation setup
├── di/                  # Hilt dependency injection modules
└── util/                # Utility classes
```

## Usage

1. **Login:** Sign in with your Google account
2. **Sync History:** Tap "Sync Watch History" to fetch your YouTube data
3. **View Balance:** See your content consumption across 6 categories on the radar chart
4. **Get Recommendations:** Tap "Recommended to Restore Balance" to see videos from your lowest category
5. **Manage Data:** Go to Settings to delete all local data or logout

## API Limits

YouTube Data API v3 has quota limits:
- Default quota: 10,000 units/day
- Playlist items list: 1 unit per request
- Videos list: 1 unit per request
- Search: 100 units per request

The app is optimized to minimize API calls.

## Security Features

- Email addresses are hashed using SHA-256 before storage
- Local database stored in app's private storage
- EncryptedSharedPreferences for sensitive data
- No external data transmission (all processing is local)
- Complete data deletion available in Settings

## Known Limitations (MVP)

- Watch history API may have limited availability based on YouTube user settings
- Category classification uses rule-based keywords (could be improved with ML)
- Recommendations limited to 5 videos per category
- No offline mode support
- Icons are placeholders (need custom app icons)

## Future Enhancements

- Machine learning-based category classification
- Historical trend tracking
- Custom category creation
- Export reports
- Widget support
- Dark theme toggle

## Troubleshooting

**Login fails:**
- Verify Google Cloud OAuth credentials are correctly configured
- Check package name and SHA-1 fingerprint match
- Ensure YouTube Data API v3 is enabled

**No watch history:**
- YouTube watch history must be enabled in YouTube settings
- User must have previously watched videos
- Some users may have restricted API access

**API quota exceeded:**
- Wait until quota resets (daily)
- Reduce sync frequency

## License

This project is created for educational and portfolio purposes.

## Contact

For questions or issues, please open a GitHub issue or contact the repository owner.

---

**Note:** This is an MVP (Minimum Viable Product) version. Some features may require additional configuration or may have limitations.
