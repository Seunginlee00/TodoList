// src/util/usePwPublicKey.ts
import { useState, useEffect } from "react";
import axios from "axios";
import { API_SERVER_HOST } from "@/api/hostApi";
export function usePwPublicKey() {
    const [publicKey, setPublicKey] = useState("");
    const [isFetched, setIsFetched] = useState(false);
    useEffect(() => {
        if (isFetched)
            return;
        axios
            .get(`${API_SERVER_HOST}/api/pub-key`)
            .then((res) => {
            setPublicKey(res.data.publicKey);
            setIsFetched(true);
            console.log("🔐 비밀번호 변경용 공개키 가져옴");
        })
            .catch((err) => {
            console.error("❌ 비밀번호 변경용 공개키 가져오기 실패", err);
        });
    }, [isFetched]);
    return { publicKey };
}
