import React from 'react';
import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

const FacultyLayout = () => {
  const { user, logout } = useAuth();

  return (
    <div className="layout">
      <aside className="sidebar">
        <div className="sidebar-header">
          <h2>Faculty Panel</h2>
          <p style={{ fontSize: '0.85rem', opacity: 0.8 }}>
            {user?.email}
          </p>
        </div>
        <nav className="sidebar-nav">
          <NavLink to="/faculty" end>Dashboard</NavLink>
          <NavLink to="/faculty/preferences">Preferences</NavLink>
          <NavLink to="/faculty/allocation">My Allocation</NavLink>
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

export default FacultyLayout;
