import React, { useEffect, useState } from "react";
import { getMyProfile, updateMyProfile } from "../api/apiService";
import Alert from "../components/common/Alert";
import { extractErrorMessage } from "../utils/format";

export default function Profile() {
  const [form, setForm] = useState(null);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    (async () => {
      try {
        const res = await getMyProfile();
        setForm(res.data.data);
      } catch (err) {
        setError(extractErrorMessage(err));
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");
    setSaving(true);
    try {
      const res = await updateMyProfile({
        firstName: form.firstName,
        lastName: form.lastName,
        phoneNumber: form.phoneNumber,
        address: form.address,
      });
      setForm(res.data.data);
      setSuccess("Profile updated successfully.");
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  if (loading || !form) {
    return (
      <div className="full-page-loader" style={{ minHeight: "50vh" }}>
        <div className="spinner" />
      </div>
    );
  }

  return (
    <div>
      <h1 className="section-title">My Profile</h1>
      <p className="section-subtitle">Update your personal information.</p>

      <div className="card" style={{ maxWidth: 640 }}>
        <Alert type="error" message={error} />
        <Alert type="success" message={success} />

        <form onSubmit={handleSubmit}>
          <div className="form-row">
            <div className="form-group">
              <label>Username</label>
              <input value={form.username} disabled />
            </div>
            <div className="form-group">
              <label>Email</label>
              <input value={form.email} disabled />
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label>First Name</label>
              <input name="firstName" value={form.firstName || ""} onChange={handleChange} required />
            </div>
            <div className="form-group">
              <label>Last Name</label>
              <input name="lastName" value={form.lastName || ""} onChange={handleChange} required />
            </div>
          </div>

          <div className="form-group">
            <label>Phone Number</label>
            <input name="phoneNumber" value={form.phoneNumber || ""} onChange={handleChange} />
          </div>

          <div className="form-group">
            <label>Address</label>
            <textarea name="address" rows={3} value={form.address || ""} onChange={handleChange} />
          </div>

          <button type="submit" className="btn btn-primary" disabled={saving}>
            {saving ? "Saving..." : "Save Changes"}
          </button>
        </form>
      </div>
    </div>
  );
}
