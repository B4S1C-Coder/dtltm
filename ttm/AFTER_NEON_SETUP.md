# Steps After Neon DB Setup

Complete guide on what to do after you've created your Neon database.

## Prerequisites Checklist

Before starting, make sure you have:
- ✅ Created Neon account at https://neon.tech
- ✅ Created a new project in Neon
- ✅ Copied your connection string from Neon dashboard

---

## Step 1: Update `.env` File

1. **Open** `backend/.env` file in VS Code or any text editor

2. **Replace** the `DATABASE_URL` line with your Neon connection string:

```env
PORT=5000
DATABASE_URL="postgresql://user:password@ep-xxx-xxx.region.aws.neon.tech/neondb?sslmode=require"
NODE_ENV=development

# Email Configuration (SMTP) - Optional, can skip for now
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_SECURE=false
SMTP_USER=your-email@gmail.com
SMTP_PASSWORD=your-app-password
```

**Important**: 
- Use the FULL connection string from Neon dashboard
- Make sure it includes `?sslmode=require` at the end
- The connection string should look like: `postgresql://username:password@ep-xxxxx-xxxxx.region.aws.neon.tech/neondb?sslmode=require`

3. **Save** the file (Ctrl+S)

---

## Step 2: Run Database Migrations

This creates all the tables in your Neon database.

Open terminal in VS Code (`` Ctrl + ` ``) and run:

```bash
cd backend
npx prisma migrate dev --name init
```

**What happens:**
- Prisma connects to your Neon database
- Creates tables: `teachers`, `subjects`, `rooms`, `slots`
- Sets up relationships between tables
- Creates migration files in `prisma/migrations/`

**Expected output:**
```
✔ Migration completed successfully!
```

**If you see errors:**
- Check your connection string in `.env`
- Make sure it includes `?sslmode=require`
- Verify your Neon project is active

---

## Step 3: Generate Prisma Client

This generates the database client code:

```bash
npx prisma generate
```

**Expected output:**
```
✔ Generated Prisma Client
```

---

## Step 4: Verify Database (Optional but Recommended)

Check that everything is set up correctly:

```bash
npx prisma studio
```

This opens a web interface where you can:
- View your database tables
- See that they're empty (ready for data)
- Manually add/edit data if needed

**To exit**: Press `Ctrl + C` in the terminal

---

## Step 5: Start Backend Server

**Open a new terminal** in VS Code (click the "+" button next to terminal tab):

```bash
cd backend
npm run dev
```

**Expected output:**
```
Server is running on port 5000
```

✅ **Backend is now running!**

**Keep this terminal running** - don't close it!

---

## Step 6: Start Frontend Server

**Open another new terminal** (click "+" again):

```bash
cd frontend
npm start
```

**Expected output:**
```
Compiled successfully!

You can now view timetable-frontend in the browser.
Local:            http://localhost:3000
```

Your browser should automatically open to `http://localhost:3000`

✅ **Frontend is now running!**

---

## Step 7: Test the Application

### Test 1: Check UI Loads
- Browser should show "Timetable Management System"
- You should see two tabs: "Upload Timetable" and "View Timetables"

### Test 2: Upload Timetable
1. Create a simple Excel file with these columns:
   ```
   Day       | Time         | Teacher        | Email                    | Subject Code | Subject Name      | Room Code
   Monday    | 09:00-10:00  | Dr. John Smith | john.smith@univ.edu     | CS101        | Introduction to CS | A101
   Tuesday   | 10:00-11:00  | Dr. Jane Doe   | jane.doe@univ.edu       | MATH201      | Calculus          | B202
   ```
2. Click "Upload Timetable" tab
3. Click to select your Excel file
4. Click "Upload Timetable"
5. Should see success message with stats

### Test 3: View Timetables
1. Click "View Timetables" tab
2. Select a teacher from dropdown
3. Should see their timetable organized by day

---

## Quick Command Reference

```bash
# Navigate to backend
cd backend

# Run migrations (Step 2)
npx prisma migrate dev --name init

# Generate client (Step 3)
npx prisma generate

# View database (Step 4 - optional)
npx prisma studio

# Start backend (Step 5)
npm run dev

# In another terminal - Start frontend (Step 6)
cd frontend
npm start
```

---

## Troubleshooting

### Error: "Can't reach database server"
- ✅ Check your Neon connection string is correct
- ✅ Make sure it includes `?sslmode=require`
- ✅ Verify your Neon project is not paused
- ✅ Check your internet connection

### Error: "Environment variable not found"
- ✅ Make sure `.env` file exists in `backend` folder
- ✅ Check that `DATABASE_URL` is spelled correctly
- ✅ No extra spaces around the `=` sign

### Error: "Migration failed"
- ✅ Your Neon database might need to be active (not paused)
- ✅ Check connection string format
- ✅ Try running `npx prisma generate` first

### Frontend shows errors when uploading
- ✅ Make sure backend is running (Step 5)
- ✅ Check backend terminal for errors
- ✅ Verify backend is on port 5000

### Can't see teachers after upload
- ✅ Check backend terminal for errors
- ✅ Verify database connection
- ✅ Try refreshing the page
- ✅ Check Prisma Studio to see if data was saved

---

## You're Done! 🎉

Once you see:
- ✅ Backend running on port 5000
- ✅ Frontend running on port 3000
- ✅ UI loads in browser
- ✅ Can upload and view timetables

**Your application is fully set up and working with Neon!**

---

## Next Steps (Optional)

### Set Up Email (Optional)
If you want to use the email feature:
1. Get Gmail App Password (see `README.md`)
2. Update `SMTP_USER` and `SMTP_PASSWORD` in `.env`
3. Restart backend server

### Deploy to Production
- Backend: Deploy to Vercel, Railway, or Render
- Frontend: Deploy to Vercel, Netlify, or GitHub Pages
- Database: Your Neon database works in production too!

---

## Summary Checklist

After Neon setup, you should:
- [ ] Updated `backend/.env` with Neon connection string
- [ ] Ran `npx prisma migrate dev --name init`
- [ ] Ran `npx prisma generate`
- [ ] Started backend: `npm run dev`
- [ ] Started frontend: `npm start`
- [ ] Tested uploading a timetable
- [ ] Tested viewing teacher timetables

**All set! Happy coding! 🚀**




