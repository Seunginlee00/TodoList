import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { encryptPassword } from "../utils/encryptUtil";

export default function Login() {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [publicKey, setPublicKey] = useState<string>(""); // 서버에서 받아올 공개키
    const [loading, setLoading] = useState(false);
    const [errorMessage, setErrorMessage] = useState("");
    const navigate = useNavigate();

    // 첫 마운트 시 서버에서 공개키 가져오기
    useEffect(() => {
        fetch("http://localhost:8080/api/public-key")
            .then((res) => {
                if (!res.ok) throw new Error("공개키 요청 실패");
                return res.text();
            })
            .then((key) => setPublicKey(key))
            .catch((err) => {
                console.error("공개키 불러오기 실패:", err);
                setErrorMessage("로그인 서비스 이용이 불가능합니다.");
            });
    }, []);

    const handleLogin = async (e: React.FormEvent) => {
        e.preventDefault();
        setErrorMessage("");
        setLoading(true);

        try {
            if (!publicKey) throw new Error("공개키가 없습니다.");

            // 1. 비밀번호 암호화
            const encryptedPassword = encryptPassword(password, publicKey);
            if (!encryptedPassword) throw new Error("비밀번호 암호화 실패");

            // 2. 로그인 요청
            const res = await fetch("http://localhost:8080/api/user/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    userId: username,
                    password: encryptedPassword,
                }),
            });

            if (!res.ok) throw new Error("로그인 실패");

            const data = await res.json();
            localStorage.setItem("token", data.token);

            // 3. 성공 시 메인 화면 이동
            navigate("/main");
        } catch (err) {
            console.error("로그인 요청 중 오류:", err);
            setErrorMessage("아이디 또는 비밀번호가 잘못되었습니다.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="flex items-center justify-center h-screen bg-gray-100">
            <form
                onSubmit={handleLogin}
                className="bg-white p-6 rounded-lg shadow-md w-80"
            >
                <h2 className="text-2xl font-bold mb-4">로그인</h2>

                <input
                    type="text"
                    placeholder="아이디"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    className="w-full p-2 border rounded mb-3"
                />

                <input
                    type="password"
                    placeholder="비밀번호"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    className="w-full p-2 border rounded mb-3"
                />

                {errorMessage && (
                    <p className="text-red-500 text-sm mb-3">{errorMessage}</p>
                )}

                <button
                    type="submit"
                    disabled={!publicKey || loading}
                    className={`w-full text-white font-bold py-2 px-4 rounded ${
                        !publicKey || loading
                            ? "bg-gray-400 cursor-not-allowed"
                            : "bg-blue-500 hover:bg-blue-600"
                    }`}
                >
                    {loading ? "로그인 중..." : "로그인"}
                </button>
            </form>
        </div>
    );
}
