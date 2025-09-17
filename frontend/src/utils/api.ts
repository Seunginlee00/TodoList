// src/utils/api.ts
export async function apiFetch(url: string, options: RequestInit = {}) {
    const token = localStorage.getItem("token");

    const res = await fetch(url, {
        ...options,
        headers: {
            ...(options.headers || {}),
            "Authorization": token ? `Bearer ${token}` : "",
            "Content-Type": "application/json",
        },
    });

    // 토큰 만료시 처리
    if (res.status === 401) {
        const refreshed = await refreshToken();
        if (!refreshed) {
            // refresh 실패 → 로그인 페이지
            localStorage.removeItem("token");
            window.location.href = "/login";
            return Promise.reject("Unauthorized");
        }
        // 새 토큰으로 재시도
        return apiFetch(url, options);
    }

    return res;
}

async function refreshToken() {
    try {
        const res = await fetch("/api/auth/refresh", {
            method: "POST",
            credentials: "include", // refresh token은 httpOnly 쿠키에 있다고 가정
        });
        if (!res.ok) return false;

        const data = await res.json();
        localStorage.setItem("token", data.token);
        return true;
    } catch {
        return false;
    }
}
