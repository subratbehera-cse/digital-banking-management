import React, { createContext, useContext, useEffect, useState, useCallback } from "react";
import { loginUser, registerUser, getMyProfile } from "../api/apiService";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  const loadStoredSession = useCallback(() => {
    const token = localStorage.getItem("token");
    const storedUser = localStorage.getItem("user");
    if (token && storedUser) {
      try {
        setUser(JSON.parse(storedUser));
      } catch {
        localStorage.removeItem("user");
      }
    }
    setLoading(false);
  }, []);

  useEffect(() => {
    loadStoredSession();
  }, [loadStoredSession]);

  const login = async (username, password) => {
    const response = await loginUser({ username, password });
    const data = response.data.data;
    localStorage.setItem("token", data.token);
    localStorage.setItem("user", JSON.stringify(data));
    setUser(data);
    return data;
  };

  const register = async (payload) => {
    const response = await registerUser(payload);
    return response.data.data;
  };

  const logout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    setUser(null);
  };

  const refreshProfile = async () => {
    const response = await getMyProfile();
    const profile = response.data.data;
    const merged = { ...user, ...profile };
    localStorage.setItem("user", JSON.stringify(merged));
    setUser(merged);
    return merged;
  };

  const isAdmin = !!user && Array.isArray(user.roles) && user.roles.includes("ROLE_ADMIN");

  const value = { user, loading, login, register, logout, refreshProfile, isAdmin };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return ctx;
}
