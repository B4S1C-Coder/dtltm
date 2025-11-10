# Testing Guide - Timetable Management System

Complete guide on how to test all features of your application.

## Prerequisites

Before testing, make sure:
- ✅ Backend is running on `http://localhost:5000`
- ✅ Frontend is running on `http://localhost:3000`
- ✅ Database is connected (Neon)

---

## Method 1: Test via Browser (Frontend - Easiest)

### Step 1: Open the Application

1. Make sure frontend is running:
   ```bash
   cd frontend
   npm start
   ```
2. Browser should open automatically to `http://localhost:3000`
3. Or manually navigate to: `http://localhost:3000`

### Step 2: Test Upload Timetable Feature

1. **Create a Test Excel File**

   Create an Excel file (`test_timetable.xlsx`) with these columns:

   | Day | Time | Teacher | Email | Subject Code | Subject Name | Room Code |
   |-----|------|---------|-------|--------------|--------------|-----------|
   | Monday | 09:00-10:00 | Dr. John Smith | john.smith@univ.edu | CS101 | Introduction to Computer Science | A101 |
   | Monday | 10:00-11:00 | Dr. Jane Doe | jane.doe@univ.edu | MATH201 | Calculus I | B202 |
   | Tuesday | 09:00-10:00 | Dr. John Smith | john.smith@univ.edu | CS102 | Data Structures | A101 |
   | Tuesday | 11:00-12:00 | Dr. Jane Doe | jane.doe@univ.edu | MATH202 | Calculus II | B202 |
   | Wednesday | 09:00-10:00 | Prof. Alice Brown | alice.brown@univ.edu | ENG101 | English Literature | C303 |

   **Column names can be:**
   - `Day`, `Time`, `Teacher`, `Email`, `Subject Code`, `Subject Name`, `Room Code`
   - Or: `day`, `time`, `teacher`, `email`, `subjectCode`, `subjectName`, `roomCode`
   - Or: `Day`, `Time`, `Teacher`, `Teacher Email`, `Subject_Code`, `Subject_Name`, `Room_Code`

2. **Upload the File**

   - Click on **"Upload Timetable"** tab
   - Click the upload area or drag and drop your Excel file
   - Click **"Upload Timetable"** button
   - **Expected Result**: Success message showing "Processed: X rows, Skipped: Y rows"

### Step 3: Test View Timetables Feature

1. **View Teacher List**
   - Click on **"View Timetables"** tab
   - You should see a dropdown with teacher names
   - Teachers from your Excel file should appear here

2. **View Individual Timetable**
   - Select a teacher from the dropdown (e.g., "Dr. John Smith")
   - **Expected Result**: 
     - Timetable displayed organized by day
     - Shows: Time, Subject, Subject Code, Room for each day
     - Days sorted: Monday, Tuesday, Wednesday, etc.

3. **Test Multiple Teachers**
   - Select different teachers from dropdown
   - Verify each teacher's timetable is different and correct

### Step 4: Test Email Feature (If Configured)

1. Select a teacher from dropdown
2. Wait for timetable to load
3. Click **"Send Timetable via Email"** button
4. **Expected Result**: 
   - Success message: "Timetable sent successfully to [email]"
   - Email sent to the teacher's email address

**Note**: Email feature requires SMTP configuration in `.env` file.

---

## Method 2: Test Backend API Directly

You can test the backend API endpoints directly using:

### Option A: Using Browser

1. **Health Check**
   ```
   http://localhost:5000/api/health
   ```
   Should return: `{"status":"OK","message":"Timetable Management API is running"}`

2. **Get All Teachers**
   ```
   http://localhost:5000/api/teachers
   ```
   Should return: JSON array of all teachers

3. **Get All Timetable**
   ```
   http://localhost:5000/api/timetable
   ```
   Should return: JSON array of all timetable slots

### Option B: Using PowerShell (in Terminal)

```powershell
# Health Check
Invoke-WebRequest -Uri http://localhost:5000/api/health -UseBasicParsing | Select-Object Content

# Get Teachers
Invoke-WebRequest -Uri http://localhost:5000/api/teachers -UseBasicParsing | Select-Object Content

# Get Timetable
Invoke-WebRequest -Uri http://localhost:5000/api/timetable -UseBasicParsing | Select-Object Content
```

### Option C: Using curl (if available)

```bash
# Health Check
curl http://localhost:5000/api/health

# Get Teachers
curl http://localhost:5000/api/teachers

# Get Timetable
curl http://localhost:5000/api/timetable
```

### Option D: Using Postman or Thunder Client (VS Code Extension)

1. **Install Thunder Client** in VS Code
2. Create requests:
   - GET `http://localhost:5000/api/health`
   - GET `http://localhost:5000/api/teachers`
   - GET `http://localhost:5000/api/timetable`
   - POST `http://localhost:5000/api/timetable/upload` (with file)

---

