import React from "react";
import { Link } from "react-router-dom";

export default function NotFound() {
  return (
    <div className="full-page-loader" style={{ flexDirection: "column", gap: 16 }}>
      <h1 style={{ fontSize: 48, margin: 0 }}>404</h1>
      <p className="text-muted">The page you're looking for doesn't exist.</p>
      <Link className="btn btn-primary" to="/">
        Go Home
      </Link>
    </div>
  );
}
