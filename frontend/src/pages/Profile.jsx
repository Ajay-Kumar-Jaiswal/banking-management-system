import { useAuth } from "../context/AuthContext";

export default function Profile() {
  const { user } = useAuth();

  return (
    <div className="page">
      <h1 className="page-title">Profile</h1>
      <div className="card card-narrow">
        <div className="detail-grid">
          <div>
            <div className="detail-label">Full Name</div>
            <div className="detail-value">{user?.fullName}</div>
          </div>
          <div>
            <div className="detail-label">Email</div>
            <div className="detail-value">{user?.email}</div>
          </div>
          <div>
            <div className="detail-label">Role</div>
            <div className="detail-value">{user?.role}</div>
          </div>
          <div>
            <div className="detail-label">User ID</div>
            <div className="detail-value">{user?.userId}</div>
          </div>
        </div>
      </div>
    </div>
  );
}
