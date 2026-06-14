import { useNavigate } from 'react-router-dom';

function LoginPage() {
  const navigate = useNavigate();

  return (
    <div className="page page-login">
      <section className="card welcome-card">
        <h1>SplitTail</h1>
        <p>Shared expense tracking for groups, CSV import, anomaly detection, and balance settlement.</p>
        <button className="primary-button" onClick={() => navigate('/dashboard')}>
          Continue to dashboard
        </button>
      </section>
    </div>
  );
}

export default LoginPage;
