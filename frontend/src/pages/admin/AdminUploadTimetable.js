import React, { useState } from 'react';
import FileUpload from '../../components/FileUpload';

const AdminUploadTimetable = () => {
  const [status, setStatus] = useState(null);

  const handleSuccess = (msg) => {
    setStatus({ type: 'success', message: msg });
    setTimeout(() => setStatus(null), 5000);
  };

  const handleError = (msg) => {
    setStatus({ type: 'error', message: msg });
    setTimeout(() => setStatus(null), 5000);
  };

  return (
    <div>
      <div className="header">
        <h1>📤 Upload Timetable</h1>
        <p>Upload the master Excel timetable to populate teachers, subjects, rooms and slots.</p>
      </div>
      {status && (
        <div className={status.type === 'success' ? 'success' : 'error'}>
          {status.message}
        </div>
      )}
      <FileUpload onUploadSuccess={handleSuccess} onUploadError={handleError} />
    </div>
  );
};

export default AdminUploadTimetable;
