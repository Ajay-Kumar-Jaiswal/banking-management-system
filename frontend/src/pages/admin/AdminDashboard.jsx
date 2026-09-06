import { useEffect, useState } from "react";
import { getAllCustomers, getAllAccounts, getAllTransactions } from "../../api/adminApi";
import { extractErrorMessage, formatCurrency } from "../../utils/errorUtils";
import AlertMessage from "../../components/AlertMessage";

export default function AdminDashboard() {
  const [stats, setStats] = useState({ customers: 0, accounts: 0, transactions: 0, totalBalance: 0 });
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    load();
  }, []);

  const load = async () => {
    setLoading(true);
    try {
      const [customersRes, accountsRes, transactionsRes] = await Promise.all([
        getAllCustomers(),
        getAllAccounts(),
        getAllTransactions(),
      ]);
      const totalBalance = accountsRes.data.reduce((sum, a) => sum + Number(a.balance), 0);
      setStats({
        customers: customersRes.data.length,
        accounts: accountsRes.data.length,
        transactions: transactionsRes.data.length,
        totalBalance,
      });
    } catch (err) {
      setError(extractErrorMessage(err, "Could not load admin dashboard."));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <h1 className="page-title">Admin Dashboard</h1>
      <AlertMessage message={error} />
      {loading ? (
        <p>Loading...</p>
      ) : (
        <div className="stat-grid">
          <div className="stat-card">
            <div className="stat-label">Total Customers</div>
            <div className="stat-value">{stats.customers}</div>
          </div>
          <div className="stat-card">
            <div className="stat-label">Total Accounts</div>
            <div className="stat-value">{stats.accounts}</div>
          </div>
          <div className="stat-card">
            <div className="stat-label">Total Transactions</div>
            <div className="stat-value">{stats.transactions}</div>
          </div>
          <div className="stat-card">
            <div className="stat-label">Bank-wide Balance</div>
            <div className="stat-value">{formatCurrency(stats.totalBalance)}</div>
          </div>
        </div>
      )}
    </div>
  );
}
