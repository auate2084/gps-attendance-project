# iOS App (Swift + SwiftUI)

GPS Attendance iOS 앱 스캐폴드입니다.

## 기술 스택
- Swift
- SwiftUI
- CoreLocation
- XcodeGen (프로젝트 생성)

## 포함 내용
- SwiftUI 앱 기본 구조
- 위치 권한 요청
- 단일 위치 조회 샘플

## 실행 환경
- iOS 앱 빌드/실행은 macOS + Xcode가 필요합니다.
- Windows에서는 소스 편집만 가능하고 실행은 불가합니다.

## 실행 방법 (macOS)
```bash
cd ios-app
brew install xcodegen
xcodegen generate
```

그 다음:
1. 생성된 `GPSAttendanceiOS.xcodeproj`를 Xcode에서 엽니다.
2. Signing(Development Team) 설정을 합니다.
3. 시뮬레이터 또는 실기기에서 실행합니다.

## 지도/GPS 테스트 가이드
- 지도 렌더링(UI 확인): 시뮬레이터 가능
- 실제 GPS 정확도/백그라운드 위치: 실기기(iPhone) 권장

## 주요 파일
- `project.yml`
- `GPSAttendance/GPSAttendanceApp.swift`
- `GPSAttendance/ContentView.swift`
- `GPSAttendance/Core/LocationPermissionManager.swift`
