import React, { useEffect, useState } from "react";
import { getMyBeneficiaries, addBeneficiary, deleteBeneficiary } from "../api/apiService";
import { extractErrorMessage } from "../utils/format";
import Alert from "../components/common/Alert";

const initialForm = { beneficiaryName: "", beneficiaryAccountNumber: "", bankName: "", nickname: "" };

export default function Beneficiaries() {
  const [beneficiaries, setBeneficiaries] = useState([]);
  const [form, setForm] = useState(initialForm);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  const load = async () => {
    setLoading(true);
    try {
      const res = await getMyBeneficiaries();
      setBeneficiaries(res.data.data);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");
    setSubmitting(true);
    try {
      await addBeneficiary(form);
      setSuccess("Beneficiary added successfully.");
      setForm(initialForm);
      await load();
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Remove this beneficiary?")) return;
    try {
      await deleteBeneficiary(id);
      await load();
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  };

  return (
    <div>
      <h1 className="section-title">Beneficiaries</h1>
      <p className="section-subtitle">Save frequently used accounts for faster transfers.</p>

      <div className="grid grid-2">
        <div className="card">
          <h3 style={{ marginTop: 0 }}>Add New Beneficiary</h3>
          <Alert type="error" message={error} />
          <Alert type="success" message={success} />
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label>Beneficiary Name</label>
              <input name="beneficiaryName" value={form.beneficiaryName} onChange={handleChange} required />
            </div>
            <div className="form-group">
              <label>Account Number</label>
              <input
                name="beneficiaryAccountNumber"
                value={form.beneficiaryAccountNumber}
                onChange={handleChange}
                required
              />
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>Bank Name (optional)</label>
                <input name="bankName" value={form.bankName} onChange={handleChange} placeholder="SecureBank" />
              </div>
              <div className="form-group">
                <label>Nickname (optional)</label>
                <input name="nickname" value={form.nickname} onChange={handleChange} placeholder="e.g. Landlord" />
              </div>
            </div>
            <button className="btn btn-primary" disabled={submitting}>
              {submitting ? "Adding..." : "Add Beneficiary"}
            </button>
          </form>
        </div>

        <div className="card">
          <h3 style={{ marginTop: 0 }}>Your Beneficiaries</h3>
          {loading ? (
            <div className="spinner" />
          ) : beneficiaries.length === 0 ? (
            <p className="text-muted">No beneficiaries added yet.</p>
          ) : (
            <div className="beneficiary-list">
              {beneficiaries.map((b) => (
                <div key={b.id} className="beneficiary-item">
                  <div>
                    <div style={{ fontWeight: 600 }}>{b.nickname || b.beneficiaryName}</div>
                    <div className="text-muted" style={{ fontSize: 13 }}>
                      {b.beneficiaryAccountNumber} {b.bankName ? `· ${b.bankName}` : ""}
                    </div>
                  </div>
                  <button className="btn btn-outline btn-sm" onClick={() => handleDelete(b.id)}>
                    Remove
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
