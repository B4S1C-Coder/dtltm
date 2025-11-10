# PostgreSQL Installation Guide for Windows

Complete step-by-step guide to install PostgreSQL on Windows.

## Method 1: Official Installer (Recommended)

### Step 1: Download PostgreSQL

1. Go to the official PostgreSQL website:
   - **Direct Link**: https://www.postgresql.org/download/windows/
   - Or visit: https://www.postgresql.org/download/
   - Click **"Download the installer"**
   - Choose **"Windows x86-64"** (for 64-bit Windows) or **"Windows x86-32"** (for 32-bit)

2. You'll be redirected to EnterpriseDB (official installer provider)
   - Click **"Download"** for the latest version (PostgreSQL 16 or 15)
   - The file will be named something like: `postgresql-16.x-windows-x64.exe`

### Step 2: Run the Installer

1. **Double-click** the downloaded `.exe` file
2. If you see a security warning, click **"Run"** or **"Yes"**

3. **Installation Wizard Steps:**
   - Click **"Next"**
   - **Installation Directory**: Leave default (`C:\Program Files\PostgreSQL\16`) or choose your own
   - Click **"Next"**
   - **Select Components**: Make sure these are checked:
     - ✅ PostgreSQL Server
     - ✅ pgAdmin 4 (GUI tool - very useful!)
     - ✅ Stack Builder (optional, for additional tools)
     - ✅ Command Line Tools (important!)
   - Click **"Next"**
   - **Data Directory**: Leave default or choose your own
   - Click **"Next"**

### Step 3: Set Up PostgreSQL Account

1. **Superuser Password**:
   - Enter a password for the `postgres` user (the database administrator)
   - **⚠️ IMPORTANT**: Remember this password! You'll need it to connect.
   - Recommended: Use a strong password you can remember
   - Click **"Next"**

2. **Port**: Leave default **5432** (click **"Next"**)

3. **Advanced Options**:
   - **Locale**: Leave default (usually fine)
   - Click **"Next"**

4. **Ready to Install**: Click **"Next"** to begin installation

5. **Installing**: Wait for installation to complete (takes 2-5 minutes)

6. **Completing Setup**: 
   - **Stack Builder**: You can uncheck this (not needed for basic use)
   - Click **"Finish"**

### Step 4: Verify Installation

1. **Check Windows Services**:
   - Press `Windows Key + R`
   - Type: `services.msc` and press Enter
   - Look for **"postgresql-x64-16"** (or similar) - should be **Running**

2. **Test Command Line** (Optional):
   - Open Command Prompt or PowerShell
   - Type: `psql --version`
   - You should see version number (e.g., `psql (PostgreSQL) 16.1`)

---

## Method 2: Using Chocolatey (If You Have It)

If you have Chocolatey package manager installed:

```powershell
choco install postgresql
```

---

## Method 3: Using winget (Windows Package Manager)

If you have winget installed:

```powershell
winget install PostgreSQL.PostgreSQL
```

---

## After Installation

### Start PostgreSQL Service

PostgreSQL should start automatically, but if it doesn't:

1. Press `Windows Key + R`
2. Type: `services.msc`
3. Find **"postgresql-x64-16"** (or similar)
4. Right-click → **Start**

Or set it to start automatically:
- Right-click the service → **Properties**
- Set **Startup type** to **Automatic**

---

## Access PostgreSQL

### Option 1: Using pgAdmin 4 (GUI - Easiest)

1. Open **pgAdmin 4** from Start Menu
2. It will ask you to set a master password (first time only)
3. Enter a password and remember it
4. Connect to server:
   - Left sidebar → **Servers** → **PostgreSQL 16**
   - Enter password (the one you set during installation)
   - Click **OK**

### Option 2: Using Command Line (psql)

1. Open **Command Prompt** or **PowerShell**
2. Navigate to PostgreSQL bin folder (or use full path):
   ```bash
   cd "C:\Program Files\PostgreSQL\16\bin"
   ```
3. Connect to PostgreSQL:
   ```bash
   psql -U postgres
   ```
4. Enter your password when prompted

**Or add to PATH** (recommended):
1. Search "Environment Variables" in Windows
2. Edit **System** → **Path** variable
3. Add: `C:\Program Files\PostgreSQL\16\bin`
4. Restart terminal

Then you can use `psql` from anywhere!

---

## Create Database for Your Project

### Using pgAdmin 4:

1. Open pgAdmin 4
2. Connect to server (enter password)
3. Expand **Servers** → **PostgreSQL 16** → **Databases**
4. Right-click **Databases** → **Create** → **Database**
5. **Name**: `timetable_db`
6. Click **Save**

### Using Command Line:

```bash
psql -U postgres
# Enter password when prompted

CREATE DATABASE timetable_db;

\q  # Exit psql
```

---

## Configuration for Your Project

After installation, update your `backend/.env` file:

```env
DATABASE_URL="postgresql://postgres:YOUR_PASSWORD@localhost:5432/timetable_db?schema=public"
```

Replace `YOUR_PASSWORD` with the password you set during installation.

---

## Troubleshooting

### PostgreSQL Service Not Running
- Open **Services** (`services.msc`)
- Find PostgreSQL service
- Right-click → **Start**

### Can't Connect - Wrong Password
- You need the password you set during installation
- If forgotten, you may need to reset it (search "reset postgres password windows")

### Port 5432 Already in Use
- Another PostgreSQL instance might be running
- Change port in installation, or stop other instance

### psql Command Not Found
- Add PostgreSQL bin folder to PATH (see instructions above)
- Or use full path: `"C:\Program Files\PostgreSQL\16\bin\psql.exe"`

### Firewall Blocking
- Windows Firewall may block PostgreSQL
- Allow PostgreSQL through firewall when prompted
- Or manually add exception for port 5432

---

## Quick Test

After installation, test your setup:

1. Open pgAdmin 4
2. Connect to server (password: your installation password)
3. You should see the server in the left sidebar
4. You're ready to create databases!

---

## Need Help?

- **Official Docs**: https://www.postgresql.org/docs/
- **pgAdmin Docs**: https://www.pgadmin.org/docs/
- **PostgreSQL Community**: https://www.postgresql.org/community/

---

## Next Steps After Installation

1. ✅ PostgreSQL installed
2. ✅ Create database `timetable_db` (see above)
3. ✅ Update `backend/.env` with your password
4. ✅ Run `npx prisma migrate dev --name init` in backend folder
5. ✅ Start your backend server!

Happy coding! 🐘




