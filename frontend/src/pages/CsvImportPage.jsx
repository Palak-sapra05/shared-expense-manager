import { useState } from 'react';
import api from '../api.js';

function CsvImportPage() {
  const [file, setFile] = useState(null);
  const [report, setReport] = useState(null);
  const [error, setError] = useState('');

  const handleFileChange = (event) => {
    setFile(event.target.files?.[0] || null);
    setReport(null);
    setError('');
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError('');
    setReport(null);

    if (!file) {
      setError('Please select a CSV file before uploading.');
      return;
    }

    const formData = new FormData();
    formData.append('file', file);

    try {
      const response = await api.post('/api/import', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
      setReport(response.data);
    } catch (uploadError) {
      const message = uploadError?.response?.data?.message || uploadError.message || 'Upload failed.';
      setError(message);
    }
  };

  return (
    <div className="page page-import">
      <section className="page-header">
        <h1>CSV Import</h1>
        <p>Upload a transaction export and review anomalies before group settlement.</p>
      </section>

      <form className="card form-card" onSubmit={handleSubmit}>
        <label className="file-label">
          CSV file
          <input type="file" accept=".csv" onChange={handleFileChange} />
        </label>

        <button type="submit" className="primary-button">
          Upload and analyze
        </button>

        {error && <div className="status-message error">{error}</div>}
        {report && (
          <div className="status-message success">
            <p>Imported rows: {report.importedRows}</p>
            <p>Detected anomalies: {report.anomalies?.length ?? 0}</p>
            {report.anomalies?.length > 0 && (
              <div className="anomaly-list">
                <h3>Anomaly details</h3>
                <ul>
                  {report.anomalies.map((anomaly) => (
                    <li key={anomaly.id ?? anomaly.rowNumber}>
                      <strong>{anomaly.type}</strong>: {anomaly.description}
                    </li>
                  ))}
                </ul>
              </div>
            )}
          </div>
        )}
      </form>
    </div>
  );
}

export default CsvImportPage;
