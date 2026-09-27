import React, { useEffect, useState } from "react";
import { getAdminTransactions } from "../../api/apiService";
import { formatCurrency, formatDate, extractErrorMessage } from "../../utils/format";
import StatusBadge from "../../components/common/StatusBadge";
import Alert from "../../components/common/Alert";
import Pagination from "../../components/common/Pagination";

const TYPES = ["", "DEPOSIT", "WITHDRAWAL", "TRANSFER"];
const STATUSES = ["", "SUCCESS", "FAILED", "PENDING"];

export default function TransactionMonitoring() {
  const [pageData, setPageData] = useState({ content: [], pageNumber: 0, totalPages: 0 });
  const [type, setType] = useState("");
  const [status, setStatus] = useState("");
  const [page, setPage] = useState(0);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      setLoading(true);
      try {
        const res = await getAdminTransactions(type || undefined, status || undefined, page, 10);
        setPageData(res.data.data);
      } catch (err) {
        setError(extractErrorMessage(err));
      } finally {
        setLoading(false);
      }
    })();
  }, [type, status, page]);

  return (
    <div>
      <h1 className="section-title">Transaction Monitoring</h1>
      <p className="section-subtitle">Monitor every transaction across the bank, with filters by type and status.</p>

      <Alert type="error" message={error} />

      <div className="flex gap-12" style={{ marginBottom: 16 }}>
        <select
          value={type}
          onChange={(e) => {
            setPage(0);
            setType(e.target.value);
          }}
          style={{ maxWidth: 200 }}
        >
          <option value="">All Types</option>
          {TYPES.filter(Boolean).map((t) => (
            <option key={t} value={t}>
              {t}
            </option>
          ))}
        </select>
        <select
          value={status}
          onChange={(e) => {
            setPage(0);
            setStatus(e.target.value);
          }}
          style={{ maxWidth: 200 }}
        >
          <option value="">All Statuses</option>
          {STATUSES.filter(Boolean).map((s) => (
            <option key={s} value={s}>
              {s}
            </option>
          ))}
        </select>
      </div>

      <div className="card">
        {loading ? (
          <div className="full-page-loader" style={{ minHeight: "30vh" }}>
            <div className="spinner" />
          </div>
        ) : pageData.content.length === 0 ? (
          <div className="empty-state">No transactions match these filters.</div>
        ) : (
          <>
            <div className="table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>Reference</th>
                    <th>Type</th>
                    <th>From</th>
                    <th>To</th>
                    <th>Amount</th>
                    <th>Status</th>
                    <th>Failure Reason</th>
                    <th>Date</th>
                  </tr>
                </thead>
                <tbody>
                  {pageData.content.map((t) => (
                    <tr key={t.id}>
                      <td>{t.transactionRef}</td>
                      <td>
                        <StatusBadge value={t.type} />
                      </td>
                      <td>{t.fromAccountNumber || "-"}</td>
                      <td>{t.toAccountNumber || "-"}</td>
                      <td>{formatCurrency(t.amount)}</td>
                      <td>
                        <StatusBadge value={t.status} />
                      </td>
                      <td>{t.failureReason || "-"}</td>
                      <td>{formatDate(t.createdAt)}</td>
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
