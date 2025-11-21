import React, { useEffect, useState } from 'react';
import api from '../../api';

const AdminManageAllocations = () => {
  const [terms, setTerms] = useState([]);
  const [selectedTermId, setSelectedTermId] = useState('');
  const [allocations, setAllocations] = useState([]);
  const [loading, setLoading] = useState(false);
  const [runningAuto, setRunningAuto] = useState(false);
  const [error, setError] = useState(null);

  const [manual, setManual] = useState({ teacherId: '', subjectId: '' });

  useEffect(() => {
    api.get('/api/terms').then((res) => {
      const list = res.data || [];
      setTerms(list);
      const current = list.find((t) => t.isCurrent || t.current) || list[0];
      if (current) setSelectedTermId(current.id);
    });
  }, []);

  const loadAllocations = async () => {
    if (!selectedTermId) return;
    setLoading(true);
    setError(null);
    try {
      const res = await api.get(`/api/allocations?termId=${selectedTermId}`);
      setAllocations(res.data || []);
    } catch (err) {
      setError('Failed to load allocations');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAllocations();
  }, [selectedTermId]);

  const handleAuto = async () => {
    if (!selectedTermId) return;
    setRunningAuto(true);
    setError(null);
    try {
      await api.post('/api/allocations/auto', { termId: selectedTermId });
      await loadAllocations();
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to run auto-allocation');
    } finally {
      setRunningAuto(false);
    }
  };

  const handleManual = async (e) => {
    e.preventDefault();
    if (!selectedTermId) return;
    try {
      await api.post('/api/allocations/manual', {
        termId: selectedTermId,
        teacherId: manual.teacherId,
        subjectId: manual.subjectId,
      });
      setManual({ teacherId: '', subjectId: '' });
      await loadAllocations();
    } catch (err) {
      alert(err.response?.data?.error || 'Failed to override allocation');
    }
  };

  return (
    <div>
      <div className="header">
        <h1>📌 Manage Allocations</h1>
        <p>View, auto-allocate, and manually override course allocations.</p>
      </div>

      {error && <div className="error" style={{ marginBottom: '10px' }}>{error}</div>}

      <div className="card">
        <div className="input-group">
          <label>Term</label>
          <select
            value={selectedTermId}
            onChange={(e) => setSelectedTermId(e.target.value)}
          >
            <option value="">-- Select term --</option>
            {terms.map((t) => (
              <option key={t.id} value={t.id}>
                {t.name || t.code || t.id}
                {t.isCurrent || t.current ? ' (Current)' : ''}
              </option>
            ))}
          </select>
        </div>

        <button
          className="btn btn-primary"
          onClick={handleAuto}
          disabled={!selectedTermId || runningAuto}
        >
          {runningAuto ? 'Running...' : 'Run Auto Allocation'}
        </button>
      </div>

      <div className="card">
        <h2>Manual Override</h2>
        <p style={{ fontSize: '0.9rem', color: '#666' }}>
          Enter Teacher ID and Subject ID to override allocation for this term.
        </p>
        <form onSubmit={handleManual}>
          <div className="input-group">
            <label>Teacher ID</label>
            <input
              value={manual.teacherId}
              onChange={(e) => setManual({ ...manual, teacherId: e.target.value })}
              required
            />
          </div>
          <div className="input-group">
            <label>Subject ID</label>
            <input
              value={manual.subjectId}
              onChange={(e) => setManual({ ...manual, subjectId: e.target.value })}
              required
            />
          </div>
          <button className="btn btn-secondary" type="submit">
            Save Manual Allocation
          </button>
        </form>
      </div>

      <div className="card">
        <h2>Allocations</h2>
        {loading ? (
          <div className="loading">Loading allocations...</div>
        ) : (
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>Teacher</th>
                  <th>Teacher Email</th>
                  <th>Subject</th>
                  <th>Subject Code</th>
                </tr>
              </thead>
              <tbody>
                {allocations.map((a) => (
                  <tr key={a.id}>
                    <td>{a.teacher?.name}</td>
                    <td>{a.teacher?.email}</td>
                    <td>{a.subject?.name}</td>
                    <td>{a.subject?.code}</td>
                  </tr>
                ))}
                {allocations.length === 0 && (
                  <tr>
                    <td colSpan="4" style={{ textAlign: 'center', color: '#666' }}>
                      No allocations for this term yet.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};

export default AdminManageAllocations;
