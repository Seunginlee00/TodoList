"use client";
import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
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
    const [fieldErrors, setFieldErrors] = useState({});
    const [errorMessage, setErrorMessage] = useState("");
    // 폼 유효성 검사
    const isFormValid = useMemo(() => {
        return userId.trim() && pw && !fieldErrors.userId && !fieldErrors.pw;
    }, [userId, pw, fieldErrors]);
    const validateForm = () => {
        const errors = {};
        if (!userId.trim())
            errors.userId = "아이디를 입력하세요.";
        if (!pw)
            errors.pw = "비밀번호를 입력하세요.";
        setFieldErrors(errors);
        return Object.keys(errors).length === 0;
    };
    // 로그인 처리
    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrorMessage("");
        if (!validateForm())
            return;
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
            // JWT-RSA 방식: token 포함
            const payload = {
                userId,
                password: encryptedPw,
                token: jwtToken, // JWT 토큰 추가
            };
            console.log("🔐 로그인 요청:", { userId, password: "***암호화됨***", token: jwtToken ? "있음" : "없음" });
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
            }
            else {
                setErrorMessage("로그인 실패: 아이디 또는 비밀번호를 확인하세요.");
            }
        }
        catch (err) {
            console.error("🚨 로그인 오류:", err);
            setErrorMessage("네트워크 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
        finally {
            setLoading(false);
        }
    };
    return (_jsx("div", { className: "flex justify-center items-center min-h-screen bg-gray-50", children: _jsxs("form", { onSubmit: handleSubmit, className: "w-full max-w-md bg-white shadow-lg rounded-lg p-8", children: [_jsx("h1", { className: "text-2xl font-bold text-center text-blue-600 mb-6", children: "Login" }), _jsxs("label", { className: "block font-semibold mb-1", children: [_jsx("span", { className: "text-red-500", children: "*" }), " UserID"] }), _jsx("input", { type: "text", value: userId, onChange: (e) => {
                        setUserId(e.target.value);
                        setFieldErrors((prev) => ({ ...prev, userId: "" }));
                    }, placeholder: "\uC544\uC774\uB514\uB97C \uC785\uB825\uD558\uC138\uC694", className: `w-full border rounded px-3 py-2 mb-2 ${fieldErrors.userId ? "border-red-500" : "border-gray-300"}` }), fieldErrors.userId && (_jsx("p", { className: "text-red-500 text-sm mb-4", children: fieldErrors.userId })), _jsxs("label", { className: "block font-semibold mb-1", children: [_jsx("span", { className: "text-red-500", children: "*" }), " Password"] }), _jsx("input", { type: "password", value: pw, onChange: (e) => {
                        setPw(e.target.value);
                        setFieldErrors((prev) => ({ ...prev, pw: "" }));
                    }, placeholder: "\uBE44\uBC00\uBC88\uD638\uB97C \uC785\uB825\uD558\uC138\uC694", className: `w-full border rounded px-3 py-2 mb-2 ${fieldErrors.pw ? "border-red-500" : "border-gray-300"}` }), fieldErrors.pw && (_jsx("p", { className: "text-red-500 text-sm mb-4", children: fieldErrors.pw })), errorMessage && (_jsx("div", { className: "bg-red-100 text-red-600 text-sm p-2 rounded mb-4", children: errorMessage })), _jsx("button", { type: "submit", disabled: loading || !isFormValid, className: "w-full bg-blue-600 text-white py-2 rounded-lg font-semibold disabled:opacity-50 mb-2", children: loading ? "로그인 중..." : "로그인" }), _jsx("button", { type: "button", onClick: () => navigate("/register"), className: "w-full bg-gray-300 text-gray-700 py-2 rounded-lg font-semibold", children: "\uD68C\uC6D0\uAC00\uC785 \uD398\uC774\uC9C0\uB85C" })] }) }));
}
