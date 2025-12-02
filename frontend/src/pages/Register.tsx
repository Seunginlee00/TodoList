import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

export default function RegisterPage() {
    const navigate = useNavigate();
    const [userId, setUserId] = useState("");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        // 회원가입 로직 추가
        console.log({ userId, password });
        alert("회원가입 기능은 아직 구현되지 않았습니다.");
        navigate("/login");
    };

    return (
        <div className="flex justify-center items-center min-h-screen bg-gray-100 font-sans">
            <div className="w-full max-w-md bg-white shadow-2xl rounded-xl p-10">
                <h1 className="text-4xl font-extrabold text-center text-gray-800 mb-8">
                    회원가입
                </h1>
                <form onSubmit={handleSubmit} className="space-y-6">
                    <div>
                        <label className="block font-bold text-gray-700 mb-2">
                            아이디
                        </label>
                        <input
                            type="text"
                            value={userId}
                            onChange={(e) => setUserId(e.target.value)}
                            placeholder="아이디를 입력하세요"
                            className="w-full px-4 py-3 border-2 border-gray-200 rounded-lg focus:outline-none focus:border-blue-500 transition duration-300"
                            required
                        />
                    </div>
                    <div>
                        <label className="block font-bold text-gray-700 mb-2">
                            비밀번호
                        </label>
                        <input
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            placeholder="비밀번호를 입력하세요"
                            className="w-full px-4 py-3 border-2 border-gray-200 rounded-lg focus:outline-none focus:border-blue-500 transition duration-300"
                            required
                        />
                    </div>
                    <div>
                        <label className="block font-bold text-gray-700 mb-2">
                            비밀번호 확인
                        </label>
                        <input
                            type="password"
                            value={confirmPassword}
                            onChange={(e) => setConfirmPassword(e.target.value)}
                            placeholder="비밀번호를 다시 입력하세요"
                            className="w-full px-4 py-3 border-2 border-gray-200 rounded-lg focus:outline-none focus:border-blue-500 transition duration-300"
                            required
                        />
                    </div>
                    <button
                        type="submit"
                        className="w-full bg-blue-600 text-white font-bold py-3 rounded-lg hover:bg-blue-700 transition duration-300"
                    >
                        가입하기
                    </button>
                </form>
                <p className="text-center text-gray-500 mt-6">
                    이미 계정이 있으신가요?{" "}
                    <Link to="/login" className="text-blue-600 hover:underline">
                        로그인
                    </Link>
                </p>
            </div>
        </div>
    );
}