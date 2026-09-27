import React, { useEffect, useState } from "react";
import { getAdminAccounts, updateAccountStatus } from "../../api/apiService";
import { formatCurrency, maskAccountNumber, extractErrorMessage } from "../../utils/format";
import StatusBadge from "../../components/common/StatusBadge";
import Alert from "../../components/common/Alert";
import Pagination from "../../components/common/Pagination";

const STATUS_OPTIONS = ["ACTIVE", "INACTIVE", "CLOSED"];

export default function AccountManagement() {
  const [pageData, setPageData] = useState({ content: [], pageNumber: 0, totalPages: 0 });
  const [keyword, setKeyword] = useState("");
  const [page, setPage] = useState(0);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(true);

  const load = async (searchTerm = keyword, pageNo = page) => {
    setLoading(true);
    try {
      const res = await getAdminAccounts(searchTerm, pageNo, 10);
      setPageData(res.data.data);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load(keyword, page);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page]);

  const handleSearch = (e) => {
    e.preventDefault();
    setPage(0);
    load(keyword, 0);
  };

  const handleStatusChange = async (account, status) => {
    if (status === account.status) return;
    setError("");
    setSuccess("");
    try {
      await updateAccountStatus(account.id, status);
      setSuccess(`Account ${account.accountNumber} updated to ${status}`);
      await load(keyword, page);
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  };

  return (
    <div>
      <h1 className="section-title">Account Management</h1>
      <p className="section-subtitle">Search bank accounts and manage their activation status.</p>

      <Alert type="error" message={error} />
      <Alert type="success" message={success} />

      <form className="flex gap-12" style={{ marginBottom: 16 }} onSubmit={handleSearch}>
        <input
          style={{ maxWidth: 320 }}
          placeholder="Search by account number, owner username or email"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
        />
        <button className="btn btn-primary" type="submit">
          Search
        </button>
      </form>

      <div className="card">
        {loading ? (
          <div className="full-page-loader" style={{ minHeight: "30vh" }}>
            <div className="spinner" />
          </div>
        ) : pageData.content.length === 0 ? (
          <div className="empty-state">No accounts found.</div>
        ) : (
          <>
            <div className="table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>Account Number</th>
                    <th>Owner</th>
                    <th>Type</th>
                    <th>Balance</th>
                    <th>Status</th>
                    <th>Change Status</th>
                  </tr>
                </thead>
                <tbody>
                  {pageData.content.map((a) => (
                    <tr key={a.id}>
                      <td>{maskAccountNumber(a.accountNumber)}</td>
                      <td>
                        {a.ownerName} <span className="text-muted">({a.ownerUsername})</span>
                      </td>
                      <td>{a.accountType}</td>
                      <td>{formatCurrency(a.balance)}</td>
                      <td>
                        <StatusBadge value={a.status} />
                      </td>
                      <td>
                        <select
                          value={a.status}
                          onChange={(e) => handleStatusChange(a, e.target.value)}
                          style={{ maxWidth: 160 }}
                        >
                          {STATUS_OPTIONS.map((s) => (
                            <option key={s} value={s}>
                              {s}
                            </option>
                          ))}
                        </select>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <Pagination page={pageData.pageNumber} totalPages={pageData.totalPages} onPageChange={setPage} />
          </>
        )}
      </div>
    </div>
  );
}
