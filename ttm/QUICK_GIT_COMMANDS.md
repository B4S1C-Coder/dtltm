# Quick Git Commands for Publishing to GitHub

## If Git is Already Installed

Copy and paste these commands one by one in your terminal (in the project root `D:\dbms_prj`):

```bash
# 1. Initialize git repository
git init

# 2. Add all files
git add .

# 3. Make your first commit
git commit -m "Initial commit: Timetable Management System"

# 4. After creating repo on GitHub, add remote (replace YOUR_USERNAME and REPO_NAME)
git remote add origin https://github.com/YOUR_USERNAME/REPO_NAME.git

# 5. Push to GitHub
git branch -M main
git push -u origin main
```

## If Git is NOT Installed

1. **Install Git first:**
   - Download: https://git-scm.com/download/win
   - Run the installer
   - Restart Cursor/your terminal after installation

2. **Then run the commands above**

## Create GitHub Repository First

Before step 4 above, create the repository on GitHub:

1. Go to https://github.com
2. Click "+" → "New repository"
3. Name it (e.g., `timetable-management-system`)
4. Choose Public or Private
5. **DON'T** initialize with README
6. Click "Create repository"
7. Copy the repository URL and use it in step 4

## Authentication

When pushing, GitHub may ask for credentials:
- **Username**: Your GitHub username
- **Password**: Use a Personal Access Token (NOT your GitHub password)
  - Create token: https://github.com/settings/tokens
  - Select `repo` scope
  - Copy and use as password





