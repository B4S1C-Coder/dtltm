import React, { useEffect, useState } from 'react';
import api from '../../api';

const AdminManageFaculty = () => {
  const [faculty, setFaculty] = useState([]);
  const [loading, setLoading] = useState(false);
  const [creating, setCreating] = useState(false);
  const [error, setError] = useState(null);

  const [form, setForm] = useState({ name: '', email: '', password: '' });

  const loadFaculty = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await api.get('/api/faculty');
      setFaculty(res.data || []);
    } catch (err) {
      setError('Failed to load faculty');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadFaculty();
  }, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    setCreating(true);
    setError(null);
    try {
      await api.post('/api/faculty', form);
      setForm({ name: '', email: '', password: '' });
      await loadFaculty();
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to create faculty');
    } finally {
      setCreating(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this faculty?')) return;
    try {
      await api.delete(`/api/faculty/${id}`);
      await loadFaculty();
    } catch (err) {
      alert('Failed to delete faculty');
    }
  };

  return (
    <div>
      <div className="header">
        <h1>👩‍🏫 Manage Faculty</h1>
        <p>Only admins can create, view, and delete faculty accounts.</p>
      </div>

      {error && <div className="error" style={{ marginBottom: '10px' }}>{error}</div>}

      <div className="card">
        <h2>Create Faculty Account</h2>
        <form onSubmit={handleCreate}>
          <div className="input-group">
            <label>Name</label>
            <input
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
              required
            />
          </div>
          <div className="input-group">
            <label>Email</label>
            <input
              type="email"
              value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
              required
            />
          </div>
          <div className="input-group">
            <label>Initial Password</label>
            <input
              type="password"
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
              required
            />
          </div>
          <button className="btn btn-primary" type="submit" disabled={creating}>
            {creating ? 'Creating...' : 'Create Faculty'}
          </button>
        </form>
      </div>

      <div className="card">
        <h2>Faculty List</h2>
        {loading ? (
          <div className="loading">Loading faculty...</div>
        ) : (
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Email</th>
                  <th style={{ width: '120px' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {faculty.map((f) => (
                  <tr key={f.id}>
                    <td>{f.name}</td>
                    <td>{f.email}</td>
                    <td>
                      <button
                        className="btn"
                        style={{ background: '#f8d7da', color: '#721c24', padding: '6px 12px', fontSize: '0.85rem' }}
                        onClick={() => handleDelete(f.id)}
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
                {faculty.length === 0 && (
                  <tr>
                    <td colSpan="3" style={{ textAlign: 'center', color: '#666' }}>
                      No faculty created yet.
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

export default AdminManageFaculty;
