# TodoList Gemini 설정

#개발 중간중간 > Save Notes 실행 → docs/todo-progress.md 자동 누적 #세션 끝내기 전 > Summarize Session 실행 → 어떤 파일이 바뀌었는지 + 오늘 한 일 기록 #다음 세션 시작할 때 docs/todo-progress.md 불러오기

⚠️ 모든 응답은 한국어(존댓말)로 해주세요.

이 프로젝트는 **React + TypeScript + Vite + Tailwind** 프론트엔드와  
**NestJS 백엔드**로 구성된 투두리스트 앱입니다.  
Gemini CLI 실행 시 아래 명령어로 서버를 실행할 수 있습니다.

commands:

- name: start backend
  run: pnpm --filter backend start:dev
  description: NestJS 백엔드 서버 실행

- name: start frontend
  run: pnpm --filter frontend dev
  description: React + TypeScript + Vite 프론트 서버 실행

- name: lint
  run: pnpm eslint .
  description: 전체 ESLint 체크

- name: save Session Notes
  run: sequential-thinking plan "지금까지 한 작업을 요약해서 마크다운 문서 형식으로 정리해줘"
  description: 현재 세션 작업 기록을 문서

- name: summarize Session
  run: sequential-thinking plan "오늘 세션에서 수정되거나 생성된 파일과 주요 작업 내역을 정리해줘"
  description: 세션 종료 전 작업 요약

- name: save notes
  run: sequential-thinking plan "지금까지 진행한 투두리스트 작업 요약을 docs/todo-progress.md 파일로 저장해줘"
  description: 진행 상황 자동 요약 및 저장

- name: load
  run: local-gemini read-file docs/todo-progress.md
  description: 지난 세션 작업 요약 불러오기

- name: plan
  run: sequential-thinking plan "{input}"
  description: 단계별 플랜을 사용자 입력 기반으로 생성

files:

- frontend/src/\*\*
- backend/src/\*\*
- frontend/package.json
- backend/package.json
