import { useEffect, useState } from "react";
import { getMyAccounts } from "../api/accountApi";
import { deposit } from "../api/transactionApi";
import { extractErrorMessage, formatCurrency } from "../utils/errorUtils";
import AlertMessage from "../components/AlertMessage";

export default function Deposit() {
  const [accounts, setAccounts] = useState([]);
  const [accountId, setAccountId] = useState("");
  const [amount, setAmount] = useState("");
  const [description, setDescription] = useState("");
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    getMyAccounts().then((res) => {
      setAccounts(res.data);
      if (res.data.length > 0) setAccountId(res.data[0].accountId);
    });
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);
    setSubmitting(true);
    try {
      const res = await deposit({ accountId: Number(accountId), amount: Number(amount), description });
      setSuccess(`Deposit successful. New balance: ${formatCurrency(res.data.balanceAfter)}`);
      setAmount("");
      setDescription("");
    } catch (err) {
      setError(extractErrorMessage(err, "Deposit failed."));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="page">
      <h1 className="page-title">Deposit Money</h1>
      <div className="card card-narrow">
        <AlertMessage message={error} />
        <AlertMessage type="success" message={success} />
        <form onSubmit={handleSubmit} className="auth-form">
          <label>
            Account
            <select value={accountId} onChange={(e) => setAccountId(e.target.value)} required>
              {accounts.map((acc) => (
                <option key={acc.accountId} value={acc.accountId}>
                  {acc.accountNumber} ({acc.accountType}) - {formatCurrency(acc.balance)}
                </option>
              ))}
            </select>
          </label>
          <label>
            Amount
            <input
              type="number"
              min="0.01"
              step="0.01"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              required
            />
          </label>
          <label>
            Description (optional)
            <input value={description} onChange={(e) => setDescription(e.target.value)} />
          </label>
          <button type="submit" className="btn btn-primary" disabled={submitting || !accountId}>
            {submitting ? "Processing..." : "Deposit"}
          </button>
        </form>
      </div>
    </div>
  );
}
