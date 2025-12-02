import axios from "axios";
import { API_SERVER_HOST } from "./hostApi";

// Axios 인스턴스 생성
const apiClient = axios.create({
    baseURL: API_SERVER_HOST,
    timeout: 10000,
    headers: {
        "Content-Type": "application/json",
    },
});

// 요청 인터셉터: JWT 토큰 자동 추가
apiClient.interceptors.request.use(
    (config) => {
        const accessToken = localStorage.getItem("accessToken");

        if (accessToken) {
            config.headers.Authorization = `Bearer ${accessToken}`;
        }

        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// 응답 인터셉터: 401 에러 처리 (토큰 만료)
apiClient.interceptors.response.use(
    (response) => {
        return response;
    },
    async (error) => {
        const originalRequest = error.config;

        // 401 에러이고, 재시도하지 않은 요청인 경우
        if (error.response?.status === 401 && !originalRequest._retry) {
            originalRequest._retry = true;

            try {
                // 리프레시 토큰으로 새 액세스 토큰 발급
                const refreshToken = localStorage.getItem("refreshToken");
                const response = await axios.post(`${API_SERVER_HOST}/api/user/refresh`, {
                    refreshToken,
                });

                const { accessToken } = response.data;

                // 새 토큰 저장
                localStorage.setItem("accessToken", accessToken);

                // 원래 요청 재시도
                originalRequest.headers.Authorization = `Bearer ${accessToken}`;
                return apiClient(originalRequest);
            } catch (refreshError) {
                // 리프레시 토큰도 만료된 경우 로그아웃
                console.error("토큰 갱신 실패, 로그아웃 처리");
                localStorage.clear();
                window.location.href = "/login";
                return Promise.reject(refreshError);
            }
        }

        return Promise.reject(error);
    }
);

export default apiClient;
