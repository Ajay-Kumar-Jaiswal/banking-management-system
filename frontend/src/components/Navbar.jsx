import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Navbar() {
  const { user, logout, isAdmin } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  if (!user) return null;

  const customerLinks = [
    { to: "/dashboard", label: "Dashboard" },
    { to: "/accounts", label: "My Accounts" },
    { to: "/transfer", label: "Transfer" },
    { to: "/deposit", label: "Deposit" },
    { to: "/withdraw", label: "Withdraw" },
    { to: "/transactions", label: "Transactions" },
    { to: "/beneficiaries", label: "Beneficiaries" },
    { to: "/profile", label: "Profile" },
  ];

  const adminLinks = [
    { to: "/admin", label: "Admin Dashboard" },
    { to: "/admin/customers", label: "Customers" },
    { to: "/admin/accounts", label: "Accounts" },
    { to: "/admin/transactions", label: "Transactions" },
  ];

  const links = isAdmin ? adminLinks : customerLinks;

  return (
    <nav className="navbar">
      <div className="navbar-brand">SecureBank</div>
      <div className="navbar-links">
        {links.map((link) => (
          <NavLink
            key={link.to}
            to={link.to}
            className={({ isActive }) => "navbar-link" + (isActive ? " active" : "")}
          >
            {link.label}
          </NavLink>
        ))}
      </div>
      <div className="navbar-user">
        <span className="navbar-username">{user.fullName}</span>
        <button className="btn btn-ghost" onClick={handleLogout}>
          Logout
        </button>
      </div>
    </nav>
  );
}
