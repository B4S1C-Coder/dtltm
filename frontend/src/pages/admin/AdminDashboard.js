import React from 'react';

const AdminDashboard = () => {
  return (
    <div>
      <div className="header">
        <h1> Admin Dashboard</h1>
        <p>Overview of timetable, faculty, and allocations.</p>
      </div>
      <div className="card">
        <p>
          Use the sidebar to upload timetables, manage faculty accounts, and
          allocate courses based on preferences.
        </p>
      </div>
    </div>
  );
};

export default AdminDashboard;
