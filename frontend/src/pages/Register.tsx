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
        <div
            className="flex flex-row justify-center items-center min-h-screen bg-gradient-to-br from-blue-50 via-indigo-50 to-purple-50 p-4"
            style={{ display: 'flex', flexDirection: 'row', justifyContent: 'center', alignItems: 'center', minHeight: '100vh' }}
        >
            <div
                className="max-w-md bg-white/80 backdrop-blur-lg shadow-2xl rounded-2xl p-10 border border-white/20"
                style={{ width: '100%', maxWidth: '448px' }}
            >
                <div className="text-center mb-8">
                    <h1 className="text-4xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent mb-2">
                        회원가입
                    </h1>
                    <p className="text-gray-500 text-sm">새로운 계정을 만들어보세요</p>
                </div>

                <form onSubmit={handleSubmit} className="space-y-5 flex flex-col items-center">
                    <div className="w-full max-w-xs">
                        <label className="block text-sm font-medium text-gray-700 mb-2 text-center">
                            아이디
                        </label>
                        <input
                            type="text"
                            value={userId}
                            onChange={(e) => setUserId(e.target.value)}
                            placeholder="아이디를 입력하세요"
                            className="w-full border border-gray-200 rounded-xl px-4 py-3 text-gray-800 text-center placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-300 focus:border-transparent transition duration-200"
                            required
                        />
                    </div>
                    <div className="w-full max-w-xs">
                        <label className="block text-sm font-medium text-gray-700 mb-2 text-center">
                            비밀번호
                        </label>
                        <input
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            placeholder="비밀번호를 입력하세요"
                            className="w-full border border-gray-200 rounded-xl px-4 py-3 text-gray-800 text-center placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-300 focus:border-transparent transition duration-200"
                            required
                        />
                    </div>
                    <div className="w-full max-w-xs">
                        <label className="block text-sm font-medium text-gray-700 mb-2 text-center">
                            비밀번호 확인
                        </label>
                        <input
                            type="password"
                            value={confirmPassword}
                            onChange={(e) => setConfirmPassword(e.target.value)}
                            placeholder="비밀번호를 다시 입력하세요"
                            className="w-full border border-gray-200 rounded-xl px-4 py-3 text-gray-800 text-center placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-300 focus:border-transparent transition duration-200"
                            required
                        />
                    </div>
                    <button
                        type="submit"
                        className="w-full max-w-xs bg-gradient-to-r from-blue-600 to-purple-600 text-white py-3 rounded-xl font-semibold text-base hover:from-blue-700 hover:to-purple-700 transform hover:scale-[1.02] transition-all duration-200 shadow-lg hover:shadow-xl"
                    >
                        가입하기
                    </button>
                </form>

                <div className="mt-6 text-center">
                    <p className="text-gray-600 text-sm">
                        이미 계정이 있으신가요?{" "}
                        <Link
                            to="/login"
                            className="text-blue-600 hover:text-purple-600 font-semibold transition-colors duration-200"
                        >
                            로그인
                        </Link>
                    </p>
                </div>
            </div>
        </div>
    );
}