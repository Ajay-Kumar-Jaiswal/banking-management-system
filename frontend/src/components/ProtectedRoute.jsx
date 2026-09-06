import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

/** Redirects to /login if there is no authenticated user. */
export default function ProtectedRoute({ children }) {
  const { user } = useAuth();
  if (!user) {
    return <Navigate to="/login" replace />;
  }
  return children;
}
