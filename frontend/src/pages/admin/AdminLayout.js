import React from 'react';
import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

const AdminLayout = () => {
  const { user, logout } = useAuth();

  return (
    <div className="layout">
      <aside className="sidebar">
        <div className="sidebar-header">
          <h2>Admin Panel</h2>
          <p style={{ fontSize: '0.85rem', opacity: 0.8 }}>
            {user?.email}
          </p>
        </div>
        <nav className="sidebar-nav">
          <NavLink to="/admin" end>Dashboard</NavLink>
          <NavLink to="/admin/upload-timetable">Upload Timetable</NavLink>
          <NavLink to="/admin/faculty">Manage Faculty</NavLink>
          <NavLink to="/admin/allocations">Allocations</NavLink>
        </nav>
        <button className="btn btn-secondary" onClick={logout}>
          Logout
        </button>
      </aside>
      <main className="layout-content">
        <Outlet />
      </main>
    </div>
  );
};

export default AdminLayout;
