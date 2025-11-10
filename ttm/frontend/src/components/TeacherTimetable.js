import React, { useState, useEffect } from 'react';
import axios from 'axios';

const TeacherTimetable = () => {
  const [teachers, setTeachers] = useState([]);
  const [selectedTeacher, setSelectedTeacher] = useState('');
  const [timetable, setTimetable] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [sendingEmail, setSendingEmail] = useState(false);
  const [emailStatus, setEmailStatus] = useState(null);

  useEffect(() => {
    fetchTeachers();
  }, []);

  useEffect(() => {
    if (selectedTeacher) {
      fetchTeacherTimetable(selectedTeacher);
    } else {
      setTimetable([]);
    }
  }, [selectedTeacher]);

  const fetchTeachers = async () => {
    try {
      const response = await axios.get('/api/teachers');
      setTeachers(response.data);
    } catch (error) {
      setError('Failed to load teachers');
      console.error('Error fetching teachers:', error);
    }
  };

  const fetchTeacherTimetable = async (teacherId) => {
    setLoading(true);
    setError(null);
    setEmailStatus(null);
    try {
      const response = await axios.get(`/api/teachers/${teacherId}/timetable`);
      setTimetable(response.data);
    } catch (error) {
      setError('Failed to load timetable');
      console.error('Error fetching timetable:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSendEmail = async () => {
    if (!selectedTeacher) return;

    setSendingEmail(true);
    setEmailStatus(null);
    setError(null);

    try {
      const response = await axios.post(`/api/teachers/${selectedTeacher}/send-email`);
      setEmailStatus({
        type: 'success',
        message: `Timetable sent successfully to ${response.data.email}`
      });
      setTimeout(() => setEmailStatus(null), 5000);
    } catch (error) {
      setEmailStatus({
        type: 'error',
        message: error.response?.data?.error || 'Failed to send email'
      });
    } finally {
      setSendingEmail(false);
    }
  };

  // Group timetable by day
  const groupedTimetable = timetable.reduce((acc, slot) => {
    if (!acc[slot.day]) {
      acc[slot.day] = [];
    }
    acc[slot.day].push(slot);
    return acc;
  }, {});

  // Sort days
  const dayOrder = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];
  const sortedDays = Object.keys(groupedTimetable).sort((a, b) => {
    return dayOrder.indexOf(a) - dayOrder.indexOf(b);
  });

  return (
    <div>
      <div className="card">
        <h2>View Teacher Timetable</h2>
        <div className="input-group">
          <label htmlFor="teacher-select">Select a Teacher</label>
          <select
            id="teacher-select"
            value={selectedTeacher}
            onChange={(e) => setSelectedTeacher(e.target.value)}
          >
            <option value="">-- Select a teacher --</option>
            {teachers.map((teacher) => (
              <option key={teacher.id} value={teacher.id}>
                {teacher.name}
              </option>
            ))}
          </select>
        </div>
      </div>

      {error && <div className="error">{error}</div>}

      {loading && <div className="loading">Loading timetable...</div>}

      {!loading && selectedTeacher && timetable.length === 0 && (
        <div className="card">
          <p style={{ textAlign: 'center', color: '#666' }}>
            No timetable data found for this teacher.
          </p>
        </div>
      )}

      {emailStatus && (
        <div className={emailStatus.type === 'success' ? 'success' : 'error'}>
          {emailStatus.message}
        </div>
      )}

      {!loading && selectedTeacher && timetable.length > 0 && (
        <div className="card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px', flexWrap: 'wrap', gap: '15px' }}>
            <h2 style={{ margin: 0 }}>
              Timetable for {timetable[0]?.teacher?.name}
            </h2>
            {timetable[0]?.teacher?.email && (
              <button
                className="btn btn-primary"
                onClick={handleSendEmail}
                disabled={sendingEmail}
                style={{ display: 'flex', alignItems: 'center', gap: '8px' }}
              >
                {sendingEmail ? (
                  <>
                    <span>📧</span> Sending...
                  </>
                ) : (
                  <>
                    <span>📧</span> Send Timetable via Email
                  </>
                )}
              </button>
            )}
          </div>
          {timetable[0]?.teacher?.email && (
            <p style={{ marginBottom: '20px', color: '#666', fontSize: '0.9rem' }}>
              Email: {timetable[0].teacher.email}
            </p>
          )}
          
          {sortedDays.map((day) => (
            <div key={day} style={{ marginBottom: '30px' }}>
              <h3 style={{ 
                background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                color: 'white',
                padding: '10px 15px',
                borderRadius: '5px',
                marginBottom: '15px'
              }}>
                {day}
              </h3>
              <div className="table-container">
                <table>
                  <thead>
                    <tr>
                      <th>Time</th>
                      <th>Subject</th>
                      <th>Subject Code</th>
                      <th>Room</th>
                    </tr>
                  </thead>
                  <tbody>
                    {groupedTimetable[day]
                      .sort((a, b) => a.time.localeCompare(b.time))
                      .map((slot) => (
                        <tr key={slot.id}>
                          <td><strong>{slot.time}</strong></td>
                          <td>{slot.subject.name}</td>
                          <td>{slot.subject.code}</td>
                          <td>{slot.room.code}</td>
                        </tr>
                      ))}
                  </tbody>
                </table>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default TeacherTimetable;

