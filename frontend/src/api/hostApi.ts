// hostApi.ts
// Vite 개발 모드에서는 프록시를 사용하므로 빈 문자열
// 프로덕션 빌드 시에는 환경변수 사용
export const API_SERVER_HOST = import.meta.env.VITE_API_SERVER_HOST || '';
export const LOGIN_PATH = "/api/user/login";

// 디버깅용
console.log('🔧 API_SERVER_HOST:', API_SERVER_HOST);
console.log('🔧 환경변수:', import.meta.env);
