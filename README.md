# GPS 출퇴근 관리 시스템

GPS 기반 자동 출퇴근 관리 웹 애플리케이션

## 기술 스택

### Backend
- Java 17
- Spring Boot 3.2.1
- Spring Security + JWT
- Spring Data JPA
- H2 Database (개발용)
- PostgreSQL (운영용)

### Frontend
- Next.js 15
- TypeScript
- Tailwind CSS
- Axios

## 주요 기능

1. **사용자 인증**
   - JWT 기반 회원가입/로그인
   - 토큰 자동 갱신

2. **회사 위치 설정**
   - GPS 좌표로 회사 위치 지정
   - 출퇴근 인정 반경 설정 (50m ~ 1000m)

3. **출퇴근 관리**
   - GPS 기반 자동 거리 계산
   - 반경 내 진입 시 출근/퇴근 가능
   - 실시간 위치 추적

4. **출퇴근 기록**
   - 출퇴근 이력 조회
   - 근무 시간 자동 계산

## 설치 및 실행

### 백엔드

\`\`\`bash
cd backend

# Maven 빌드
./mvnw clean install

# 실행
./mvnw spring-boot:run
\`\`\`

서버가 http://localhost:8080 에서 실행됩니다.

### 프론트엔드

\`\`\`bash
cd frontend

# 패키지 설치
npm install

# 개발 서버 실행
npm run dev
\`\`\`

애플리케이션이 http://localhost:3000 에서 실행됩니다.

## API 엔드포인트

### 인증
- `POST /api/auth/register` - 회원가입
- `POST /api/auth/login` - 로그인

### 회사 위치
- `POST /api/workplace` - 회사 위치 저장
- `GET /api/workplace` - 회사 위치 조회

### 출퇴근
- `POST /api/attendance/checkin` - 출근
- `POST /api/attendance/checkout` - 퇴근
- `GET /api/attendance/history` - 전체 기록 조회
- `GET /api/attendance/today` - 오늘 기록 조회
- `GET /api/attendance/status` - 현재 상태 조회

## 사용 방법

1. **회원가입/로그인**
   - 이메일과 비밀번호로 계정 생성
   - 로그인하여 JWT 토큰 발급

2. **회사 위치 설정**
   - 설정 메뉴에서 회사 위치 등록
   - "현재 위치 가져오기" 버튼으로 간편 설정
   - 출퇴근 인정 반경 설정

3. **출퇴근하기**
   - 대시보드에서 현재 위치와 회사와의 거리 확인
   - 반경 내에서 출근/퇴근 버튼 클릭

4. **기록 확인**
   - 기록 메뉴에서 출퇴근 이력 조회
   - 근무 시간 자동 계산

## GPS 거리 계산

Haversine Formula를 사용하여 두 GPS 좌표 간의 거리를 미터 단위로 계산합니다.

## 보안

- 비밀번호: BCrypt 암호화
- 인증: JWT 토큰 (24시간 유효)
- CORS: localhost:3000만 허용

## 주의사항

### 웹 브라우저 제한사항
- 백그라운드에서 GPS 추적 제한적
- HTTPS 환경에서만 GPS 정확도 향상
- 배터리 소모 고려 필요

### 개선 방향
실제 프로덕션 환경에서는:
1. React Native로 네이티브 앱 개발 권장
2. 백그라운드 위치 추적 구현
3. Push 알림 기능 추가
4. PostgreSQL 등 운영 DB 사용

## 라이선스

MIT
