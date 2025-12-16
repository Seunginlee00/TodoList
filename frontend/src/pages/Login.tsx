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
        <div className="flex justify-center items-center min-h-screen bg-amber-100 p-4"> {/* 배경색 아이보리/연한 갈색 */}
            <form
                onSubmit={handleSubmit}
                className="w-full max-w-md bg-white shadow-2xl rounded-xl p-8 text-center" /* 라운드 처리, 그림자, 중앙 정렬 */
            >
                <h1 className="text-3xl font-extrabold text-amber-800 mb-8">로그인</h1> {/* 제목 중앙 정렬 및 색상 변경 */}

                {/* UserID */}
                <label className="block font-semibold text-gray-700 mb-2">
                    <span className="text-red-500">*</span> 아이디
                </label>
                <input
                    type="text"
                    value={userId}
                    onChange={(e) => {
                        setUserId(e.target.value);
                        setFieldErrors((prev) => ({ ...prev, userId: "" }));
                    }}
                    placeholder="아이디를 입력하세요"
                    className={`w-full border-2 rounded-lg px-4 py-3 mb-4 focus:outline-none focus:border-amber-500 transition duration-300 ${
                        fieldErrors.userId ? "border-red-500" : "border-gray-200"
                    }`}
                />
                {fieldErrors.userId && (
                    <p className="text-red-500 text-sm -mt-2 mb-4">{fieldErrors.userId}</p>
                )}

                {/* Password */}
                <label className="block font-semibold text-gray-700 mb-2">
                    <span className="text-red-500">*</span> 비밀번호
                </label>
                <input
                    type="password"
                    value={pw}
                    onChange={(e) => {
                        setPw(e.target.value);
                        setFieldErrors((prev) => ({ ...prev, pw: "" }));
                    }}
                    placeholder="비밀번호를 입력하세요"
                    className={`w-full border-2 rounded-lg px-4 py-3 mb-4 focus:outline-none focus:border-amber-500 transition duration-300 ${
                        fieldErrors.pw ? "border-red-500" : "border-gray-200"
                    }`}
                />
                {fieldErrors.pw && (
                    <p className="text-red-500 text-sm -mt-2 mb-4">{fieldErrors.pw}</p>
                )}

                {/* 에러 메시지 */}
                {errorMessage && (
                    <div className="bg-red-100 text-red-600 text-sm p-3 rounded-lg mb-4 text-left">
                        {errorMessage}
                    </div>
                )}

                {/* 버튼 */}
                <button
                    type="submit"
                    disabled={loading || !isFormValid}
                    className="w-full bg-amber-600 text-white py-3 rounded-lg font-bold text-lg hover:bg-amber-700 transition duration-300 disabled:opacity-50"
                >
                    {loading ? "로그인 중..." : "로그인"}
                </button>

                {/* 회원가입 버튼 */}
                <button
                    type="button"
                    onClick={() => navigate("/register")}
                    className="w-full bg-gray-400 text-white py-3 rounded-lg font-bold text-lg mt-3 hover:bg-gray-500 transition duration-300"
                >
                    회원가입
                </button>
            </form>
        </div>
    );
}
