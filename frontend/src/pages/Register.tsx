import React from "react";
import { Link } from "react-router-dom";

export default function RegisterPage() {
    return (
        <div className="flex justify-center items-center min-h-screen bg-gray-50">
            <div className="w-full max-w-md bg-white shadow-lg rounded-lg p-8">
                <h1 className="text-2xl font-bold text-center text-blue-600 mb-6">Register</h1>
                <p className="text-center text-gray-700 mb-4">회원가입 페이지입니다.</p>
                <Link to="/login" className="block text-center text-blue-600 hover:underline">
                    로그인 페이지로 돌아가기
                </Link>
            </div>
        </div>
    );
}