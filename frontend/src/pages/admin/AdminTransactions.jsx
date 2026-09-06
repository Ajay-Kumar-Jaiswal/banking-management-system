import { useEffect, useState } from "react";
import { getAllTransactions } from "../../api/adminApi";
import { extractErrorMessage, formatCurrency, formatDateTime } from "../../utils/errorUtils";
import AlertMessage from "../../components/AlertMessage";

export default function AdminTransactions() {
  const [transactions, setTransactions] = useState([]);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    load();
  }, []);

  const load = async () => {
    setLoading(true);
    try {
      const res = await getAllTransactions();
      setTransactions(res.data);
    } catch (err) {
      setError(extractErrorMessage(err, "Could not load transactions."));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <h1 className="page-title">All Transactions</h1>
      <div className="card">
        <AlertMessage message={error} />
        {loading ? (
          <p>Loading...</p>
        ) : transactions.length === 0 ? (
          <p>No transactions found.</p>
        ) : (
          <table className="table">
            <thead>
              <tr>
                <th>Date</th>
                <th>Reference</th>
                <th>Account</th>
                <th>Type</th>
                <th>Amount</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {transactions.map((tx) => (
                <tr key={tx.transactionId}>
                  <td>{formatDateTime(tx.createdAt)}</td>
                  <td>{tx.transactionReference}</td>
                  <td>{tx.accountNumber}</td>
                  <td>{tx.transactionType}</td>
                  <td>{formatCurrency(tx.amount)}</td>
                  <td>{tx.status}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
