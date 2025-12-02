// src/utils/usePublicKey.ts
import { useState, useEffect } from "react";
import axios from "axios";
import { API_SERVER_HOST } from "@/api/hostApi";

interface JWTRSAResponse {
    publicKey: string;
    token: string;
    expiresIn: number;
}

export function usePublicKey() {
    const [publicKey, setPublicKey] = useState<string>("");
    const [jwtToken, setJwtToken] = useState<string>("");
    const [isFetched, setIsFetched] = useState<boolean>(false);
    const [error, setError] = useState<string>("");

    useEffect(() => {
        if (isFetched) return;

        // Fixed RSA 방식 사용 (Redis 불필요)
        const apiUrl = `${API_SERVER_HOST}/api/pub-key`;
        console.log("🔍 공개키 요청 URL:", apiUrl);
        console.log("🔍 API_SERVER_HOST:", API_SERVER_HOST);

        axios
            .get<JWTRSAResponse>(apiUrl)
            .then((res) => {
                console.log("✅ 공개키 응답:", res.data);
                setPublicKey(res.data.publicKey);
                // Fixed RSA 방식: token 저장하지 않음
                // setJwtToken(res.data.token);
                setIsFetched(true);
                console.log("🔐 공개키 가져옴 (Fixed RSA 방식)");
            })
            .catch((err) => {
                const errorMsg = err.response?.data?.message || err.message || "알 수 없는 오류";
                console.error("❌ 공개키 가져오기 실패");
                console.error("📍 요청 URL:", apiUrl);
                console.error("📍 상태 코드:", err.response?.status);
                console.error("📍 에러 메시지:", errorMsg);
                console.error("📍 전체 에러:", err);
                setError(errorMsg);
            });
    }, [isFetched]);

    return { publicKey, jwtToken, isFetched, error };
}

export default usePublicKey;
