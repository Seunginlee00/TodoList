"use client";

import { useState, useMemo } from "react";
import { useNavigate } from "react-router-dom"; // ✅ 교체
import axios from "axios";
import { encryptPassword } from "@/utils/encryptUtil";
import { API_SERVER_HOST } from "@/api/hostApi";
import { usePublicKey } from "@/utils/usePublicKey";

export default function LoginPage() {
    const navigate = useNavigate(); // ✅ 수정
    const { publicKey, jwtToken } = usePublicKey();

    // 상태
    const [userId, setUserId] = useState("");
    const [pw, setPw] = useState("");
    const [loading, setLoading] = useState(false);
    const [fieldErrors, setFieldErrors] = useState<{ userId?: string; pw?: string }>({});
    const [errorMessage, setErrorMessage] = useState("");

    // 폼 유효성 검사
    const isFormValid = useMemo(() => {
        return userId.trim() && pw && !fieldErrors.userId && !fieldErrors.pw;
    }, [userId, pw, fieldErrors]);

    const validateForm = (): boolean => {
        const errors: { userId?: string; pw?: string } = {};
        if (!userId.trim()) errors.userId = "아이디를 입력하세요.";
        if (!pw) errors.pw = "비밀번호를 입력하세요.";
        setFieldErrors(errors);
        return Object.keys(errors).length === 0;
    };

    // 로그인 처리
    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setErrorMessage("");

        if (!validateForm()) return;
        if (!publicKey) {
            setErrorMessage("보안 키 정보를 불러올 수 없습니다.");
            return;
        }

        setLoading(true);
        try {
            const encryptedPw = encryptPassword(pw, publicKey);
            if (!encryptedPw) {
                setErrorMessage("비밀번호 암호화에 실패했습니다.");
                return;
            }

            // 고정 키 방식은 token 불필요
            const payload = {
                userId,
                password: encryptedPw,
            };

            console.log("🔐 로그인 요청:", { userId, password: "***암호화됨***" });
            const res = await axios.post(`${API_SERVER_HOST}/api/user/login`, payload);
            const data = res.data;
            console.log("✅ 로그인 응답:", data);

            if (data?.success) {
                alert("로그인 성공");
                navigate("/"); // ✅ 수정
            } else {
                setErrorMessage("로그인 실패: 아이디 또는 비밀번호를 확인하세요.");
            }
        } catch (err) {
            console.error("🚨 로그인 오류:", err);
            setErrorMessage("네트워크 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="flex justify-center items-center min-h-screen bg-gray-50">
            <form
                onSubmit={handleSubmit}
                className="w-full max-w-md bg-white shadow-lg rounded-lg p-8"
            >
                <h1 className="text-2xl font-bold text-center text-blue-600 mb-6">Login</h1>

                {/* UserID */}
                <label className="block font-semibold mb-1">
                    <span className="text-red-500">*</span> UserID
                </label>
                <input
                    type="text"
                    value={userId}
                    onChange={(e) => {
                        setUserId(e.target.value);
                        setFieldErrors((prev) => ({ ...prev, userId: "" }));
                    }}
                    placeholder="아이디를 입력하세요"
                    className={`w-full border rounded px-3 py-2 mb-2 ${
                        fieldErrors.userId ? "border-red-500" : "border-gray-300"
                    }`}
                />
                {fieldErrors.userId && (
                    <p className="text-red-500 text-sm mb-4">{fieldErrors.userId}</p>
                )}

                {/* Password */}
                <label className="block font-semibold mb-1">
                    <span className="text-red-500">*</span> Password
                </label>
                <input
                    type="password"
                    value={pw}
                    onChange={(e) => {
                        setPw(e.target.value);
                        setFieldErrors((prev) => ({ ...prev, pw: "" }));
                    }}
                    placeholder="비밀번호를 입력하세요"
                    className={`w-full border rounded px-3 py-2 mb-2 ${
                        fieldErrors.pw ? "border-red-500" : "border-gray-300"
                    }`}
                />
                {fieldErrors.pw && (
                    <p className="text-red-500 text-sm mb-4">{fieldErrors.pw}</p>
                )}

                {/* 에러 메시지 */}
                {errorMessage && (
                    <div className="bg-red-100 text-red-600 text-sm p-2 rounded mb-4">
                        {errorMessage}
                    </div>
                )}

                {/* 버튼 */}
                <button
                    type="submit"
                    disabled={loading || !isFormValid}
                    className="w-full bg-blue-600 text-white py-2 rounded-lg font-semibold disabled:opacity-50"
                >
                    {loading ? "로그인 중..." : "로그인"}
                </button>
            </form>
        </div>
    );
}
