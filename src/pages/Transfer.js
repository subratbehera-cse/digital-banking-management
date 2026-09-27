import React, { useEffect, useState } from "react";
import { getMyAccounts, getMyBeneficiaries, transferMoney } from "../api/apiService";
import { formatCurrency, extractErrorMessage } from "../utils/format";
import Alert from "../components/common/Alert";

export default function Transfer() {
  const [accounts, setAccounts] = useState([]);
  const [beneficiaries, setBeneficiaries] = useState([]);
  const [form, setForm] = useState({ fromAccountId: "", toAccountNumber: "", amount: "", description: "" });
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    (async () => {
      try {
        const [accRes, benRes] = await Promise.all([getMyAccounts(), getMyBeneficiaries()]);
        setAccounts(accRes.data.data);
        setBeneficiaries(benRes.data.data);
        if (accRes.data.data.length > 0) {
          setForm((f) => ({ ...f, fromAccountId: accRes.data.data[0].id }));
        }
      } catch (err) {
        setError(extractErrorMessage(err));
      }
    })();
  }, []);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");
    setSubmitting(true);
    try {
      const res = await transferMoney({
        fromAccountId: Number(form.fromAccountId),
        toAccountNumber: form.toAccountNumber.trim(),
        amount: Number(form.amount),
        description: form.description,
      });
      setSuccess(
        `Transfer successful! Reference: ${res.data.data.transactionRef}. New balance: ${formatCurrency(
          res.data.data.balanceAfter
        )}`
      );
      setForm({ ...form, toAccountNumber: "", amount: "", description: "" });
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div>
      <h1 className="section-title">Transfer Money</h1>
      <p className="section-subtitle">Send money to another SecureBank account instantly.</p>

      <div className="grid grid-2">
        <div className="card">
          <Alert type="error" message={error} />
          <Alert type="success" message={success} />

          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label>From Account</label>
              <select name="fromAccountId" value={form.fromAccountId} onChange={handleChange} required>
                {accounts.map((a) => (
                  <option key={a.id} value={a.id}>
                    {a.accountType} · {a.accountNumber} ({formatCurrency(a.balance)})
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label>To Account Number</label>
              <input
                name="toAccountNumber"
                value={form.toAccountNumber}
                onChange={handleChange}
                placeholder="Enter beneficiary account number"
                required
              />
            </div>

            <div className="form-group">
              <label>Amount</label>
              <input
                type="number"
                name="amount"
                min="0.01"
                step="0.01"
                value={form.amount}
                onChange={handleChange}
                required
              />
            </div>

            <div className="form-group">
              <label>Description (optional)</label>
              <input name="description" value={form.description} onChange={handleChange} placeholder="e.g. Rent payment" />
            </div>

            <button className="btn btn-primary btn-block" disabled={submitting || accounts.length === 0}>
              {submitting ? "Processing..." : "Transfer Now"}
            </button>
          </form>
        </div>

        <div className="card">
          <h3 style={{ marginTop: 0 }}>Saved Beneficiaries</h3>
          {beneficiaries.length === 0 ? (
            <p className="text-muted">You haven&apos;t added any beneficiaries yet.</p>
          ) : (
            <div className="beneficiary-list">
              {beneficiaries.map((b) => (
                <div
                  key={b.id}
                  className="beneficiary-item"
                  style={{ cursor: "pointer" }}
                  onClick={() => setForm((f) => ({ ...f, toAccountNumber: b.beneficiaryAccountNumber }))}
                >
                  <div>
                    <div style={{ fontWeight: 600 }}>{b.nickname || b.beneficiaryName}</div>
                    <div className="text-muted" style={{ fontSize: 13 }}>
                      {b.beneficiaryAccountNumber}
                    </div>
                  </div>
                  <span className="badge badge-info">Use</span>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
