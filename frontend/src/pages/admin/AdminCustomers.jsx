import { useEffect, useState } from "react";
import { getAllCustomers } from "../../api/adminApi";
import { extractErrorMessage, formatDateTime } from "../../utils/errorUtils";
import AlertMessage from "../../components/AlertMessage";

export default function AdminCustomers() {
  const [customers, setCustomers] = useState([]);
  const [search, setSearch] = useState("");
  const [error, setError] = useState(null);
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
      const res = await getAllCustomers(search);
      setCustomers(res.data);
    } catch (err) {
      setError(extractErrorMessage(err, "Could not load customers."));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <h1 className="page-title">Customers</h1>
      <div className="card">
        <AlertMessage message={error} />
        <form onSubmit={load} className="inline-form">
          <input
            placeholder="Search by name or email..."
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
        ) : customers.length === 0 ? (
          <p>No customers found.</p>
        ) : (
          <table className="table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Email</th>
                <th>Phone</th>
                <th>Role</th>
                <th>Joined</th>
              </tr>
            </thead>
            <tbody>
              {customers.map((c) => (
                <tr key={c.userId}>
                  <td>{c.userId}</td>
                  <td>{c.fullName}</td>
                  <td>{c.email}</td>
                  <td>{c.phoneNumber}</td>
                  <td>{c.role}</td>
                  <td>{formatDateTime(c.createdAt)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