## Method 3: Test Database Directly

### Using Prisma Studio

1. **Open Prisma Studio**:
   ```bash
   cd backend
   npx prisma studio
   ```

2. **View Tables**:
   - Browser opens to `http://localhost:5555`
   - You can see all tables: Teachers, Subjects, Rooms, Slots
   - Click on each table to see the data
   - Add/edit/delete data manually if needed

### Using pgAdmin (if using local PostgreSQL)

1. Open pgAdmin
2. Connect to your database
3. Browse tables to verify data

### Using Neon Dashboard

1. Go to https://console.neon.tech
2. Select your project
3. Click "SQL Editor"
4. Run queries:
   ```sql
   SELECT * FROM teachers;
   SELECT * FROM subjects;
   SELECT * FROM rooms;
   SELECT * FROM slots;
   ```

---

## Complete Test Scenario

Follow this complete test flow:

### 1. Initial State Check
- [ ] Backend running: `http://localhost:5000/api/health` returns OK
- [ ] Frontend running: `http://localhost:3000` loads
- [ ] Database connected (check Prisma Studio)

### 2. Upload Test
- [ ] Create test Excel file with sample data
- [ ] Upload via frontend
- [ ] See success message with stats
- [ ] Verify data in Prisma Studio or database

### 3. View Test
- [ ] Teachers appear in dropdown
- [ ] Select teacher shows their timetable
- [ ] Timetable organized by day
- [ ] All days displayed correctly
- [ ] Times sorted correctly

### 4. Data Validation
- [ ] All teachers from Excel appear
- [ ] All subjects from Excel appear
- [ ] All rooms from Excel appear
- [ ] Timetable slots match Excel data

### 5. Email Test (Optional)
- [ ] Teacher has email in database
- [ ] Click "Send Email" button
- [ ] See success message
- [ ] Check email inbox (may take a minute)

### 6. Error Handling
- [ ] Try uploading invalid file (non-Excel)
- [ ] Try uploading empty file
- [ ] Try uploading file with missing columns
- [ ] Verify error messages appear

---

## Sample Test Excel File Format

Create a file named `sample_timetable.xlsx`:

### Sheet 1: Timetable Data

| Day | Time | Teacher | Email | Subject Code | Subject Name | Room Code |
|-----|------|---------|-------|--------------|--------------|-----------|
| Monday | 09:00-10:00 | Dr. John Smith | john.smith@univ.edu | CS101 | Introduction to CS | A101 |
| Monday | 10:00-11:00 | Dr. Jane Doe | jane.doe@univ.edu | MATH201 | Calculus | B202 |
| Tuesday | 09:00-10:00 | Dr. John Smith | john.smith@univ.edu | CS102 | Data Structures | A101 |
| Wednesday | 14:00-15:00 | Prof. Alice Brown | alice.brown@univ.edu | ENG101 | Literature | C303 |

**Save as Excel format (.xlsx or .xls)**

---

## Quick Test Checklist

Quick test if everything works:

```bash
# 1. Check backend
curl http://localhost:5000/api/health

# 2. Check teachers (should be empty before upload)
curl http://localhost:5000/api/teachers

# 3. Upload file via frontend
# Go to http://localhost:3000 and upload Excel file

# 4. Check teachers again (should have data now)
curl http://localhost:5000/api/teachers

# 5. View timetable in frontend
# Select a teacher from dropdown
```

---

## Troubleshooting Tests

### Backend not responding
- ✅ Check if backend is running: `npm run dev` in backend folder
- ✅ Check port 5000 is not blocked
- ✅ Check for errors in backend terminal

### Frontend not loading
- ✅ Check if frontend is running: `npm start` in frontend folder
- ✅ Check browser console for errors (F12)
- ✅ Verify backend is running first

### Upload not working
- ✅ Check Excel file format matches required columns
- ✅ Check backend terminal for errors
- ✅ Verify file is .xlsx or .xls format
- ✅ Check file size (should be < 10MB)

### No teachers showing
- ✅ Upload Excel file first
- ✅ Check if upload was successful
- ✅ Verify teachers table in Prisma Studio
- ✅ Check browser console for errors

### Database connection error
- ✅ Verify Neon connection string in `.env`
- ✅ Check Neon dashboard - project should be active
- ✅ Run `npx prisma migrate dev --name init` again

---

## Expected Results Summary

| Feature | Expected Result |
|---------|----------------|
| Backend Health | `{"status":"OK"}` |
| Upload Excel | Success message with processed rows |
| View Teachers | Dropdown shows all teachers |
| View Timetable | Timetable organized by day with all details |
| Send Email | Success message (if configured) |
| Database | All tables have data after upload |

---

## Need Help?

If tests fail:
1. Check backend terminal for errors
2. Check browser console (F12) for frontend errors
3. Verify database connection
4. Check `.env` file configuration
5. Try restarting both servers

Happy Testing! 🧪




