import React, { useEffect, useState } from 'react';
import api from '../../api';
import { useAuth } from '../../context/AuthContext';

const FacultyAllocation = () => {
  const { user } = useAuth();
  const [terms, setTerms] = useState([]);
  const [selectedTermId, setSelectedTermId] = useState('');
  const [allocation, setAllocation] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    api.get('/api/terms').then((res) => {
      const list = res.data || [];
      setTerms(list);
      const current = list.find((t) => t.isCurrent || t.current) || list[0];
      if (current) setSelectedTermId(current.id);
    });
  }, []);

  useEffect(() => {
    const loadAlloc = async () => {
      if (!selectedTermId || !user?.teacherId) return;
      setLoading(true);
      try {
        const res = await api.get(
          `/api/allocations/teacher/${user.teacherId}?termId=${selectedTermId}`
        );
        setAllocation(res.data || null);
      } catch {
        setAllocation(null);
      } finally {
        setLoading(false);
      }
    };
    loadAlloc();
  }, [selectedTermId, user]);

  return (
    <div>
      <div className="header">
        <h1>✅ My Allocation</h1>
        <p>See which subject has been allocated to you for the selected term.</p>
      </div>

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

        {loading && <div className="loading">Loading allocation...</div>}

        {!loading && selectedTermId && (
          <>
            {allocation ? (
              <div className="card" style={{ marginTop: '10px', background: '#f8f9ff' }}>
                <h3>Allocated Subject</h3>
                <p>
                  <strong>{allocation.subject?.name}</strong> ({allocation.subject?.code})
                </p>
              </div>
            ) : (
              <p style={{ marginTop: '10px', color: '#666' }}>
                No allocation found for this term yet.
              </p>
            )}
          </>
        )}
      </div>
    </div>
  );
};

export default FacultyAllocation;
