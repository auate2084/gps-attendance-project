# GPS Attendance

GPS 기반 출퇴근 관리 프로젝트입니다. 이 저장소는 아래 4개 앱으로 구성되어 있습니다.

- `backend`: Spring Boot API 서버 (Gradle)
- `frontend`: Next.js 웹 앱
- `android-app`: Kotlin + Jetpack Compose 안드로이드 앱
- `ios-app`: Swift + SwiftUI iOS 앱 스캐폴드

## 1) 사전 준비

### 공통
- Git

### Backend
- JDK 17
- Docker Desktop (PostgreSQL, Redis 실행용)

### Frontend
- Node.js 20+

### Android
- Android Studio
- Android SDK (minSdk 26 이상)

### iOS
- macOS + Xcode (Windows에서는 빌드/실행 불가)

## 2) 빠른 실행 순서

1. DB/Redis 실행
2. Backend 실행 (Gradle)
3. Frontend 실행 (선택)
4. Android 또는 iOS 앱 실행

---

## 3) Backend 실행 (Gradle)

`backend`는 Maven이 아니라 **Gradle Wrapper**를 사용합니다.

### 3-1. 인프라(PostgreSQL, Redis) 실행

```bash
cd backend
docker compose up -d
```

기본 포트:
- PostgreSQL: `5432`
- Redis: `6379`

### 3-2. 서버 실행

Windows:
```bash
cd backend
.\gradlew.bat bootRun
```

macOS/Linux:
```bash
cd backend
./gradlew bootRun
```

서버 주소:
- `http://localhost:8080`

### 3-3. 테스트 실행

Windows:
```bash
cd backend
.\gradlew.bat test
```

macOS/Linux:
```bash
cd backend
./gradlew test
```

### 3-4. 주요 환경변수 (선택)

`backend/src/main/resources/application.yml` 기준 기본값:
- `DB_HOST=localhost`
- `DB_PORT=5432`
- `DB_NAME=gps_attendance`
- `DB_SCHEMA=attendance`
- `DB_USERNAME=gps_user`
- `DB_PASSWORD=gps_pass`
- `REDIS_HOST=localhost`
- `REDIS_PORT=6379`

---

## 4) Frontend 실행

```bash
cd frontend
npm install
npm run dev
```

웹 주소:
- `http://localhost:3000`

---

## 5) Android 실행

1. Android Studio에서 `android-app` 열기
2. Gradle Sync
3. 백엔드 실행 상태 확인
4. 에뮬레이터 또는 실기기에서 앱 실행

기본 API 주소:
- `android-app/app/build.gradle.kts`의 `BASE_URL`
- 기본값: `http://10.0.2.2:8080/` (에뮬레이터 기준)

카카오 지도 키:
- `KAKAO_NATIVE_APP_KEY`를 `gradle.properties`에 설정 필요

---

## 6) iOS 실행

`ios-app`은 SwiftUI 기반 스캐폴드이며, macOS에서만 실행 가능합니다.

```bash
cd ios-app
brew install xcodegen
xcodegen generate
```

생성된 `GPSAttendanceiOS.xcodeproj`를 Xcode로 열어 실행하세요.

참고:
- 지도 UI는 시뮬레이터에서도 확인 가능
- 실제 GPS 정확도/백그라운드 위치 검증은 실기기(iPhone) 권장

---

## 7) 현재 API 경로

백엔드 컨트롤러 기준:
- `POST /api/v1/users/register`
- `POST /api/v1/users/login`
- `POST /api/v1/users/refresh`
- `POST /api/v1/attendance/me/location`
- `GET /api/v1/attendance/me/sessions`
- `GET /api/v1/attendance/visible-sessions`
- `GET /api/v1/teams`
- `POST /api/v1/teams`
- `POST /api/v1/teams/work-policies`
