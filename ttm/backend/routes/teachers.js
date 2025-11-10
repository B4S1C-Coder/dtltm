const express = require('express');
const { PrismaClient } = require('@prisma/client');

const router = express.Router();
const prisma = new PrismaClient();

// Get all teachers
router.get('/', async (req, res) => {
  try {
    const teachers = await prisma.teacher.findMany({
      orderBy: { name: 'asc' }
    });
    res.json(teachers);
  } catch (error) {
    console.error('Error fetching teachers:', error);
    res.status(500).json({ error: 'Failed to fetch teachers' });
  }
});

// Get timetable for a specific teacher
router.get('/:teacherId/timetable', async (req, res) => {
  try {
    const { teacherId } = req.params;

    const slots = await prisma.slot.findMany({
      where: { teacherId },
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
    console.error('Error fetching teacher timetable:', error);
    res.status(500).json({ error: 'Failed to fetch teacher timetable' });
  }
});

// Get teacher by name (for search)
router.get('/search/:name', async (req, res) => {
  try {
    const { name } = req.params;
    const teachers = await prisma.teacher.findMany({
      where: {
        name: {
          contains: name,
          mode: 'insensitive'
        }
      }
    });
    res.json(teachers);
  } catch (error) {
    console.error('Error searching teachers:', error);
    res.status(500).json({ error: 'Failed to search teachers' });
  }
});

// Send timetable email to teacher
router.post('/:teacherId/send-email', async (req, res) => {
  try {
    const { teacherId } = req.params;
    const { sendTimetableEmail } = require('../utils/emailService');

    // Get teacher details
    const teacher = await prisma.teacher.findUnique({
      where: { id: teacherId }
    });

    if (!teacher) {
      return res.status(404).json({ error: 'Teacher not found' });
    }

    if (!teacher.email) {
      return res.status(400).json({ error: 'Teacher email not found in database' });
    }

    // Get teacher timetable
    const slots = await prisma.slot.findMany({
      where: { teacherId },
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

    if (slots.length === 0) {
      return res.status(400).json({ error: 'No timetable found for this teacher' });
    }

    // Send email
    const result = await sendTimetableEmail(teacher.email, teacher.name, slots);

    res.json({
      message: 'Timetable emailed successfully',
      email: teacher.email,
      messageId: result.messageId
    });
  } catch (error) {
    console.error('Error sending email:', error);
    res.status(500).json({ 
      error: error.message || 'Failed to send email. Please check your email configuration.' 
    });
  }
});

module.exports = router;

