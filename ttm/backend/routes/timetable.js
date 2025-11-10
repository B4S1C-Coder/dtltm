const express = require('express');
const multer = require('multer');
const path = require('path');
const fs = require('fs');
const XLSX = require('xlsx');
const { PrismaClient } = require('@prisma/client');

const router = express.Router();
const prisma = new PrismaClient();

// Configure multer for file uploads
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    const uploadDir = path.join(__dirname, '../uploads');
    if (!fs.existsSync(uploadDir)) {
      fs.mkdirSync(uploadDir, { recursive: true });
    }
    cb(null, uploadDir);
  },
  filename: (req, file, cb) => {
    cb(null, `timetable-${Date.now()}${path.extname(file.originalname)}`);
  }
});

const upload = multer({
  storage: storage,
  fileFilter: (req, file, cb) => {
    const ext = path.extname(file.originalname).toLowerCase();
    if (ext === '.xlsx' || ext === '.xls') {
      cb(null, true);
    } else {
      cb(new Error('Only Excel files (.xlsx, .xls) are allowed!'));
    }
  },
  limits: { fileSize: 10 * 1024 * 1024 } // 10MB limit
});

// Expected Excel format:
// Columns: Day | Time | Teacher | Subject Code | Subject Name | Room Code
router.post('/upload', upload.single('file'), async (req, res) => {
  try {
    if (!req.file) {
      return res.status(400).json({ error: 'No file uploaded' });
    }

    const filePath = req.file.path;
    const workbook = XLSX.readFile(filePath);
    const sheetName = workbook.SheetNames[0];
    const worksheet = workbook.Sheets[sheetName];
    const data = XLSX.utils.sheet_to_json(worksheet);

    // Process and insert data into database
    const results = await processTimetableData(data);

    // Clean up uploaded file
    fs.unlinkSync(filePath);

    res.json({
      message: 'Timetable uploaded successfully',
      stats: results
    });
  } catch (error) {
    console.error('Upload error:', error);
    if (req.file && fs.existsSync(req.file.path)) {
      fs.unlinkSync(req.file.path);
    }
    res.status(500).json({ error: error.message || 'Failed to process file' });
  }
});

async function processTimetableData(data) {
  let processed = 0;
  let skipped = 0;
  const errors = [];

  // Clear existing data (optional - comment out if you want to append)
  await prisma.slot.deleteMany({});
  await prisma.teacher.deleteMany({});
  await prisma.subject.deleteMany({});
  await prisma.room.deleteMany({});

  for (const row of data) {
    try {
      const day = String(row.Day || row.day || '').trim();
      const time = String(row.Time || row.time || '').trim();
      const teacherName = String(row.Teacher || row.teacher || '').trim();
      const teacherEmail = String(row.Email || row.email || row['Teacher Email'] || row.teacherEmail || '').trim();
      const subjectCode = String(row['Subject Code'] || row.subjectCode || row['Subject_Code'] || '').trim();
      const subjectName = String(row['Subject Name'] || row.subjectName || row['Subject_Name'] || '').trim();
      const roomCode = String(row['Room Code'] || row.roomCode || row['Room_Code'] || '').trim();

      if (!day || !time || !teacherName || !subjectCode || !roomCode) {
        skipped++;
        continue;
      }

      // Upsert teacher - use email from Excel if provided, otherwise generate one
      const email = teacherEmail || (teacherName.toLowerCase().replace(/\s+/g, '.') + '@university.edu');
      
      let teacher = await prisma.teacher.findUnique({
        where: { email: email }
      });

      if (!teacher) {
        teacher = await prisma.teacher.create({
          data: {
            name: teacherName,
            email: email
          }
        });
      } else if (teacherName !== teacher.name) {
        // Update teacher name if changed
        teacher = await prisma.teacher.update({
          where: { id: teacher.id },
          data: { name: teacherName }
        });
      }

      // Upsert subject
      let subject = await prisma.subject.findUnique({
        where: { code: subjectCode }
      });

      if (!subject) {
        subject = await prisma.subject.create({
          data: {
            code: subjectCode,
            name: subjectName || subjectCode
          }
        });
      }

      // Upsert room
      let room = await prisma.room.findUnique({
        where: { code: roomCode }
      });

      if (!room) {
        room = await prisma.room.create({
          data: {
            code: roomCode,
            capacity: null
          }
        });
      }

      // Create slot
      await prisma.slot.create({
        data: {
          day: day,
          time: time,
          teacherId: teacher.id,
          subjectId: subject.id,
          roomId: room.id
        }
      });

      processed++;
    } catch (error) {
      skipped++;
      errors.push({ row, error: error.message });
    }
  }

  return { processed, skipped, errors: errors.slice(0, 10) }; // Limit errors to first 10
}

// Get all timetable data
router.get('/', async (req, res) => {
  try {
    const slots = await prisma.slot.findMany({
      include: {
        teacher: true,
        subject: true,
        room: true
      },
      orderBy: [
        { day: 'asc' },
        { time: 'asc' }
      ]
    });

    res.json(slots);
  } catch (error) {
    console.error('Error fetching timetable:', error);
    res.status(500).json({ error: 'Failed to fetch timetable' });
  }
});

module.exports = router;

