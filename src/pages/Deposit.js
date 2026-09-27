import React, { useEffect, useState } from "react";
import { getMyAccounts, depositMoney } from "../api/apiService";
import { formatCurrency, extractErrorMessage } from "../utils/format";
import Alert from "../components/common/Alert";

export default function Deposit() {
  const [accounts, setAccounts] = useState([]);
  const [form, setForm] = useState({ accountId: "", amount: "", description: "" });
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    (async () => {
      try {
        const res = await getMyAccounts();
        setAccounts(res.data.data);
        if (res.data.data.length > 0) {
          setForm((f) => ({ ...f, accountId: res.data.data[0].id }));
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
      const res = await depositMoney({
        accountId: Number(form.accountId),
        amount: Number(form.amount),
        description: form.description,
      });
      setSuccess(
        `Deposit successful! Reference: ${res.data.data.transactionRef}. New balance: ${formatCurrency(
          res.data.data.balanceAfter
        )}`
      );
      setForm({ ...form, amount: "", description: "" });
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div>
      <h1 className="section-title">Deposit Funds</h1>
      <p className="section-subtitle">Add money to one of your accounts.</p>

      <div className="card" style={{ maxWidth: 480 }}>
        <Alert type="error" message={error} />
        <Alert type="success" message={success} />

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Account</label>
            <select name="accountId" value={form.accountId} onChange={handleChange} required>
              {accounts.map((a) => (
                <option key={a.id} value={a.id}>
                  {a.accountType} · {a.accountNumber} ({formatCurrency(a.balance)})
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label>Amount</label>
            <input type="number" name="amount" min="0.01" step="0.01" value={form.amount} onChange={handleChange} required />
          </div>

          <div className="form-group">
            <label>Description (optional)</label>
            <input name="description" value={form.description} onChange={handleChange} placeholder="e.g. Salary credit" />
          </div>

          <button className="btn btn-accent btn-block" disabled={submitting || accounts.length === 0}>
            {submitting ? "Processing..." : "Deposit"}
          </button>
        </form>
      </div>
    </div>
  );
}
