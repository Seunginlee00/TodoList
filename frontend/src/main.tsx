import React from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Login from "./pages/Login";
import App from "./App";
import PrivateRoute from "./components/PrivateRoute";
import RegisterPage from "./pages/Register"; // Import the RegisterPage component
import "./index.css"; // Tailwind CSS 적용

ReactDOM.createRoot(document.getElementById("root")!).render(
    <React.StrictMode>
        <BrowserRouter>
            <Routes>
                <Route path="/login" element={<Login />} />
                <Route path="/register" element={<RegisterPage />} /> {/* Add the register route */}
                <Route
                    path="/main"
                    element={
                        <PrivateRoute>
                            <App />
                        </PrivateRoute>
                    }
                />
                <Route path="*" element={<Navigate to="/login" />} />
            </Routes>
        </BrowserRouter>
    </React.StrictMode>
);
