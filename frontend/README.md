# Frontend (Next.js)

GPS Attendance 웹 프론트엔드입니다.

## 기술 스택
- Next.js 15
- TypeScript
- React 18
- Tailwind CSS
- Axios

## 사전 준비
- Node.js 20+

## 실행 방법
```bash
cd frontend
npm install
npm run dev
```

웹 주소:
- `http://localhost:3000`

## 빌드/프로덕션 실행
```bash
cd frontend
npm run build
npm run start
```

## API 설정
- 기본 API 주소: `http://localhost:8080/api/v1`
- 환경변수로 변경 가능: `NEXT_PUBLIC_API_BASE_URL`

예시:
```bash
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080/api/v1
```

## 현재 동작 방식 참고
- 로그인/회원가입은 `/users/login`, `/users/register`를 사용합니다.
- 출퇴근 처리는 `/attendance/me/location`을 사용합니다.
- 회사 위치 설정은 프론트 로컬 스토리지에 저장되며, 서버 정책 생성(`/teams/work-policies`)은 권한이 있을 때만 반영됩니다.
