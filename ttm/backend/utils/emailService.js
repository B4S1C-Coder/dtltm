const nodemailer = require('nodemailer');

// Create reusable transporter object using SMTP transport
const createTransporter = () => {
  return nodemailer.createTransport({
    host: process.env.SMTP_HOST || 'smtp.gmail.com',
    port: parseInt(process.env.SMTP_PORT || '587'),
    secure: process.env.SMTP_SECURE === 'true', // true for 465, false for other ports
    auth: {
      user: process.env.SMTP_USER,
      pass: process.env.SMTP_PASSWORD
    }
  });
};

// Generate HTML email template for timetable
const generateTimetableHTML = (teacherName, timetableSlots) => {
  // Group timetable by day
  const groupedByDay = timetableSlots.reduce((acc, slot) => {
    if (!acc[slot.day]) {
      acc[slot.day] = [];
    }
    acc[slot.day].push(slot);
    return acc;
  }, {});

  // Sort days
  const dayOrder = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];
  const sortedDays = Object.keys(groupedByDay).sort((a, b) => {
    return dayOrder.indexOf(a) - dayOrder.indexOf(b);
  });

  let timetableHTML = '';
  sortedDays.forEach(day => {
    timetableHTML += `
      <div style="margin-bottom: 30px;">
        <h3 style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 12px; border-radius: 5px; margin-bottom: 15px;">
          ${day}
        </h3>
        <table style="width: 100%; border-collapse: collapse; background: white; border-radius: 5px; overflow: hidden; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
          <thead>
            <tr style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white;">
              <th style="padding: 12px; text-align: left; border-bottom: 2px solid #ddd;">Time</th>
              <th style="padding: 12px; text-align: left; border-bottom: 2px solid #ddd;">Subject</th>
              <th style="padding: 12px; text-align: left; border-bottom: 2px solid #ddd;">Subject Code</th>
              <th style="padding: 12px; text-align: left; border-bottom: 2px solid #ddd;">Room</th>
            </tr>
          </thead>
          <tbody>
            ${groupedByDay[day]
              .sort((a, b) => a.time.localeCompare(b.time))
              .map(slot => `
                <tr style="border-bottom: 1px solid #eee;">
                  <td style="padding: 12px;"><strong>${slot.time}</strong></td>
                  <td style="padding: 12px;">${slot.subject.name}</td>
                  <td style="padding: 12px;">${slot.subject.code}</td>
                  <td style="padding: 12px;">${slot.room.code}</td>
                </tr>
              `).join('')}
          </tbody>
        </table>
      </div>
    `;
  });

  return `
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="utf-8">
      <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
        .container { max-width: 800px; margin: 0 auto; padding: 20px; }
        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; border-radius: 10px; margin-bottom: 30px; text-align: center; }
        .footer { margin-top: 30px; padding: 20px; text-align: center; color: #666; font-size: 0.9rem; }
      </style>
    </head>
    <body>
      <div class="container">
        <div class="header">
          <h1>📅 Your Timetable</h1>
          <p>Academic Timetable for ${teacherName}</p>
        </div>
        ${timetableHTML}
        <div class="footer">
          <p>This is an automated email from the Timetable Management System.</p>
          <p>Please do not reply to this email.</p>
        </div>
      </div>
    </body>
    </html>
  `;
};

// Send timetable email to teacher
const sendTimetableEmail = async (teacherEmail, teacherName, timetableSlots) => {
  try {
    if (!process.env.SMTP_USER || !process.env.SMTP_PASSWORD) {
      throw new Error('Email configuration not set. Please configure SMTP_USER and SMTP_PASSWORD in .env file');
    }

    const transporter = createTransporter();

    const mailOptions = {
      from: `"Timetable Management System" <${process.env.SMTP_USER}>`,
      to: teacherEmail,
      subject: `Your Timetable - ${teacherName}`,
      html: generateTimetableHTML(teacherName, timetableSlots),
      text: generatePlainTextTimetable(teacherName, timetableSlots)
    };

    const info = await transporter.sendMail(mailOptions);
    return { success: true, messageId: info.messageId };
  } catch (error) {
    console.error('Error sending email:', error);
    throw error;
  }
};

// Generate plain text version for email clients that don't support HTML
const generatePlainTextTimetable = (teacherName, timetableSlots) => {
  const groupedByDay = timetableSlots.reduce((acc, slot) => {
    if (!acc[slot.day]) {
      acc[slot.day] = [];
    }
    acc[slot.day].push(slot);
    return acc;
  }, {});

  const dayOrder = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];
  const sortedDays = Object.keys(groupedByDay).sort((a, b) => {
    return dayOrder.indexOf(a) - dayOrder.indexOf(b);
  });

  let text = `Timetable for ${teacherName}\n\n`;
  sortedDays.forEach(day => {
    text += `${day}\n`;
    text += '='.repeat(day.length) + '\n';
    groupedByDay[day]
      .sort((a, b) => a.time.localeCompare(b.time))
      .forEach(slot => {
        text += `${slot.time} | ${slot.subject.name} (${slot.subject.code}) | Room: ${slot.room.code}\n`;
      });
    text += '\n';
  });

  return text;
};

module.exports = {
  sendTimetableEmail,
  createTransporter
};





