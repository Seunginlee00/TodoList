"use client";
import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useState, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { encryptPassword } from "@/utils/encryptUtil";
import { API_SERVER_HOST } from "@/api/hostApi";
import { usePublicKey } from "@/utils/usePublicKey";
export default function RegisterPage() {
    const navigate = useNavigate();
    const { publicKey, jwtToken } = usePublicKey();
    // 상태
    const [loginId, setLoginId] = useState("");
    const [passwd, setPasswd] = useState("");
    const [passwdConfirm, setPasswdConfirm] = useState("");
    const [userNm, setUserNm] = useState("");
    const [loading, setLoading] = useState(false);
    const [fieldErrors, setFieldErrors] = useState({});
    const [errorMessage, setErrorMessage] = useState("");
    // 폼 유효성 검사
    const isFormValid = useMemo(() => {
        return (loginId.trim() &&
            passwd &&
            passwdConfirm &&
            userNm.trim() &&
            !Object.values(fieldErrors).some((error) => error));
    }, [loginId, passwd, passwdConfirm, userNm, fieldErrors]);
    const validateForm = () => {
        const errors = {};
        if (!loginId.trim())
            errors.loginId = "아이디를 입력하세요.";
        if (!passwd)
            errors.passwd = "비밀번호를 입력하세요.";
        if (!passwdConfirm)
            errors.passwdConfirm = "비밀번호 확인을 입력하세요.";
        if (passwd !== passwdConfirm) {
            errors.passwdConfirm = "비밀번호가 일치하지 않습니다.";
        }
        if (!userNm.trim())
            errors.userNm = "이름을 입력하세요.";
        setFieldErrors(errors);
        return Object.keys(errors).length === 0;
    };
    // 회원가입 처리
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
            // ✅ RSA 암호화 적용
            const encryptedPw = encryptPassword(passwd, publicKey);
            if (!encryptedPw) {
                setErrorMessage("비밀번호 암호화에 실패했습니다.");
                return;
            }
            const payload = {
                loginId,
                passwd: encryptedPw, // ✅ 암호화된 비밀번호 전송
                userNm,
                token: jwtToken, // JWT 토큰 추가
            };
            console.log("🔐 회원가입 요청:", { loginId, userNm, passwd: "***암호화됨***", token: jwtToken ? "있음" : "없음" });
            const res = await axios.post(`${API_SERVER_HOST}/api/user/register`, payload);
            const data = res.data;
            console.log("✅ 회원가입 응답:", data);
            if (data?.success) {
                alert("회원가입이 완료되었습니다. 로그인해주세요.");
                navigate("/login");
            }
            else {
                setErrorMessage(data?.message || "회원가입에 실패했습니다.");
            }
        }
        catch (err) {
            console.error("🚨 회원가입 오류:", err);
            const errorMsg = err.response?.data?.message || "네트워크 오류가 발생했습니다.";
            setErrorMessage(errorMsg);
        }
        finally {
            setLoading(false);
        }
    };
    return (_jsx("div", { className: "flex justify-center items-center min-h-screen bg-gray-50", children: _jsxs("form", { onSubmit: handleSubmit, className: "w-full max-w-md bg-white shadow-lg rounded-lg p-8", children: [_jsx("h1", { className: "text-2xl font-bold text-center text-blue-600 mb-6", children: "\uD68C\uC6D0\uAC00\uC785" }), _jsxs("label", { className: "block font-semibold mb-1", children: [_jsx("span", { className: "text-red-500", children: "*" }), " \uC544\uC774\uB514"] }), _jsx("input", { type: "text", value: loginId, onChange: (e) => {
                        setLoginId(e.target.value);
                        setFieldErrors((prev) => ({ ...prev, loginId: "" }));
                    }, placeholder: "\uC544\uC774\uB514\uB97C \uC785\uB825\uD558\uC138\uC694", className: `w-full border rounded px-3 py-2 mb-2 ${fieldErrors.loginId ? "border-red-500" : "border-gray-300"}` }), fieldErrors.loginId && (_jsx("p", { className: "text-red-500 text-sm mb-4", children: fieldErrors.loginId })), _jsxs("label", { className: "block font-semibold mb-1", children: [_jsx("span", { className: "text-red-500", children: "*" }), " \uC774\uB984"] }), _jsx("input", { type: "text", value: userNm, onChange: (e) => {
                        setUserNm(e.target.value);
                        setFieldErrors((prev) => ({ ...prev, userNm: "" }));
                    }, placeholder: "\uC774\uB984\uC744 \uC785\uB825\uD558\uC138\uC694", className: `w-full border rounded px-3 py-2 mb-2 ${fieldErrors.userNm ? "border-red-500" : "border-gray-300"}` }), fieldErrors.userNm && (_jsx("p", { className: "text-red-500 text-sm mb-4", children: fieldErrors.userNm })), _jsxs("label", { className: "block font-semibold mb-1", children: [_jsx("span", { className: "text-red-500", children: "*" }), " \uBE44\uBC00\uBC88\uD638"] }), _jsx("input", { type: "password", value: passwd, onChange: (e) => {
                        setPasswd(e.target.value);
                        setFieldErrors((prev) => ({ ...prev, passwd: "" }));
                    }, placeholder: "\uBE44\uBC00\uBC88\uD638\uB97C \uC785\uB825\uD558\uC138\uC694", className: `w-full border rounded px-3 py-2 mb-2 ${fieldErrors.passwd ? "border-red-500" : "border-gray-300"}` }), fieldErrors.passwd && (_jsx("p", { className: "text-red-500 text-sm mb-4", children: fieldErrors.passwd })), _jsxs("label", { className: "block font-semibold mb-1", children: [_jsx("span", { className: "text-red-500", children: "*" }), " \uBE44\uBC00\uBC88\uD638 \uD655\uC778"] }), _jsx("input", { type: "password", value: passwdConfirm, onChange: (e) => {
                        setPasswdConfirm(e.target.value);
                        setFieldErrors((prev) => ({ ...prev, passwdConfirm: "" }));
                    }, placeholder: "\uBE44\uBC00\uBC88\uD638\uB97C \uB2E4\uC2DC \uC785\uB825\uD558\uC138\uC694", className: `w-full border rounded px-3 py-2 mb-2 ${fieldErrors.passwdConfirm ? "border-red-500" : "border-gray-300"}` }), fieldErrors.passwdConfirm && (_jsx("p", { className: "text-red-500 text-sm mb-4", children: fieldErrors.passwdConfirm })), errorMessage && (_jsx("div", { className: "bg-red-100 text-red-600 text-sm p-2 rounded mb-4", children: errorMessage })), _jsx("button", { type: "submit", disabled: loading || !isFormValid, className: "w-full bg-blue-600 text-white py-2 rounded-lg font-semibold disabled:opacity-50 mb-2", children: loading ? "회원가입 중..." : "회원가입" }), _jsx("button", { type: "button", onClick: () => navigate("/login"), className: "w-full bg-gray-300 text-gray-700 py-2 rounded-lg font-semibold", children: "\uB85C\uADF8\uC778 \uD398\uC774\uC9C0\uB85C" })] }) }));
}
