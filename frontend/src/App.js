import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import ProtectedRoute from './components/ProtectedRoute';
import AdminLayout from './pages/admin/AdminLayout';
import AdminDashboard from './pages/admin/AdminDashboard';
import AdminUploadTimetable from './pages/admin/AdminUploadTimetable';
import AdminManageFaculty from './pages/admin/AdminManageFaculty';
import AdminManageAllocations from './pages/admin/AdminManageAllocations';
import FacultyLayout from './pages/faculty/FacultyLayout';
import FacultyDashboard from './pages/faculty/FacultyDashboard';
import FacultyPreferences from './pages/faculty/FacultyPreferences';
import FacultyAllocation from './pages/faculty/FacultyAllocation';

const App = () => {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login" replace />} />

      <Route path="/login" element={<LoginPage />} />

      {/* Admin routes */}
      <Route element={<ProtectedRoute requiredRole="ADMIN" />}>
        <Route path="/admin" element={<AdminLayout />}>
          <Route index element={<AdminDashboard />} />
          <Route path="upload-timetable" element={<AdminUploadTimetable />} />
          <Route path="faculty" element={<AdminManageFaculty />} />
          <Route path="allocations" element={<AdminManageAllocations />} />
        </Route>
      </Route>

      {/* Faculty routes */}
      <Route element={<ProtectedRoute requiredRole="FACULTY" />}>
        <Route path="/faculty" element={<FacultyLayout />}>
          <Route index element={<FacultyDashboard />} />
          <Route path="preferences" element={<FacultyPreferences />} />
          <Route path="allocation" element={<FacultyAllocation />} />
        </Route>
      </Route>

      {/* fallback */}
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
};

export default App;
