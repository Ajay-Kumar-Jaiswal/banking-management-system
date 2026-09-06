import { useEffect, useState } from "react";
import { getAllAccounts, updateAccountStatus } from "../../api/adminApi";
import { extractErrorMessage, formatCurrency } from "../../utils/errorUtils";
import AlertMessage from "../../components/AlertMessage";
import StatusBadge from "../../components/StatusBadge";

export default function AdminAccounts() {
  const [accounts, setAccounts] = useState([]);
  const [search, setSearch] = useState("");
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const load = async (e) => {
    if (e) e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const res = await getAllAccounts(search);
      setAccounts(res.data);
    } catch (err) {
      setError(extractErrorMessage(err, "Could not load accounts."));
    } finally {
      setLoading(false);
    }
  };

  const handleStatusChange = async (accountId, status) => {
    setError(null);
    setSuccess(null);
    try {
      await updateAccountStatus(accountId, status);
      setSuccess(`Account ${accountId} status updated to ${status}.`);
      await load();
    } catch (err) {
      setError(extractErrorMessage(err, "Could not update account status."));
    }
  };

  return (
    <div className="page">
      <h1 className="page-title">Accounts</h1>
      <div className="card">
        <AlertMessage message={error} />
        <AlertMessage type="success" message={success} />
        <form onSubmit={load} className="inline-form">
          <input
            placeholder="Search by account number or customer name..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
          <button className="btn btn-secondary" type="submit">
            Search
          </button>
        </form>
      </div>

      <div className="card">
        {loading ? (
          <p>Loading...</p>
        ) : accounts.length === 0 ? (
          <p>No accounts found.</p>
        ) : (
          <table className="table">
            <thead>
              <tr>
                <th>Account Number</th>
                <th>Customer</th>
                <th>Type</th>
                <th>Balance</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {accounts.map((acc) => (
                <tr key={acc.accountId}>
                  <td>{acc.accountNumber}</td>
                  <td>{acc.customerName}</td>
                  <td>{acc.accountType}</td>
                  <td>{formatCurrency(acc.balance)}</td>
                  <td>
                    <StatusBadge status={acc.status} />
                  </td>
                  <td className="action-cell">
                    {acc.status !== "ACTIVE" && (
                      <button className="btn btn-sm btn-secondary" onClick={() => handleStatusChange(acc.accountId, "ACTIVE")}>
                        Activate
                      </button>
                    )}
                    {acc.status !== "SUSPENDED" && (
                      <button className="btn btn-sm btn-danger" onClick={() => handleStatusChange(acc.accountId, "SUSPENDED")}>
                        Suspend
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
