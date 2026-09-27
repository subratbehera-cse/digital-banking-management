import React, { useEffect, useState } from "react";
import { getAdminCustomers, setCustomerStatus } from "../../api/apiService";
import { formatDate, extractErrorMessage } from "../../utils/format";
import Alert from "../../components/common/Alert";
import Pagination from "../../components/common/Pagination";

export default function CustomerManagement() {
  const [pageData, setPageData] = useState({ content: [], pageNumber: 0, totalPages: 0 });
  const [keyword, setKeyword] = useState("");
  const [page, setPage] = useState(0);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  const load = async (searchTerm = keyword, pageNo = page) => {
    setLoading(true);
    try {
      const res = await getAdminCustomers(searchTerm, pageNo, 10);
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

  const toggleStatus = async (customer) => {
    try {
      await setCustomerStatus(customer.id, !customer.enabled);
      await load(keyword, page);
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  };

  return (
    <div>
      <h1 className="section-title">Customer Management</h1>
      <p className="section-subtitle">Search customers and manage their account access.</p>

      <Alert type="error" message={error} />

      <form className="flex gap-12" style={{ marginBottom: 16 }} onSubmit={handleSearch}>
        <input
          style={{ maxWidth: 320 }}
          placeholder="Search by name, username or email"
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
          <div className="empty-state">No customers found.</div>
        ) : (
          <>
            <div className="table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>Username</th>
                    <th>Name</th>
                    <th>Email</th>
                    <th>Phone</th>
                    <th>Joined</th>
                    <th>Status</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {pageData.content.map((c) => (
                    <tr key={c.id}>
                      <td>{c.username}</td>
                      <td>
                        {c.firstName} {c.lastName}
                      </td>
                      <td>{c.email}</td>
                      <td>{c.phoneNumber || "-"}</td>
                      <td>{formatDate(c.createdAt)}</td>
                      <td>
                        <span className={`badge badge-${c.enabled ? "success" : "danger"}`}>
                          {c.enabled ? "Active" : "Disabled"}
                        </span>
                      </td>
                      <td>
                        <button
                          className={`btn btn-sm ${c.enabled ? "btn-outline" : "btn-accent"}`}
                          onClick={() => toggleStatus(c)}
                        >
                          {c.enabled ? "Deactivate" : "Activate"}
                        </button>
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
