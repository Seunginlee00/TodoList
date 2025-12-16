# 프로젝트 로드맵: Todo List + Calendar + Memo (With Horoscope)

이 문서는 React(프론트엔드)와 Spring Boot(백엔드)를 사용한 종합 일정 관리 애플리케이션 개발 로드맵입니다.

## 1. 프로젝트 개요
*   **목표**: 단순한 투두리스트를 넘어 캘린더, 메모 기능을 통합하고, 추후 운세 기능을 추가하여 사용자의 하루를 관리하고 조언해주는 플랫폼 구축.
*   **핵심 기술 스택**:
    *   **Frontend**: React (Vite 권장), JS/TS
    *   **Backend**: Spring Boot (Java), Spring Data JPA
    *   **Database**: MySQL 또는 H2 (개발용), 추후 결정
    *   **Style**: (제안) TailwindCSS 또는 Styled Components (세련된 UI 구현을 위함)

## 2. 개발 단계 (Phases)

### Phase 1: 프로젝트 셋팅 및 기본 골격 (Foundation)
*   **Backend**:
    *   Spring Boot 프로젝트 생성 (Dependencies: Web, JPA, Lombok, Security, MySQL setup).
    *   기본 패키지 구조 설계 (Controller, Service, Repository, DTO).
    *   데이터베이스 연동 확인.
*   **Frontend**:
    *   React 프로젝트 생성 (Vite 사용).
    *   기본 폴더 구조 설계 (components, pages, api, hooks).
    *   기본 라우팅 설정 (React Router).
*   **Infra/Common**:
    *   API 명세서 작성 (Swagger/OpenAPI 설정).
    *   CORS 설정.

### Phase 2: 핵심 기능 구현 (Core Features - MVP)
*   **인증/인가 (Auth)**:
    *   회원가입, 로그인 (Spring Security + JWT).
    *   프론트엔드 로그인 페이지 및 토큰 처리.
*   **할 일 관리 (Todo)**:
    *   CRUD (생성, 조회, 수정, 삭제).
    *   완료 체크, 우선순위 설정.
*   **메모 (Memo)**:
    *   간단한 텍스트 에디터 구현.
    *   메모 저장 및 목록 조회.
*   **캘린더 (Calendar)**:
    *   월간/주간 달력 뷰 구현 (라이브러리 활용 or 직접 구현).
    *   달력에 Todo 표시.

### Phase 3: 고도화 및 확장 (Enhancement)
*   **운세/사주 (Horoscope/Fortune)**:
    *   외부 운세 API 연동 또는 자체 로직(간단한 띠별 운세 등) 구현.
    *   매일 아침 '오늘의 운세'와 함께 할 일 추천 기능.
*   **UI/UX 개선**:
    *   다크 모드 지원.
    *   드래그 앤 드롭 (Todo 순서 변경).
    *   애니메이션 효과 추가.

## 3. 상세 개발 순서 (Step-by-Step Guide)

1.  **환경 설정**: JDK, Node.js, IDE 설치 확인.
2.  **백엔드 API 개발**: 인증 -> 투두 -> 메모 -> 캘린더 순으로 API 개발.
3.  **프론트엔드 UI 개발**: 공통 컴포넌트 -> 페이지 레이아웃 -> 기능 연동.
4.  **통합 테스트**: 프론트-백엔드 연동 확인 및 버그 수정.

## 4. 디렉토리 구조 제안
```
project-root/
├── backend/ (Spring Boot)
└── frontend/ (React)
```
