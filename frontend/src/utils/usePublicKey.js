// src/utils/usePublicKey.ts
import { useState, useEffect } from "react";
import axios from "axios";
import { API_SERVER_HOST } from "@/api/hostApi";
export function usePublicKey() {
    const [publicKey, setPublicKey] = useState("");
    const [jwtToken, setJwtToken] = useState("");
    const [isFetched, setIsFetched] = useState(false);
    const [error, setError] = useState("");
    useEffect(() => {
        if (isFetched)
            return;
        // JWT-RSA 방식 사용 (일회용 임시 키, Redis 저장)
        const apiUrl = `${API_SERVER_HOST}/api/jwt-pub-key`;
        console.log("🔍 공개키 요청 URL:", apiUrl);
        console.log("🔍 API_SERVER_HOST:", API_SERVER_HOST);
        axios
            .get(apiUrl)
            .then((res) => {
            console.log("✅ 공개키 응답:", res.data);
            setPublicKey(res.data.publicKey);
            setJwtToken(res.data.token);
            setIsFetched(true);
            console.log("🔐 공개키 가져옴 (JWT-RSA 방식)");
            console.log("⏱️ 만료 시간:", res.data.expiresIn, "초");
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
