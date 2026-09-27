import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getMyAccounts, createAccount } from "../api/apiService";
import { formatCurrency, maskAccountNumber, extractErrorMessage } from "../utils/format";
import StatusBadge from "../components/common/StatusBadge";
import Alert from "../components/common/Alert";

export default function Accounts() {
  const navigate = useNavigate();
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ accountType: "SAVINGS", initialDeposit: "0" });
  const [creating, setCreating] = useState(false);

  const loadAccounts = async () => {
    setLoading(true);
    try {
      const res = await getMyAccounts();
      setAccounts(res.data.data);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAccounts();
  }, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    setError("");
    setCreating(true);
    try {
      await createAccount({ accountType: form.accountType, initialDeposit: Number(form.initialDeposit || 0) });
      setShowForm(false);
      setForm({ accountType: "SAVINGS", initialDeposit: "0" });
      await loadAccounts();
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setCreating(false);
    }
  };

  return (
    <div>
      <div className="flex items-center justify-between" style={{ marginBottom: 6 }}>
        <div>
          <h1 className="section-title mb-0">My Accounts</h1>
          <p className="section-subtitle">Manage all your bank accounts in one place.</p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowForm((v) => !v)}>
          {showForm ? "Cancel" : "+ Open New Account"}
        </button>
      </div>

      <Alert type="error" message={error} />

      {showForm && (
        <div className="card" style={{ marginBottom: 20, maxWidth: 480 }}>
          <h3 style={{ marginTop: 0 }}>Open a New Account</h3>
          <form onSubmit={handleCreate}>
            <div className="form-group">
              <label>Account Type</label>
              <select
                value={form.accountType}
                onChange={(e) => setForm({ ...form, accountType: e.target.value })}
              >
                <option value="SAVINGS">Savings</option>
                <option value="CURRENT">Current</option>
              </select>
            </div>
            <div className="form-group">
              <label>Initial Deposit</label>
              <input
                type="number"
                min="0"
                step="0.01"
                value={form.initialDeposit}
                onChange={(e) => setForm({ ...form, initialDeposit: e.target.value })}
              />
            </div>
            <button className="btn btn-primary" disabled={creating}>
              {creating ? "Creating..." : "Create Account"}
            </button>
          </form>
        </div>
      )}

      {loading ? (
        <div className="full-page-loader" style={{ minHeight: "30vh" }}>
          <div className="spinner" />
        </div>
      ) : accounts.length === 0 ? (
        <div className="card empty-state">No accounts yet. Open one to get started.</div>
      ) : (
        <div className="grid grid-3">
          {accounts.map((acc) => (
            <div key={acc.id} className="list-account-card" onClick={() => navigate(`/accounts/${acc.id}`)}>
              <div className="flex items-center justify-between" style={{ marginBottom: 10 }}>
                <span className="text-muted" style={{ fontSize: 13, fontWeight: 600 }}>
                  {acc.accountType}
                </span>
                <StatusBadge value={acc.status} />
              </div>
              <div style={{ fontSize: 22, fontWeight: 800 }}>{formatCurrency(acc.balance)}</div>
              <div className="text-muted" style={{ fontSize: 13.5, marginTop: 4 }}>
                {maskAccountNumber(acc.accountNumber)}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
