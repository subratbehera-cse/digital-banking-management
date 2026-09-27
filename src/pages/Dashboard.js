import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getMyAccounts, getMyTransactions } from "../api/apiService";
import { formatCurrency, formatDate, maskAccountNumber, extractErrorMessage } from "../utils/format";
import StatusBadge from "../components/common/StatusBadge";
import Alert from "../components/common/Alert";
import { useAuth } from "../context/AuthContext";

const quickActions = [
  { to: "/transfer", icon: "💸", label: "Transfer" },
  { to: "/deposit", icon: "➕", label: "Deposit" },
  { to: "/withdraw", icon: "➖", label: "Withdraw" },
  { to: "/beneficiaries", icon: "👥", label: "Beneficiaries" },
];

export default function Dashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [accounts, setAccounts] = useState([]);
  const [transactions, setTransactions] = useState([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        const [accRes, txnRes] = await Promise.all([getMyAccounts(), getMyTransactions(0, 5)]);
        setAccounts(accRes.data.data);
        setTransactions(txnRes.data.data.content);
      } catch (err) {
        setError(extractErrorMessage(err));
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const totalBalance = accounts.reduce((sum, a) => sum + Number(a.balance), 0);
  const primaryAccount = accounts[0];

  if (loading) {
    return (
      <div className="full-page-loader" style={{ minHeight: "60vh" }}>
        <div className="spinner" />
      </div>
    );
  }

  return (
    <div>
      <h1 className="section-title">Welcome back, {user?.username}</h1>
      <p className="section-subtitle">Here&apos;s an overview of your finances today.</p>

      <Alert type="error" message={error} />

      {accounts.length === 0 ? (
        <div className="card empty-state">
          <p>You don&apos;t have any bank accounts yet.</p>
          <button className="btn btn-primary" onClick={() => navigate("/accounts")}>
            Open Your First Account
          </button>
        </div>
      ) : (
        <>
          <div className="grid grid-2" style={{ marginBottom: 20 }}>
            <div className="balance-hero">
              <div className="label">Total Balance Across Accounts</div>
              <div className="amount">{formatCurrency(totalBalance)}</div>
              <div className="account-no">{accounts.length} active account(s)</div>
            </div>
            {primaryAccount && (
              <div className="card">
                <div className="text-muted" style={{ fontSize: 13, fontWeight: 600, textTransform: "uppercase" }}>
                  Primary Account
                </div>
                <div style={{ fontSize: 22, fontWeight: 800, margin: "8px 0 4px" }}>
                  {formatCurrency(primaryAccount.balance)}
                </div>
                <div className="text-muted" style={{ fontSize: 13.5, marginBottom: 10 }}>
                  {maskAccountNumber(primaryAccount.accountNumber)} · {primaryAccount.accountType}
                </div>
                <StatusBadge value={primaryAccount.status} />
              </div>
            )}
          </div>

          <h3 style={{ marginBottom: 12 }}>Quick Actions</h3>
          <div className="quick-actions" style={{ marginBottom: 24 }}>
            {quickActions.map((qa) => (
              <div key={qa.to} className="quick-action" onClick={() => navigate(qa.to)}>
                <div className="icon">{qa.icon}</div>
                <div className="label">{qa.label}</div>
              </div>
            ))}
          </div>

          <div className="card">
            <div className="card-header">
              <h3>Recent Transactions</h3>
              <button className="btn btn-outline btn-sm" onClick={() => navigate("/transactions")}>
                View All
              </button>
            </div>
            {transactions.length === 0 ? (
              <div className="empty-state">No transactions yet.</div>
            ) : (
              <div className="table-wrapper">
                <table>
                  <thead>
                    <tr>
                      <th>Reference</th>
                      <th>Type</th>
                      <th>Amount</th>
                      <th>Status</th>
                      <th>Date</th>
                    </tr>
                  </thead>
                  <tbody>
                    {transactions.map((t) => (
                      <tr key={t.id}>
                        <td>{t.transactionRef}</td>
                        <td>
                          <StatusBadge value={t.type} />
                        </td>
                        <td>{formatCurrency(t.amount)}</td>
                        <td>
                          <StatusBadge value={t.status} />
                        </td>
                        <td>{formatDate(t.createdAt)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </>
      )}
    </div>
  );
}
