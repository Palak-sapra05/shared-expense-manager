import { useState } from 'react';
import api from '../api.js';

function BalancePage() {
  const [groupId, setGroupId] = useState('');
  const [balances, setBalances] = useState([]);
  const [settlements, setSettlements] = useState([]);
  const [error, setError] = useState('');

  const fetchBalances = async (event) => {
    event.preventDefault();
    setError('');
    setBalances([]);
    setSettlements([]);

    if (!groupId.trim()) {
      setError('Enter a group ID to fetch balances.');
      return;
    }

    try {
      const [balanceResponse, settlementResponse] = await Promise.all([
        api.get(`/api/balances/group/${groupId}`),
        api.get(`/api/balances/simplified/${groupId}`),
      ]);
      setBalances(balanceResponse.data || []);
      setSettlements(settlementResponse.data || []);
    } catch (loadError) {
      const message = loadError?.response?.data?.message || 'Unable to load balances for this group.';
      setError(message);
    }
  };

  return (
    <div className="page page-balances">
      <section className="page-header">
        <h1>Balance Summary</h1>
        <p>Calculate member balances and simplified settlement suggestions.</p>
      </section>

      <div className="card form-card">
        <form onSubmit={fetchBalances} className="balance-form">
          <label>
            Group ID
            <input
              type="text"
              value={groupId}
              onChange={(event) => setGroupId(event.target.value)}
              placeholder="e.g. 1"
            />
          </label>
          <button type="submit" className="primary-button">
            Load balances
          </button>
        </form>

        {error && <div className="status-message error">{error}</div>}
      </div>

      {balances.length > 0 && (
        <section className="card result-card">
          <h2>Member Balances</h2>
          <table>
            <thead>
              <tr>
                <th>User</th>
                <th>Paid (INR)</th>
                <th>Owed (INR)</th>
                <th>Net Balance (INR)</th>
              </tr>
            </thead>
            <tbody>
              {balances.map((balance) => (
                <tr key={balance.userId}>
                  <td>{balance.userName || `User ${balance.userId}`}</td>
                  <td>{balance.totalPaidInr ?? 0}</td>
                  <td>{balance.totalOwedInr ?? 0}</td>
                  <td>{balance.netBalanceInr ?? 0}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>
      )}

      {settlements.length > 0 && (
        <section className="card result-card">
          <h2>Suggested Settlements</h2>
          <ul>
            {settlements.map((settlement, index) => (
              <li key={index}>
                {settlement.fromUserName || `User ${settlement.fromUserId}`} pays {settlement.toUserName || `User ${settlement.toUserId}`} {settlement.amountInr} INR
              </li>
            ))}
          </ul>
        </section>
      )}
    </div>
  );
}

export default BalancePage;
