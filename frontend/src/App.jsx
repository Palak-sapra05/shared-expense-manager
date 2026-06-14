import { Link, Navigate, Route, Routes } from 'react-router-dom'
import LoginPage from './pages/LoginPage.jsx'
import DashboardPage from './pages/DashboardPage.jsx'
import CsvImportPage from './pages/CsvImportPage.jsx'
import BalancePage from './pages/BalancePage.jsx'
import './App.css'

function App() {
  return (
    <div className="app-shell">
      <header className="app-header">
        <div className="brand">
          <h1>SplitTail</h1>
          <p>Shared expense manager for group CSV import and balances.</p>
        </div>
        <nav className="app-nav">
          <Link to="/dashboard">Dashboard</Link>
          <Link to="/import">CSV Import</Link>
          <Link to="/balances">Balances</Link>
        </nav>
      </header>

      <main className="app-main">
        <Routes>
          <Route path="/" element={<LoginPage />} />
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/import" element={<CsvImportPage />} />
          <Route path="/balances" element={<BalancePage />} />
          <Route path="*" element={<Navigate replace to="/dashboard" />} />
        </Routes>
      </main>
    </div>
  )
}

export default App
