import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { registerUser } from "../api/authApi";
import { extractErrorMessage } from "../utils/errorUtils";
import AlertMessage from "../components/AlertMessage";

const initialForm = {
  fullName: "",
  email: "",
  phoneNumber: "",
  password: "",
  address: "",
};

export default function Register() {
  const navigate = useNavigate();
  const [form, setForm] = useState(initialForm);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);
    setSubmitting(true);
    try {
      await registerUser(form);
      setSuccess("Registration successful! Redirecting to login...");
      setTimeout(() => navigate("/login"), 1500);
    } catch (err) {
      setError(extractErrorMessage(err, "Registration failed. Please check your details."));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h1 className="auth-title">Create your account</h1>
        <p className="auth-subtitle">Join SecureBank in a few seconds</p>

        <AlertMessage message={error} />
        <AlertMessage type="success" message={success} />

        <form onSubmit={handleSubmit} className="auth-form">
          <label>
            Full Name
            <input name="fullName" value={form.fullName} onChange={handleChange} required />
          </label>
          <label>
            Email
            <input type="email" name="email" value={form.email} onChange={handleChange} required />
          </label>
          <label>
            Phone Number
            <input name="phoneNumber" value={form.phoneNumber} onChange={handleChange} required />
          </label>
          <label>
            Password
            <input
              type="password"
              name="password"
              value={form.password}
              onChange={handleChange}
              required
              minLength={8}
            />
          </label>
          <label>
            Address
            <input name="address" value={form.address} onChange={handleChange} required />
          </label>

          <button type="submit" className="btn btn-primary" disabled={submitting}>
            {submitting ? "Creating account..." : "Register"}
          </button>
        </form>

        <p className="auth-footer">
          Already have an account? <Link to="/login">Log in</Link>
        </p>
      </div>
    </div>
  );
}
