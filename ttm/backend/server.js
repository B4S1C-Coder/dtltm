const express = require('express');
const cors = require('cors');
const dotenv = require('dotenv');
const timetableRoutes = require('./routes/timetable');
const teacherRoutes = require('./routes/teachers');

dotenv.config();

const app = express();
const PORT = process.env.PORT || 5000;

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Routes
app.use('/api/timetable', timetableRoutes);
app.use('/api/teachers', teacherRoutes);

// Health check
app.get('/api/health', (req, res) => {
  res.json({ status: 'OK', message: 'Timetable Management API is running' });
});

app.listen(PORT, () => {
  console.log(`Server is running on port ${PORT}`);
});





