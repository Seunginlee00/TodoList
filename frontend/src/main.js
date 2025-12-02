import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import React from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Login from "./pages/Login";
import App from "./App";
import PrivateRoute from "./components/PrivateRoute";
ReactDOM.createRoot(document.getElementById("root")).render(_jsx(React.StrictMode, { children: _jsx(BrowserRouter, { children: _jsxs(Routes, { children: [_jsx(Route, { path: "/login", element: _jsx(Login, {}) }), _jsx(Route, { path: "/main", element: _jsx(PrivateRoute, { children: _jsx(App, {}) }) }), _jsx(Route, { path: "*", element: _jsx(Navigate, { to: "/login" }) })] }) }) }));
