"use client";

import { useState, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { encryptPassword } from "@/utils/encryptUtil"; // 🚨 이 함수는 URL-Safe Base64를 사용하도록 수정 필요!
import { API_SERVER_HOST } from "@/api/hostApi";
import { usePublicKey } from "@/utils/usePublicKey";

export default function RegisterPage() {
    const navigate = useNavigate();
    // ✅ 일회용 RSA 키와 JWT 토큰을 함께 가져옴
    const { publicKey, jwtToken } = usePublicKey(); 

    // 상태
    const [loginId, setLoginId] = useState("");
    const [passwd, setPasswd] = useState("");
    const [passwdConfirm, setPasswdConfirm] = useState("");
    const [userNm, setUserNm] = useState("");
    const [loading, setLoading] = useState(false);
    const [fieldErrors, setFieldErrors] = useState<{
        loginId?: string;
        passwd?: string;
        passwdConfirm?: string;
        userNm?: string;
    }>({});
    const [errorMessage, setErrorMessage] = useState("");

    // 폼 유효성 검사
    const isFormValid = useMemo(() => {
        return (
            loginId.trim() &&
            passwd &&
            passwdConfirm &&
            userNm.trim() &&
            !Object.values(fieldErrors).some((error) => error)
        );
    }, [loginId, passwd, passwdConfirm, userNm, fieldErrors]);

    const validateForm = (): boolean => {
        const errors: {
            loginId?: string;
            passwd?: string;
            passwdConfirm?: string;
            userNm?: string;
        } = {};

        if (!loginId.trim()) errors.loginId = "아이디를 입력하세요.";
        if (!passwd) errors.passwd = "비밀번호를 입력하세요.";
        if (!passwdConfirm) errors.passwdConfirm = "비밀번호 확인을 입력하세요.";
        if (passwd !== passwdConfirm) {
            errors.passwdConfirm = "비밀번호가 일치하지 않습니다.";
        }
        if (!userNm.trim()) errors.userNm = "이름을 입력하세요.";

        setFieldErrors(errors);
        return Object.keys(errors).length === 0;
    };

    // 회원가입 처리
    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setErrorMessage("");

        if (!validateForm()) return;
        
        // 🌟 [수정된 체크]: Public Key와 JWT 토큰 모두 확인
        if (!publicKey || !jwtToken) { 
            setErrorMessage("보안 키 정보를 불러올 수 없습니다. 다시 시도해주세요.");
            return;
        }

        setLoading(true);
        try {
            // ✅ RSA 암호화 적용
            // 🚨 encryptPassword 내부에서 URL-Safe Base64를 사용하는지 반드시 확인해야 합니다!
            const encryptedPw = encryptPassword(passwd, publicKey);
            if (!encryptedPw) {
                setErrorMessage("비밀번호 암호화에 실패했습니다.");
                return;
            }

            // 🌟 [수정된 Payload]: JWT 토큰과 암호화된 비밀번호를 분리하여 전송
            const payload = {
                loginId,
                userNm,
                // 백엔드 UsersRequest DTO에 맞춰 필드 이름 변경
                encryptedData: encryptedPw, 
                token: jwtToken, // ⬅️ 일회용 키를 식별하는 JWT 토큰
            };

            console.log("🔐 회원가입 요청:", { loginId, userNm, encryptedData: "***암호화됨***", token: jwtToken ? "있음" : "없음" });
            
            const res = await axios.post(`${API_SERVER_HOST}/api/user/register`, payload);
            const data = res.data;
            console.log("✅ 회원가입 응답:", data);

            if (data?.success) {
                alert("회원가입이 완료되었습니다. 로그인해주세요.");
                navigate("/login");
            } else {
                setErrorMessage(data?.message || "회원가입에 실패했습니다.");
            }
        } catch (err: unknown) {
            console.error("🚨 회원가입 오류:", err);
                const errorMsg =
                axios.isAxiosError(err) && err.response?.data?.message
                ? err.response.data.message
                : "네트워크 오류가 발생했습니다.";
            setErrorMessage(errorMsg);
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
                <h1 className="text-2xl font-bold text-center text-blue-600 mb-6">
                    회원가입
                </h1>

                {/* LoginID */}
                <label className="block font-semibold mb-1">
                    <span className="text-red-500">*</span> 아이디
                </label>
                <input
                    type="text"
                    value={loginId}
                    onChange={(e) => {
                        setLoginId(e.target.value);
                        setFieldErrors((prev) => ({ ...prev, loginId: "" }));
                    }}
                    placeholder="아이디를 입력하세요"
                    className={`w-full border rounded px-3 py-2 mb-2 ${
                        fieldErrors.loginId ? "border-red-500" : "border-gray-300"
                    }`}
                />
                {fieldErrors.loginId && (
                    <p className="text-red-500 text-sm mb-4">{fieldErrors.loginId}</p>
                )}

                {/* UserName */}
                <label className="block font-semibold mb-1">
                    <span className="text-red-500">*</span> 이름
                </label>
                <input
                    type="text"
                    value={userNm}
                    onChange={(e) => {
                        setUserNm(e.target.value);
                        setFieldErrors((prev) => ({ ...prev, userNm: "" }));
                    }}
                    placeholder="이름을 입력하세요"
                    className={`w-full border rounded px-3 py-2 mb-2 ${
                        fieldErrors.userNm ? "border-red-500" : "border-gray-300"
                    }`}
                />
                {fieldErrors.userNm && (
                    <p className="text-red-500 text-sm mb-4">{fieldErrors.userNm}</p>
                )}

                {/* Password */}
                <label className="block font-semibold mb-1">
                    <span className="text-red-500">*</span> 비밀번호
                </label>
                <input
                    type="password"
                    value={passwd}
                    onChange={(e) => {
                        setPasswd(e.target.value);
                        setFieldErrors((prev) => ({ ...prev, passwd: "" }));
                    }}
                    placeholder="비밀번호를 입력하세요"
                    className={`w-full border rounded px-3 py-2 mb-2 ${
                        fieldErrors.passwd ? "border-red-500" : "border-gray-300"
                    }`}
                />
                {fieldErrors.passwd && (
                    <p className="text-red-500 text-sm mb-4">{fieldErrors.passwd}</p>
                )}

                {/* Password Confirm */}
                <label className="block font-semibold mb-1">
                    <span className="text-red-500">*</span> 비밀번호 확인
                </label>
                <input
                    type="password"
                    value={passwdConfirm}
                    onChange={(e) => {
                        setPasswdConfirm(e.target.value);
                        setFieldErrors((prev) => ({ ...prev, passwdConfirm: "" }));
                    }}
                    placeholder="비밀번호를 다시 입력하세요"
                    className={`w-full border rounded px-3 py-2 mb-2 ${
                        fieldErrors.passwdConfirm ? "border-red-500" : "border-gray-300"
                    }`}
                />
                {fieldErrors.passwdConfirm && (
                    <p className="text-red-500 text-sm mb-4">
                        {fieldErrors.passwdConfirm}
                    </p>
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
                    className="w-full bg-blue-600 text-white py-2 rounded-lg font-semibold disabled:opacity-50 mb-2"
                >
                    {loading ? "회원가입 중..." : "회원가입"}
                </button>

                <button
                    type="button"
                    onClick={() => navigate("/login")}
                    className="w-full bg-gray-300 text-gray-700 py-2 rounded-lg font-semibold"
                >
                    로그인 페이지로
                </button>
            </form>
        </div>
    );
}