import React, { useState } from "react";
import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";

const customerLinks = [
  { to: "/dashboard", label: "Dashboard", icon: "🏠" },
  { to: "/accounts", label: "Accounts", icon: "🏦" },
  { to: "/transfer", label: "Transfer Money", icon: "💸" },
  { to: "/deposit", label: "Deposit", icon: "➕" },
  { to: "/withdraw", label: "Withdraw", icon: "➖" },
  { to: "/beneficiaries", label: "Beneficiaries", icon: "👥" },
  { to: "/transactions", label: "Transaction History", icon: "📜" },
  { to: "/profile", label: "Profile", icon: "⚙️" },
];

const adminLinks = [
  { to: "/admin/dashboard", label: "Admin Dashboard", icon: "📊" },
  { to: "/admin/customers", label: "Customer Management", icon: "👤" },
  { to: "/admin/accounts", label: "Account Management", icon: "🏦" },
  { to: "/admin/transactions", label: "Transaction Monitoring", icon: "🔍" },
];

export default function DashboardLayout({ variant = "customer" }) {
  const { user, logout, isAdmin } = useAuth();
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);

  const links = variant === "admin" ? adminLinks : customerLinks;

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  const initials = user ? `${user.username?.[0] || "U"}`.toUpperCase() : "U";

  return (
    <div className="app-shell">
      {menuOpen && <div className="sidebar-backdrop" onClick={() => setMenuOpen(false)} />}
      <aside className={`sidebar ${menuOpen ? "open" : ""}`}>
        <div className="sidebar-brand">
          <span className="logo-dot" />
          SecureBank
        </div>
        <nav>
          {links.map((link) => (
            <NavLink
              key={link.to}
              to={link.to}
              className={({ isActive }) => (isActive ? "active" : "")}
              onClick={() => setMenuOpen(false)}
            >
              <span>{link.icon}</span> {link.label}
            </NavLink>
          ))}
          {isAdmin && variant === "customer" && (
            <NavLink to="/admin/dashboard">
              <span>🛡️</span> Switch to Admin
            </NavLink>
          )}
          {isAdmin && variant === "admin" && (
            <NavLink to="/dashboard">
              <span>🏠</span> Switch to Customer View
            </NavLink>
          )}
        </nav>
      </aside>

      <div className="app-main">
        <header className="topbar">
          <div className="flex items-center gap-12">
            <button className="mobile-menu-toggle" onClick={() => setMenuOpen((v) => !v)}>
              ☰
            </button>
            <span className="title">{variant === "admin" ? "Admin Console" : "Customer Portal"}</span>
          </div>
          <div className="user-chip">
            <div className="avatar">{initials}</div>
            <div>
              <div style={{ fontWeight: 600 }}>{user?.username}</div>
              <div className="text-muted" style={{ fontSize: 12 }}>
                {user?.email}
              </div>
            </div>
            <button className="btn btn-outline btn-sm" onClick={handleLogout}>
              Logout
            </button>
          </div>
        </header>
        <main className="page-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
