# Timetable Management System

A full-stack web application for managing university timetables using MERN stack with PostgreSQL and Prisma.

## Features

- 📤 Upload master timetable from Excel files
- 👨‍🏫 View individual teacher timetables
- 📧 Send timetable via email to teachers
- 🗄️ PostgreSQL database with Prisma ORM
- 🎨 Modern and responsive UI
- 📊 Automatic timetable organization by day and time

## Tech Stack

### Backend
- Node.js & Express.js
- PostgreSQL
- Prisma ORM
- Multer (file upload)
- XLSX (Excel parsing)
- Nodemailer (email sending)

### Frontend
- React.js
- Axios
- React Router

## Prerequisites

- Node.js (v14 or higher)
- PostgreSQL (v12 or higher)
- npm or yarn

## Installation

### 1. Clone the repository

```bash
git clone <your-repo-url>
cd dbms_prj
```

### 2. Backend Setup

```bash
cd backend
npm install
```

Create a `.env` file in the `backend` directory:

```env
PORT=5000
DATABASE_URL="postgresql://username:password@localhost:5432/timetable_db?schema=public"
NODE_ENV=development

# Email Configuration (SMTP)
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_SECURE=false
SMTP_USER=your-email@gmail.com
SMTP_PASSWORD=your-app-password
```

Replace `username`, `password`, and `localhost` with your PostgreSQL credentials.

**For Email Configuration:**
- For Gmail: Enable 2-factor authentication and generate an [App Password](https://support.google.com/accounts/answer/185833)
- Use the App Password (not your regular password) in `SMTP_PASSWORD`
- For other email providers, update `SMTP_HOST` and `SMTP_PORT` accordingly

### 3. Database Setup

Create a PostgreSQL database:

```sql
CREATE DATABASE timetable_db;
```

Run Prisma migrations:

```bash
npx prisma migrate dev --name init
```

Generate Prisma Client:

```bash
npx prisma generate
```

### 4. Frontend Setup

```bash
cd ../frontend
npm install
```

## Running the Application

### Start Backend Server

```bash
cd backend
npm run dev
```

The backend will run on `http://localhost:5000`

### Start Frontend Server

```bash
cd frontend
npm start
```

The frontend will run on `http://localhost:3000`

## Excel File Format

Your Excel file should have the following columns:

| Day | Time | Teacher | Email (Optional) | Subject Code | Subject Name | Room Code |
|-----|------|---------|------------------|--------------|--------------|-----------|
| Monday | 09:00-10:00 | Dr. John Smith | john.smith@university.edu | CS101 | Introduction to CS | A101 |
| Monday | 10:00-11:00 | Dr. Jane Doe | jane.doe@university.edu | MATH201 | Calculus | B202 |

**Column names are case-insensitive and can include spaces or underscores.**

**Note:** The `Email` column is optional. If not provided, the system will auto-generate an email address based on the teacher's name. However, to use the email feature, you should include actual email addresses in your Excel file.

## API Endpoints

### Upload Timetable
- **POST** `/api/timetable/upload`
  - Upload Excel file with timetable data

### Get All Timetable
- **GET** `/api/timetable`
  - Get all timetable slots

### Get All Teachers
- **GET** `/api/teachers`
  - Get list of all teachers

### Get Teacher Timetable
- **GET** `/api/teachers/:teacherId/timetable`
  - Get timetable for a specific teacher

### Send Timetable Email
- **POST** `/api/teachers/:teacherId/send-email`
  - Send timetable via email to the teacher's registered email address

## Database Schema

- **Teacher**: Stores teacher information
- **Subject**: Stores subject/course information
- **Room**: Stores room information
- **Slot**: Stores timetable slots linking teachers, subjects, and rooms

## Project Structure

```
dbms_prj/
├── backend/
│   ├── prisma/
│   │   └── schema.prisma
│   ├── routes/
│   │   ├── timetable.js
│   │   └── teachers.js
│   ├── utils/
│   │   └── emailService.js
│   ├── uploads/
│   ├── server.js
│   ├── package.json
│   └── .env
├── frontend/
│   ├── public/
│   ├── src/
│   │   ├── components/
│   │   │   ├── FileUpload.js
│   │   │   └── TeacherTimetable.js
│   │   ├── App.js
│   │   ├── App.css
│   │   └── index.js
│   └── package.json
└── README.md
```

## Notes

- The uploaded Excel files are automatically deleted after processing
- Existing timetable data will be cleared when uploading a new file (you can modify this behavior in `backend/routes/timetable.js`)
- The app supports both `.xlsx` and `.xls` formats
- Email feature requires proper SMTP configuration in the `.env` file
- Teachers must have a valid email address in the database to receive timetable emails

## License

ISC

