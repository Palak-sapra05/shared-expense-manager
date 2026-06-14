import { Link } from 'react-router-dom';

function DashboardPage() {
  return (
    <div className="page page-dashboard">
      <section className="page-header">
        <h1>Dashboard</h1>
        <p>Get started by importing a CSV and reviewing group balances.</p>
      </section>

      <div className="cards-grid">
        <article className="card">
          <h2>CSV Import</h2>
          <p>Upload your transaction file to detect anomalies and capture expense data.</p>
          <Link to="/import" className="secondary-button">
            Upload CSV
          </Link>
        </article>

        <article className="card">
          <h2>Balances</h2>
          <p>Calculate each member's total paid, owed, and net balance for any group.</p>
          <Link to="/balances" className="secondary-button">
            View Balances
          </Link>
        </article>
      </div>
    </div>
  );
}

export default DashboardPage;
