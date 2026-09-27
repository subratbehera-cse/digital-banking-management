import React, { useEffect, useState } from "react";
import { getMyTransactions } from "../api/apiService";
import { formatCurrency, formatDate, extractErrorMessage } from "../utils/format";
import StatusBadge from "../components/common/StatusBadge";
import Alert from "../components/common/Alert";
import Pagination from "../components/common/Pagination";

export default function TransactionHistory() {
  const [pageData, setPageData] = useState({ content: [], pageNumber: 0, totalPages: 0 });
  const [page, setPage] = useState(0);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      setLoading(true);
      try {
        const res = await getMyTransactions(page, 10);
        setPageData(res.data.data);
      } catch (err) {
        setError(extractErrorMessage(err));
      } finally {
        setLoading(false);
      }
    })();
  }, [page]);

  return (
    <div>
      <h1 className="section-title">Transaction History</h1>
      <p className="section-subtitle">A complete record of every deposit, withdrawal and transfer on your accounts.</p>

      <Alert type="error" message={error} />

      <div className="card">
        {loading ? (
          <div className="full-page-loader" style={{ minHeight: "30vh" }}>
            <div className="spinner" />
          </div>
        ) : pageData.content.length === 0 ? (
          <div className="empty-state">No transactions found.</div>
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
                    <th>Balance After</th>
                    <th>Status</th>
                    <th>Description</th>
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
                      <td>{t.balanceAfter != null ? formatCurrency(t.balanceAfter) : "-"}</td>
                      <td>
                        <StatusBadge value={t.status} />
                      </td>
                      <td>{t.description || "-"}</td>
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
