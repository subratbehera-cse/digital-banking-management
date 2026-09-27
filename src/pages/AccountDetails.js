import React, { useEffect, useState, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { getAccountById, getAccountTransactions } from "../api/apiService";
import { formatCurrency, formatDate, extractErrorMessage } from "../utils/format";
import StatusBadge from "../components/common/StatusBadge";
import Alert from "../components/common/Alert";
import Pagination from "../components/common/Pagination";

export default function AccountDetails() {
  const { accountId } = useParams();
  const navigate = useNavigate();
  const [account, setAccount] = useState(null);
  const [txnPage, setTxnPage] = useState({ content: [], pageNumber: 0, totalPages: 0 });
  const [page, setPage] = useState(0);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const [accRes, txnRes] = await Promise.all([
        getAccountById(accountId),
        getAccountTransactions(accountId, page, 8),
      ]);
      setAccount(accRes.data.data);
      setTxnPage(txnRes.data.data);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [accountId, page]);

  useEffect(() => {
    load();
  }, [load]);

  if (loading && !account) {
    return (
      <div className="full-page-loader" style={{ minHeight: "50vh" }}>
        <div className="spinner" />
      </div>
    );
  }

  return (
    <div>
      <button className="btn btn-outline btn-sm" onClick={() => navigate("/accounts")} style={{ marginBottom: 16 }}>
        ← Back to Accounts
      </button>

      <Alert type="error" message={error} />

      {account && (
        <>
          <div className="balance-hero" style={{ marginBottom: 20 }}>
            <div className="label">{account.accountType} Account</div>
            <div className="amount">{formatCurrency(account.balance)}</div>
            <div className="account-no">{account.accountNumber}</div>
          </div>

          <div className="flex gap-12" style={{ marginBottom: 20 }}>
            <StatusBadge value={account.status} />
            <span className="text-muted" style={{ fontSize: 13.5 }}>
              Opened on {formatDate(account.createdAt)}
            </span>
          </div>

          <div className="flex gap-12" style={{ marginBottom: 24 }}>
            <button className="btn btn-primary" onClick={() => navigate("/transfer")}>
              Transfer
            </button>
            <button className="btn btn-outline" onClick={() => navigate("/deposit")}>
              Deposit
            </button>
            <button className="btn btn-outline" onClick={() => navigate("/withdraw")}>
              Withdraw
            </button>
          </div>

          <div className="card">
            <div className="card-header">
              <h3>Transaction History</h3>
            </div>
            {txnPage.content.length === 0 ? (
              <div className="empty-state">No transactions on this account yet.</div>
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
                        <th>Date</th>
                      </tr>
                    </thead>
                    <tbody>
                      {txnPage.content.map((t) => (
                        <tr key={t.id}>
                          <td>{t.transactionRef}</td>
                          <td>
                            <StatusBadge value={t.type} />
                          </td>
                          <td>{t.fromAccountNumber || "-"}</td>
                          <td>{t.toAccountNumber || "-"}</td>
                          <td>{formatCurrency(t.amount)}</td>
                          <td>{formatCurrency(t.balanceAfter)}</td>
                          <td>
                            <StatusBadge value={t.status} />
                          </td>
                          <td>{formatDate(t.createdAt)}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
                <Pagination page={txnPage.pageNumber} totalPages={txnPage.totalPages} onPageChange={setPage} />
              </>
            )}
          </div>
        </>
      )}
    </div>
  );
}
