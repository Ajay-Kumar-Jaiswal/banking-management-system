import { useEffect, useState } from "react";
import { getMyAccounts } from "../api/accountApi";
import { getBeneficiaries } from "../api/beneficiaryApi";
import { transfer } from "../api/transactionApi";
import { extractErrorMessage, formatCurrency } from "../utils/errorUtils";
import AlertMessage from "../components/AlertMessage";

export default function Transfer() {
  const [accounts, setAccounts] = useState([]);
  const [beneficiaries, setBeneficiaries] = useState([]);
  const [fromAccountId, setFromAccountId] = useState("");
  const [toAccountNumber, setToAccountNumber] = useState("");
  const [amount, setAmount] = useState("");
  const [description, setDescription] = useState("");
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    getMyAccounts().then((res) => {
      setAccounts(res.data);
      if (res.data.length > 0) setFromAccountId(res.data[0].accountId);
    });
    getBeneficiaries().then((res) => setBeneficiaries(res.data));
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);
    setSubmitting(true);
    try {
      const res = await transfer({
        fromAccountId: Number(fromAccountId),
        toAccountNumber,
        amount: Number(amount),
        description,
      });
      setSuccess(
        `Transfer successful. Reference: ${res.data.transactionReference}. Your new balance: ${formatCurrency(
          res.data.balanceAfter
        )}`
      );
      setAmount("");
      setDescription("");
    } catch (err) {
      setError(extractErrorMessage(err, "Transfer failed."));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="page">
      <h1 className="page-title">Transfer Money</h1>
      <div className="card card-narrow">
        <AlertMessage message={error} />
        <AlertMessage type="success" message={success} />
        <form onSubmit={handleSubmit} className="auth-form">
          <label>
            From Account
            <select value={fromAccountId} onChange={(e) => setFromAccountId(e.target.value)} required>
              {accounts.map((acc) => (
                <option key={acc.accountId} value={acc.accountId}>
                  {acc.accountNumber} ({acc.accountType}) - {formatCurrency(acc.balance)}
                </option>
              ))}
            </select>
          </label>

          <label>
            To Account Number
            <input
              value={toAccountNumber}
              onChange={(e) => setToAccountNumber(e.target.value)}
              placeholder="e.g. AC1234567890"
              required
            />
          </label>

          {beneficiaries.length > 0 && (
            <div className="beneficiary-quickpicks">
              <span>Quick pick:</span>
              {beneficiaries.map((b) => (
                <button
                  type="button"
                  key={b.beneficiaryId}
                  className="chip"
                  onClick={() => setToAccountNumber(b.accountNumber)}
                >
                  {b.name}
                </button>
              ))}
            </div>
          )}

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

          <button type="submit" className="btn btn-primary" disabled={submitting || !fromAccountId}>
            {submitting ? "Processing..." : "Transfer"}
          </button>
        </form>
      </div>
    </div>
  );
}
