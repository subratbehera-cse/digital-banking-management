import React, { useEffect, useState } from "react";
import { getAdminDashboard } from "../../api/apiService";
import { formatCurrency, extractErrorMessage } from "../../utils/format";
import Alert from "../../components/common/Alert";

const StatCard = ({ label, value, accent }) => (
  <div className="card stat-card">
    <div className="text-muted" style={{ fontSize: 13, fontWeight: 600, textTransform: "uppercase" }}>
      {label}
    </div>
    <div style={{ fontSize: 26, fontWeight: 800, marginTop: 6, color: accent || "var(--text)" }}>{value}</div>
  </div>
);

export default function AdminDashboard() {
  const [stats, setStats] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        const res = await getAdminDashboard();
        setStats(res.data.data);
      } catch (err) {
        setError(extractErrorMessage(err));
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  if (loading) {
    return (
      <div className="full-page-loader" style={{ minHeight: "60vh" }}>
        <div className="spinner" />
      </div>
    );
  }

  return (
    <div>
      <h1 className="section-title">Admin Dashboard</h1>
      <p className="section-subtitle">A bird&apos;s-eye view of the entire bank&apos;s activity.</p>

      <Alert type="error" message={error} />

      {stats && (
        <div className="grid grid-4" style={{ marginBottom: 24 }}>
          <StatCard label="Total Customers" value={stats.totalCustomers} />
          <StatCard label="Total Accounts" value={stats.totalAccounts} />
          <StatCard label="Active Accounts" value={stats.activeAccounts} accent="var(--accent)" />
          <StatCard label="Inactive Accounts" value={stats.inactiveAccounts} accent="var(--warning)" />
          <StatCard label="Total Bank Balance" value={formatCurrency(stats.totalBankBalance)} accent="var(--primary)" />
          <StatCard label="Total Transactions" value={stats.totalTransactions} />
          <StatCard label="Transactions Today" value={stats.transactionsToday} accent="var(--accent)" />
        </div>
      )}
    </div>
  );
}
