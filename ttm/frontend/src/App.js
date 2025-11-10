import React, { useState } from 'react';
import axios from 'axios';
import FileUpload from './components/FileUpload';
import TeacherTimetable from './components/TeacherTimetable';
import './App.css';

function App() {
  const [activeTab, setActiveTab] = useState('upload');
  const [uploadStatus, setUploadStatus] = useState(null);

  const handleUploadSuccess = (message) => {
    setUploadStatus({ type: 'success', message });
    setTimeout(() => setUploadStatus(null), 5000);
  };

  const handleUploadError = (error) => {
    setUploadStatus({ type: 'error', message: error });
    setTimeout(() => setUploadStatus(null), 5000);
  };

  return (
    <div className="App">
      <div className="container">
        <div className="header">
          <h1>📅 Timetable Management System</h1>
          <p>Upload your master timetable Excel file and view individual teacher schedules</p>
        </div>

        <div className="tabs">
          <button
            className={`tab-button ${activeTab === 'upload' ? 'active' : ''}`}
            onClick={() => setActiveTab('upload')}
          >
            Upload Timetable
          </button>
          <button
            className={`tab-button ${activeTab === 'view' ? 'active' : ''}`}
            onClick={() => setActiveTab('view')}
          >
            View Timetables
          </button>
        </div>

        {uploadStatus && (
          <div className={uploadStatus.type === 'success' ? 'success' : 'error'}>
            {uploadStatus.message}
          </div>
        )}

        {activeTab === 'upload' && (
          <FileUpload
            onUploadSuccess={handleUploadSuccess}
            onUploadError={handleUploadError}
          />
        )}

        {activeTab === 'view' && <TeacherTimetable />}
      </div>
    </div>
  );
}

export default App;





