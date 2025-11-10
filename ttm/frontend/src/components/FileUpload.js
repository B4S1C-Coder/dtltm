import React, { useState } from 'react';
import axios from 'axios';

const FileUpload = ({ onUploadSuccess, onUploadError }) => {
  const [file, setFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [uploadStats, setUploadStats] = useState(null);

  const handleFileChange = (e) => {
    const selectedFile = e.target.files[0];
    if (selectedFile) {
      const ext = selectedFile.name.split('.').pop().toLowerCase();
      if (ext === 'xlsx' || ext === 'xls') {
        setFile(selectedFile);
      } else {
        onUploadError('Please select a valid Excel file (.xlsx or .xls)');
      }
    }
  };

  const handleUpload = async () => {
    if (!file) {
      onUploadError('Please select a file to upload');
      return;
    }

    setUploading(true);
    const formData = new FormData();
    formData.append('file', file);

    try {
      const response = await axios.post('/api/timetable/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });

      setUploadStats(response.data.stats);
      onUploadSuccess(
        `File uploaded successfully! Processed: ${response.data.stats.processed} rows, Skipped: ${response.data.stats.skipped} rows`
      );
      setFile(null);
      document.getElementById('file-input').value = '';
    } catch (error) {
      const errorMessage = error.response?.data?.error || 'Failed to upload file';
      onUploadError(errorMessage);
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="card">
      <h2>Upload Master Timetable</h2>
      <p style={{ marginBottom: '20px', color: '#666' }}>
        Upload an Excel file with columns: <strong>Day</strong>, <strong>Time</strong>,{' '}
        <strong>Teacher</strong>, <strong>Subject Code</strong>, <strong>Subject Name</strong>,{' '}
        <strong>Room Code</strong>
      </p>

      <div className="file-upload" onClick={() => document.getElementById('file-input').click()}>
        <div className="upload-icon">📄</div>
        <input
          id="file-input"
          type="file"
          accept=".xlsx,.xls"
          onChange={handleFileChange}
        />
        {file ? (
          <div>
            <p style={{ fontWeight: 'bold', color: '#667eea' }}>Selected: {file.name}</p>
            <p style={{ fontSize: '0.9rem', color: '#666' }}>
              {(file.size / 1024 / 1024).toFixed(2)} MB
            </p>
          </div>
        ) : (
          <div>
            <p>Click to select or drag and drop your Excel file</p>
            <p style={{ fontSize: '0.9rem', color: '#666' }}>
              Supported formats: .xlsx, .xls
            </p>
          </div>
        )}
      </div>

      {file && (
        <div style={{ marginTop: '20px', textAlign: 'center' }}>
          <button
            className="btn btn-primary"
            onClick={handleUpload}
            disabled={uploading}
          >
            {uploading ? 'Uploading...' : 'Upload Timetable'}
          </button>
        </div>
      )}

      {uploadStats && (
        <div style={{ marginTop: '20px', padding: '15px', background: '#f8f9ff', borderRadius: '5px' }}>
          <h3>Upload Statistics</h3>
          <p><strong>Processed:</strong> {uploadStats.processed} rows</p>
          <p><strong>Skipped:</strong> {uploadStats.skipped} rows</p>
          {uploadStats.errors && uploadStats.errors.length > 0 && (
            <div style={{ marginTop: '10px' }}>
              <p><strong>Errors:</strong></p>
              <ul style={{ fontSize: '0.9rem', color: '#666' }}>
                {uploadStats.errors.slice(0, 5).map((err, idx) => (
                  <li key={idx}>{err.error}</li>
                ))}
              </ul>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default FileUpload;





