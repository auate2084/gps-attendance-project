# GPS Attendance Android App

## Scope
This Android app is wired to the backend APIs under `/api/v1`.
Implemented features:
- Login: `POST /api/v1/users/login`
- Token refresh: `POST /api/v1/users/refresh`
- Send current location: `POST /api/v1/attendance/me/location`
- Fetch my sessions: `GET /api/v1/attendance/me/sessions`

## How To Run
1. Open `android-app` in Android Studio.
2. Sync Gradle.
3. Start backend server first.
4. Run app on emulator and log in.

## Base URL
- Default: `http://10.0.2.2:8080/`
- Location: `app/build.gradle.kts` (`BASE_URL`)

For a physical device, replace `10.0.2.2` with your PC LAN IP.

## Map Setup
- This app shows a Kakao Map after login.
- Issue a Kakao Native App Key from Kakao Developers.
- Kakao Maps SDK repository is configured in `settings.gradle.kts`:
  - `maven("https://devrepo.kakao.com/nexus/content/groups/public/")`
- Add it in `~/.gradle/gradle.properties` or project `gradle.properties`:
  - `KAKAO_NATIVE_APP_KEY=YOUR_KAKAO_NATIVE_APP_KEY`
- `GpsAttendanceApp` initializes Kakao Maps SDK with `BuildConfig.KAKAO_NATIVE_APP_KEY`.
