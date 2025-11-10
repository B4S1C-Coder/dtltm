# How to Run in Visual Studio Code

## Quick Setup in VS Code

### Step 1: Open Project in VS Code

1. Open Visual Studio Code
2. Go to **File** → **Open Folder**
3. Select your project folder: `D:\dbms_prj`
4. Click **Select Folder**

### Step 2: Open Terminal in VS Code

You can open the integrated terminal in several ways:
- **Keyboard Shortcut**: `` Ctrl + ` `` (backtick key, usually above Tab)
- **Menu**: **Terminal** → **New Terminal**
- **View Menu**: **View** → **Terminal**

---

## Method 1: Using VS Code Terminal (Recommended)

### Running Backend and Frontend Separately

You'll need **TWO terminal windows** in VS Code:

#### Terminal 1 - Backend Server

1. Open a new terminal (`` Ctrl + ` `` or **Terminal** → **New Terminal**)
2. Run these commands:

```bash
cd backend
npm run dev
```

You should see: `Server is running on port 5000`

#### Terminal 2 - Frontend Server

1. Click the **"+"** button next to the terminal tab to create a new terminal
2. Or press `` Ctrl + Shift + ` `` for a new terminal
3. Run these commands:

```bash
cd frontend
npm start
```

You should see: `Compiled successfully!` and the browser will open to `http://localhost:3000`

---

## Method 2: Using VS Code Tasks (Automatic)

I've created task configurations for you! Follow these steps:

### Run Both Servers at Once

1. Press **`Ctrl + Shift + P`** (Command Palette)
2. Type: **"Tasks: Run Task"**
3. Select: **"Run Backend & Frontend"**

This will automatically start both servers in separate terminal panels!

### Run Individual Tasks

1. Press **`Ctrl + Shift + P`**
2. Type: **"Tasks: Run Task"**
3. Choose:
   - **"npm: start:backend"** - Run only backend
   - **"npm: start:frontend"** - Run only frontend

---

## Method 3: Using Debug Configuration

For debugging with breakpoints:

1. Press **`F5`** or go to **Run** → **Start Debugging**
2. Select **"Launch Backend"** from the dropdown
3. Set breakpoints in your backend code
4. The debugger will stop at breakpoints

---

## VS Code Tips

### Multiple Terminals

- **New Terminal**: `` Ctrl + Shift + ` ``
- **Split Terminal**: Click the split icon in terminal panel
- **Switch Terminals**: Click on terminal tabs

### Terminal Navigation

- **Backend terminal**: Usually shows `PS D:\dbms_prj\backend>`
- **Frontend terminal**: Usually shows `PS D:\dbms_prj\frontend>`

### Stopping Servers

- Click in the terminal window
- Press **`Ctrl + C`** to stop the running server
- Press **`Ctrl + C`** again if needed to confirm

---

## Recommended VS Code Extensions

Install these extensions for better development:

1. **ES7+ React/Redux/React-Native snippets** - React code snippets
2. **Prettier - Code formatter** - Auto-format code
3. **ESLint** - Code linting
4. **Prisma** - Prisma schema syntax highlighting
5. **DotENV** - .env file syntax highlighting
6. **Thunder Client** or **REST Client** - Test API endpoints

To install:
- Press **`Ctrl + Shift + X`** (Extensions)
- Search for extension name
- Click **Install**

---

## Quick Reference

| Action | Command/Shortcut |
|--------|------------------|
| Open Terminal | `` Ctrl + ` `` |
| New Terminal | `` Ctrl + Shift + ` `` |
| Run Task | `Ctrl + Shift + P` → "Tasks: Run Task" |
| Start Debugging | `F5` |
| Stop Server | `Ctrl + C` |
| Split Terminal | Click split icon in terminal panel |

---

## Troubleshooting

### Terminal Shows Wrong Directory
- Type `cd D:\dbms_prj\backend` (or `cd D:\dbms_prj\frontend`)
- Or use the folder icon in terminal to select directory

### Port Already in Use
- Backend: Change `PORT=5000` to another port in `backend/.env`
- Frontend: React will ask to use a different port automatically

### Can't See Terminal
- Press `` Ctrl + ` `` to toggle terminal visibility
- Or go to **View** → **Terminal**

### Tasks Not Showing
- Make sure you're in the root folder (`D:\dbms_prj`)
- Restart VS Code
- Check that `.vscode/tasks.json` file exists

---

## Summary

**Easiest Method:**
1. Open project in VS Code
2. Press `` Ctrl + ` `` to open terminal
3. In Terminal 1: `cd backend && npm run dev`
4. Click "+" for Terminal 2: `cd frontend && npm start`
5. Wait for both to compile/start
6. Open `http://localhost:3000` in browser

Happy coding! 🚀




