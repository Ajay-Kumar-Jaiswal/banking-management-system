import { useEffect, useState } from "react";
import { getBeneficiaries, addBeneficiary, deleteBeneficiary } from "../api/beneficiaryApi";
import { extractErrorMessage } from "../utils/errorUtils";
import AlertMessage from "../components/AlertMessage";

const initialForm = { name: "", accountNumber: "", bankName: "", ifscCode: "" };

export default function Beneficiaries() {
  const [beneficiaries, setBeneficiaries] = useState([]);
  const [form, setForm] = useState(initialForm);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    load();
  }, []);

  const load = async () => {
    try {
      const res = await getBeneficiaries();
      setBeneficiaries(res.data);
    } catch (err) {
      setError(extractErrorMessage(err, "Could not load beneficiaries."));
    }
  };

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleAdd = async (e) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);
    setSubmitting(true);
    try {
      await addBeneficiary(form);
      setSuccess("Beneficiary added successfully.");
      setForm(initialForm);
      await load();
    } catch (err) {
      setError(extractErrorMessage(err, "Could not add beneficiary."));
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id) => {
    setError(null);
    try {
      await deleteBeneficiary(id);
      await load();
    } catch (err) {
      setError(extractErrorMessage(err, "Could not delete beneficiary."));
    }
  };

  return (
    <div className="page">
      <h1 className="page-title">Beneficiaries</h1>

      <div className="card">
        <h2>Add a Beneficiary</h2>
        <AlertMessage message={error} />
        <AlertMessage type="success" message={success} />
        <form onSubmit={handleAdd} className="auth-form">
          <label>
            Name
            <input name="name" value={form.name} onChange={handleChange} required />
          </label>
          <label>
            Account Number
            <input name="accountNumber" value={form.accountNumber} onChange={handleChange} required />
          </label>
          <label>
            Bank Name
            <input name="bankName" value={form.bankName} onChange={handleChange} required />
          </label>
          <label>
            IFSC Code
            <input name="ifscCode" value={form.ifscCode} onChange={handleChange} required />
          </label>
          <button type="submit" className="btn btn-primary" disabled={submitting}>
            {submitting ? "Adding..." : "Add Beneficiary"}
          </button>
        </form>
      </div>

      <div className="card">
        <h2>Saved Beneficiaries</h2>
        {beneficiaries.length === 0 ? (
          <p>You haven't added any beneficiaries yet.</p>
        ) : (
          <table className="table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Account Number</th>
                <th>Bank</th>
                <th>IFSC</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {beneficiaries.map((b) => (
                <tr key={b.beneficiaryId}>
                  <td>{b.name}</td>
                  <td>{b.accountNumber}</td>
                  <td>{b.bankName}</td>
                  <td>{b.ifscCode}</td>
                  <td>
                    <button className="btn btn-danger btn-sm" onClick={() => handleDelete(b.beneficiaryId)}>
                      Delete
                    </button>
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
