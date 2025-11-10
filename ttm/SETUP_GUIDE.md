# Quick Setup Guide - Timetable Management System

Follow these steps to get your application running:

## Prerequisites Check

Make sure you have installed:
- ✅ **Node.js** (v14 or higher) - [Download here](https://nodejs.org/)
- ✅ **PostgreSQL** (v12 or higher) - [Download here](https://www.postgresql.org/download/)
- ✅ **npm** (comes with Node.js)

## Step-by-Step Setup

### Step 1: Install Backend Dependencies

Open a terminal/command prompt and navigate to the backend folder:

```bash
cd backend
npm install
```

This will install all required packages including Express, Prisma, Nodemailer, etc.

### Step 2: Set Up PostgreSQL Database

1. **Open PostgreSQL** (pgAdmin or command line)

2. **Create the database:**
   ```sql
   CREATE DATABASE timetable_db;
   ```

3. **Note down your PostgreSQL credentials:**
   - Username (usually `postgres`)
   - Password (the one you set during PostgreSQL installation)
   - Host (usually `localhost`)
   - Port (usually `5432`)

### Step 3: Configure Environment Variables

1. **In the `backend` folder, create a `.env` file** (copy from `env.example` if needed)

2. **Edit the `.env` file with your settings:**

   ```env
   PORT=5000
   DATABASE_URL="postgresql://postgres:your_password@localhost:5432/timetable_db?schema=public"
   NODE_ENV=development

   # Email Configuration (SMTP) - Optional for now
   SMTP_HOST=smtp.gmail.com
   SMTP_PORT=587
   SMTP_SECURE=false
   SMTP_USER=your-email@gmail.com
   SMTP_PASSWORD=your-app-password
   ```

   **Important:** 
   - Replace `postgres` with your PostgreSQL username
   - Replace `your_password` with your PostgreSQL password
   - For email: Skip the email config for now if you just want to test without email functionality

### Step 4: Set Up Database Schema

Still in the `backend` folder, run:

```bash
npx prisma migrate dev --name init
```

This will:
- Create all database tables (teachers, subjects, rooms, slots)
- Set up the schema in your PostgreSQL database

Then generate the Prisma client:

```bash
npx prisma generate
```

### Step 5: Install Frontend Dependencies

Open a **NEW terminal window** (keep the backend one open) and navigate to the frontend folder:

```bash
cd frontend
npm install
```

### Step 6: Run the Application

You need **TWO terminal windows** running simultaneously:

#### Terminal 1 - Backend Server

```bash
cd backend
npm run dev
```

You should see:
```
Server is running on port 5000
```

✅ Backend is now running at `http://localhost:5000`

#### Terminal 2 - Frontend Server

```bash
cd frontend
npm start
```

The browser should automatically open to `http://localhost:3000`

If it doesn't, manually navigate to: `http://localhost:3000`

✅ Frontend is now running!

## Testing the Application

1. **Open the app** in your browser: `http://localhost:3000`

2. **Upload a Timetable:**
   - Click on "Upload Timetable" tab
   - Create an Excel file with these columns:
     - Day | Time | Teacher | Email (optional) | Subject Code | Subject Name | Room Code
   - Example:
     ```
     Day       | Time         | Teacher        | Email                    | Subject Code | Subject Name      | Room Code
     Monday    | 09:00-10:00  | Dr. John Smith | john.smith@univ.edu     | CS101        | Introduction to CS | A101
     Monday    | 10:00-11:00  | Dr. Jane Doe   | jane.doe@univ.edu       | MATH201      | Calculus          | B202
     ```
   - Upload the file

3. **View Teacher Timetable:**
   - Click on "View Timetables" tab
   - Select a teacher from the dropdown
   - View their schedule organized by day

4. **Send Email (if configured):**
   - After viewing a teacher's timetable
   - Click "Send Timetable via Email" button
   - The teacher will receive a formatted email

## Troubleshooting

### Database Connection Error
- ✅ Check PostgreSQL is running
- ✅ Verify username/password in `.env` file
- ✅ Ensure database `timetable_db` exists

### Port Already in Use
- ✅ Backend: Change `PORT=5000` to another port (e.g., `5001`) in `.env`
- ✅ Frontend: React will ask to use a different port automatically

### Email Not Working
- ✅ Check SMTP credentials in `.env`
- ✅ For Gmail: Enable 2FA and use App Password
- ✅ Email feature is optional - app works without it

### Module Not Found Errors
- ✅ Run `npm install` in both `backend` and `frontend` folders
- ✅ Delete `node_modules` and `package-lock.json`, then run `npm install` again

### Prisma Errors
- ✅ Run `npx prisma generate` again
- ✅ Make sure `.env` has correct `DATABASE_URL`

## Stopping the Application

- Press `Ctrl + C` in both terminal windows to stop the servers

## Need Help?

Check the main `README.md` file for more detailed information about:
- API endpoints
- Database schema
- Excel file format requirements





