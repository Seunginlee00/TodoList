"use client";

import { useState, useMemo } from "react";
import { useNavigate } from "react-router-dom"; // ✅ 교체
import axios from "axios";
import { encryptPassword } from "@/utils/encryptUtil";
import { API_SERVER_HOST } from "@/api/hostApi";
import { usePublicKey } from "@/utils/usePublicKey";

export default function LoginPage() {
    const navigate = useNavigate(); // ✅ 수정
    const { publicKey } = usePublicKey();

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

            if (data?.success && data?.accessToken) {
                // ✅ JWT 토큰을 localStorage에 저장
                localStorage.setItem("accessToken", data.accessToken);
                localStorage.setItem("refreshToken", data.refreshToken);
                localStorage.setItem("userId", data.userId);
                localStorage.setItem("userNm", data.userNm);

                alert(`로그인 성공! ${data.userNm}님 환영합니다.`);
                navigate("/"); // ✅ 메인 페이지로 이동
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
        <div
            className="flex flex-row justify-center items-center min-h-screen bg-gradient-to-br from-blue-50 via-indigo-50 to-purple-50 p-4"
            style={{ display: 'flex', flexDirection: 'row', justifyContent: 'center', alignItems: 'center', minHeight: '100vh' }}
        >
            <form
                onSubmit={handleSubmit}
                className="max-w-md bg-white/80 backdrop-blur-lg shadow-2xl rounded-2xl p-10 border border-white/20"
                style={{ width: '100%', maxWidth: '448px' }}
            >
                <div className="text-center mb-8">
                    <h1 className="text-4xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent mb-2">
                        로그인
                    </h1>
                    <p className="text-gray-500 text-sm">계정에 로그인하여 계속하세요</p>
                </div>

                <div className="space-y-5 flex flex-col items-center">
                    {/* UserID */}
                    <div className="w-full max-w-xs">
                        <label className="block text-sm font-medium text-gray-700 mb-2 text-center">
                            아이디
                        </label>
                        <input
                            type="text"
                            value={userId}
                            onChange={(e) => {
                                setUserId(e.target.value);
                                setFieldErrors((prev) => ({ ...prev, userId: "" }));
                            }}
                            placeholder="아이디를 입력하세요"
                            className={`w-full border rounded-xl px-4 py-3 text-gray-800 text-center placeholder-gray-400 focus:outline-none focus:ring-2 transition duration-200 ${
                                fieldErrors.userId
                                    ? "border-red-400 focus:ring-red-300"
                                    : "border-gray-200 focus:ring-blue-300 focus:border-transparent"
                            }`}
                        />
                        {fieldErrors.userId && (
                            <p className="text-red-500 text-xs mt-1 text-center">{fieldErrors.userId}</p>
                        )}
                    </div>

                    {/* Password */}
                    <div className="w-full max-w-xs">
                        <label className="block text-sm font-medium text-gray-700 mb-2 text-center">
                            비밀번호
                        </label>
                        <input
                            type="password"
                            value={pw}
                            onChange={(e) => {
                                setPw(e.target.value);
                                setFieldErrors((prev) => ({ ...prev, pw: "" }));
                            }}
                            placeholder="비밀번호를 입력하세요"
                            className={`w-full border rounded-xl px-4 py-3 text-gray-800 text-center placeholder-gray-400 focus:outline-none focus:ring-2 transition duration-200 ${
                                fieldErrors.pw
                                    ? "border-red-400 focus:ring-red-300"
                                    : "border-gray-200 focus:ring-blue-300 focus:border-transparent"
                            }`}
                        />
                        {fieldErrors.pw && (
                            <p className="text-red-500 text-xs mt-1 text-center">{fieldErrors.pw}</p>
                        )}
                    </div>

                    {/* 에러 메시지 */}
                    {errorMessage && (
                        <div className="w-full max-w-xs bg-red-50 border border-red-200 text-red-700 text-sm p-3 rounded-xl text-center">
                            {errorMessage}
                        </div>
                    )}

                    {/* 로그인 버튼 */}
                    <button
                        type="submit"
                        disabled={loading || !isFormValid}
                        className="w-full max-w-xs bg-gradient-to-r from-blue-600 to-purple-600 text-white py-3 rounded-xl font-semibold text-base hover:from-blue-700 hover:to-purple-700 transform hover:scale-[1.02] transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none shadow-lg hover:shadow-xl"
                    >
                        {loading ? "로그인 중..." : "로그인"}
                    </button>

                    {/* 회원가입 버튼 */}
                    <button
                        type="button"
                        onClick={() => navigate("/register")}
                        className="w-full max-w-xs bg-white border-2 border-gray-200 text-gray-700 py-3 rounded-xl font-semibold text-base hover:bg-gray-50 hover:border-gray-300 transform hover:scale-[1.02] transition-all duration-200 shadow-md hover:shadow-lg"
                    >
                        회원가입
                    </button>
                </div>
            </form>
    
        </div>
    );
}
