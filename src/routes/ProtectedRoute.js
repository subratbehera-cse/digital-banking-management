import React from "react";
import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

/**
 * Guards a route (or nested routes via <Outlet/>) so that only authenticated
 * users can reach it. Optionally restrict further to a specific role, e.g.
 * <ProtectedRoute requireAdmin /> for admin-only sections.
 */
export default function ProtectedRoute({ requireAdmin = false }) {
  const { user, loading, isAdmin } = useAuth();

  if (loading) {
    return (
      <div className="full-page-loader">
        <div className="spinner" />
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (requireAdmin && !isAdmin) {
    return <Navigate to="/dashboard" replace />;
  }

  return <Outlet />;
}
