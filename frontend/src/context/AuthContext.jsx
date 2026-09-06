import { createContext, useContext, useEffect, useState } from "react";
import { loginUser } from "../api/authApi";

const AuthContext = createContext(null);

/**
 * Holds the current authentication state (JWT + user info) and exposes
 * login()/logout() helpers. Backed by localStorage so a page refresh
 * doesn't log the user out.
 */
export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem("bms_user");
    return stored ? JSON.parse(stored) : null;
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (user) {
      localStorage.setItem("bms_user", JSON.stringify(user));
    } else {
      localStorage.removeItem("bms_user");
    }
  }, [user]);

  const login = async (email, password) => {
    setLoading(true);
    setError(null);
    try {
      const response = await loginUser({ email, password });
      const data = response.data;
      localStorage.setItem("bms_token", data.token);
      const userInfo = {
        userId: data.userId,
        fullName: data.fullName,
        email: data.email,
        role: data.role,
      };
      setUser(userInfo);
      return userInfo;
    } catch (err) {
      const message =
        err.response?.data?.message || "Login failed. Please check your credentials.";
      setError(message);
      throw new Error(message);
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    localStorage.removeItem("bms_token");
    localStorage.removeItem("bms_user");
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, logout, loading, error, isAdmin: user?.role === "ADMIN" }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return ctx;
}
