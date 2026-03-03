# Android App (Kotlin + Compose)

GPS Attendance 안드로이드 앱입니다.

## 기술 스택
- Kotlin
- Jetpack Compose
- Hilt
- Retrofit + OkHttp
- DataStore
- Google Location Services
- Kakao Maps SDK

## 사전 준비
- Android Studio
- Android SDK (minSdk 26)
- 실행 중인 백엔드 (`http://localhost:8080`)

## 실행 방법
1. Android Studio에서 `android-app` 폴더를 엽니다.
2. Gradle Sync를 완료합니다.
3. 에뮬레이터 또는 실기기를 실행합니다.
4. 앱을 빌드/실행합니다.

## API 서버 주소
- 설정 위치: `app/build.gradle.kts`
- 키: `BASE_URL`
- 기본값: `http://10.0.2.2:8080/` (안드로이드 에뮬레이터 기준)

실기기 테스트 시:
- `10.0.2.2` 대신 PC의 LAN IP로 변경해야 합니다.

## 카카오맵 설정
1. Kakao Developers에서 Native App Key 발급
2. `gradle.properties` 또는 `~/.gradle/gradle.properties`에 아래 추가
```properties
KAKAO_NATIVE_APP_KEY=YOUR_KAKAO_NATIVE_APP_KEY
```
3. 앱 실행

## 구현된 주요 API 연동
- `POST /api/v1/users/login`
- `POST /api/v1/users/refresh`
- `POST /api/v1/attendance/me/location`
- `GET /api/v1/attendance/me/sessions`
