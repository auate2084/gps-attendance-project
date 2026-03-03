# iOS Project (Swift + SwiftUI)

이 폴더는 SwiftUI 기반 iOS 앱 스캐폴드입니다.

## 포함 내용
- `GPSAttendance/` : 앱 소스
- `project.yml` : XcodeGen 프로젝트 정의

## Mac에서 프로젝트 열기
1. Homebrew가 없다면 설치
2. `brew install xcodegen`
3. 이 폴더에서 `xcodegen generate`
4. 생성된 `GPSAttendanceiOS.xcodeproj`를 Xcode로 열기

## 참고
- 현재 위치 권한 요청/단일 위치 조회 화면이 포함되어 있습니다.
- 실제 배포 전에는 AppIcon 이미지와 팀 서명(Development Team) 설정이 필요합니다.
