import React, { useEffect, useMemo, useState } from 'react';
import api from '../../api';
import { useAuth } from '../../context/AuthContext';

const FacultyPreferences = () => {
  const { user } = useAuth();

  const [terms, setTerms] = useState([]);
  const [selectedTermId, setSelectedTermId] = useState('');
  const [courses, setCourses] = useState([]);
  const [preferences, setPreferences] = useState([]);

  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);

  const [error, setError] = useState(null);
  const [status, setStatus] = useState(null);

  useEffect(() => {
    api.get('/api/terms')
      .then((res) => {
        const list = res.data || [];
        setTerms(list);
        const current = list.find((t) => t.isCurrent || t.current) || list[0];
        if (current) setSelectedTermId(current.id);
      })
      .catch(() => setError('Failed to load terms'));
  }, []);

  useEffect(() => {
    const loadData = async () => {
      if (!selectedTermId || !user?.teacherId) return;
      setLoading(true);
      setError(null);
      setStatus(null);
      try {
        const [courseRes, prefRes] = await Promise.all([
          api.get(`/api/courses?termId=${selectedTermId}`),
          api.get(`/api/preferences?teacherId=${user.teacherId}&termId=${selectedTermId}`),
        ]);

        const courseList = courseRes.data || [];
        const prefList = (prefRes.data || []).sort((a, b) => a.rank - b.rank);

        setCourses(courseList);
        setPreferences(
          prefList.map((p) => ({
            id: p.id,
            rank: p.rank,
            subject: p.subject,
          }))
        );
      } catch (err) {
        setError('Failed to load courses or preferences');
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, [selectedTermId, user]);

  const availableCourses = useMemo(() => {
    const selectedIds = new Set(preferences.map((p) => p.subject?.id));
    return courses.filter((c) => !selectedIds.has(c.id));
  }, [courses, preferences]);

  const addPreference = (course) => {
    setPreferences((prev) => {
      const nextRank = prev.length + 1;
      return [
        ...prev,
        {
          id: `${course.id}-${nextRank}`,
          rank: nextRank,
          subject: course,
        },
      ];
    });
    setError(null);
    setStatus(null);
  };

  const removePreference = (id) => {
    setPreferences((prev) =>
      prev
        .filter((p) => p.id !== id)
        .sort((a, b) => a.rank - b.rank)
        .map((p, index) => ({ ...p, rank: index + 1 }))
    );
  };

  const movePreference = (id, direction) => {
    setPreferences((prev) => {
      const sorted = [...prev].sort((a, b) => a.rank - b.rank);
      const index = sorted.findIndex((p) => p.id === id);
      if (index === -1) return prev;
      const newIndex = direction === 'up' ? index - 1 : index + 1;
      if (newIndex < 0 || newIndex >= sorted.length) return prev;

      const temp = sorted[index];
      sorted[index] = sorted[newIndex];
      sorted[newIndex] = temp;

      return sorted.map((p, idx) => ({ ...p, rank: idx + 1 }));
    });
  };

  const handleSave = async () => {
    if (!selectedTermId || !user?.teacherId) return;
    if (preferences.length === 0) {
      setError('Please add at least one preference.');
      return;
    }

    setSaving(true);
    setError(null);
    setStatus(null);

    try {
      const payload = {
        teacherId: user.teacherId,
        termId: selectedTermId,
        preferences: preferences
          .slice()
          .sort((a, b) => a.rank - b.rank)
          .map((p) => ({
            subjectId: p.subject.id,
            rank: p.rank,
          })),
      };

      const res = await api.post('/api/preferences', payload);
      const updated = (res.data || []).sort((a, b) => a.rank - b.rank);
      setPreferences(
        updated.map((p) => ({
          id: p.id,
          rank: p.rank,
          subject: p.subject,
        }))
      );
      setStatus('Preferences saved successfully.');
      setTimeout(() => setStatus(null), 5000);
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to save preferences');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div>
      <div className="header">
        <h1>🎯 My Preferences</h1>
        <p>Choose and order the subjects you prefer to teach.</p>
      </div>

      {error && <div className="error" style={{ marginBottom: '10px' }}>{error}</div>}
      {status && <div className="success" style={{ marginBottom: '10px' }}>{status}</div>}

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

        {loading && <div className="loading">Loading courses & preferences...</div>}

        {!loading && selectedTermId && (
          <div className="pref-layout">
            <div className="pref-column">
              <h3>Available Subjects</h3>
              {availableCourses.length === 0 ? (
                <p style={{ color: '#888', fontSize: '0.9rem' }}>
                  All subjects are already in your preference list.
                </p>
              ) : (
                <div className="table-container">
                  <table>
                    <thead>
                      <tr>
                        <th>Code</th>
                        <th>Name</th>
                        <th style={{ width: '120px' }}>Action</th>
                      </tr>
                    </thead>
                    <tbody>
                      {availableCourses.map((c) => (
                        <tr key={c.id}>
                          <td><strong>{c.code}</strong></td>
                          <td>{c.name}</td>
                          <td>
                            <button
                              className="btn btn-primary"
                              style={{ padding: '6px 12px', fontSize: '0.9rem' }}
                              onClick={() => addPreference(c)}
                            >
                              Add
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>

            <div className="pref-column">
              <h3>Your Preference Order</h3>
              {preferences.length === 0 ? (
                <p style={{ color: '#888', fontSize: '0.9rem' }}>
                  No preferences yet. Add subjects from the left.
                </p>
              ) : (
                <div className="table-container">
                  <table>
                    <thead>
                      <tr>
                        <th>Rank</th>
                        <th>Subject</th>
                        <th>Code</th>
                        <th style={{ width: '170px' }}>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {preferences
                        .slice()
                        .sort((a, b) => a.rank - b.rank)
                        .map((p, idx, arr) => (
                          <tr key={p.id}>
                            <td><strong>{p.rank}</strong></td>
                            <td>{p.subject?.name}</td>
                            <td>{p.subject?.code}</td>
                            <td>
                              <div style={{ display: 'flex', gap: '6px', flexWrap: 'wrap' }}>
                                <button
                                  className="btn btn-secondary"
                                  style={{ padding: '4px 10px', fontSize: '0.8rem' }}
                                  disabled={idx === 0}
                                  onClick={() => movePreference(p.id, 'up')}
                                >
                                  ▲ Up
                                </button>
                                <button
                                  className="btn btn-secondary"
                                  style={{ padding: '4px 10px', fontSize: '0.8rem' }}
                                  disabled={idx === arr.length - 1}
                                  onClick={() => movePreference(p.id, 'down')}
                                >
                                  ▼ Down
                                </button>
                                <button
                                  className="btn"
                                  style={{
                                    padding: '4px 10px',
                                    fontSize: '0.8rem',
                                    background: '#f8d7da',
                                    color: '#721c24',
                                  }}
                                  onClick={() => removePreference(p.id)}
                                >
                                  ✕ Remove
                                </button>
                              </div>
                            </td>
                          </tr>
                        ))}
                    </tbody>
                  </table>
                </div>
              )}

              <div style={{ marginTop: '20px', textAlign: 'right' }}>
                <button
                  className="btn btn-primary"
                  disabled={saving || preferences.length === 0}
                  onClick={handleSave}
                >
                  {saving ? 'Saving...' : 'Save Preferences'}
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default FacultyPreferences;
