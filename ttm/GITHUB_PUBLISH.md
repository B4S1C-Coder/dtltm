# How to Publish to GitHub

Follow these steps to publish your Timetable Management System to GitHub:

## Prerequisites

1. **Install Git** (if not already installed)
   - Download from: https://git-scm.com/download/win
   - During installation, make sure to select "Git from the command line and also from 3rd-party software"

2. **Create a GitHub account** (if you don't have one)
   - Go to: https://github.com
   - Sign up for a free account

## Step-by-Step Instructions

### Step 1: Initialize Git Repository

Open a terminal in your project folder (`D:\dbms_prj`) and run:

```bash
git init
```

### Step 2: Add All Files to Git

```bash
git add .
```

### Step 3: Create Initial Commit

```bash
git commit -m "Initial commit: Timetable Management System with MERN stack"
```

### Step 4: Create Repository on GitHub

1. Go to https://github.com and sign in
2. Click the **"+"** icon in the top right corner
3. Select **"New repository"**
4. Fill in:
   - **Repository name**: `timetable-management-system` (or any name you prefer)
   - **Description**: "University Timetable Management System built with MERN stack, PostgreSQL, and Prisma"
   - **Visibility**: Choose Public or Private
   - **DO NOT** initialize with README, .gitignore, or license (we already have these)
5. Click **"Create repository"**

### Step 5: Connect Local Repository to GitHub

After creating the repository, GitHub will show you commands. Use these (replace `YOUR_USERNAME` with your GitHub username):

```bash
git remote add origin https://github.com/YOUR_USERNAME/timetable-management-system.git
git branch -M main
git push -u origin main
```

**Note:** If you're asked for credentials:
- Username: Your GitHub username
- Password: Use a **Personal Access Token** (not your GitHub password)
  - To create one: GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic) → Generate new token
  - Give it `repo` permissions

### Alternative: Using GitHub CLI

If you have GitHub CLI installed:

```bash
gh repo create timetable-management-system --public --source=. --remote=origin --push
```

## Quick Command Reference

```bash
# Initialize repository
git init

# Add all files
git add .

# Commit changes
git commit -m "Your commit message"

# Add remote (only once)
git remote add origin https://github.com/YOUR_USERNAME/REPO_NAME.git

# Push to GitHub
git push -u origin main

# For future updates
git add .
git commit -m "Updated feature description"
git push
```

## Important Notes

✅ The `.gitignore` file is already set up to exclude:
- `node_modules/` (don't commit dependencies)
- `.env` files (keep your secrets safe!)
- `uploads/` folder
- Build files

⚠️ **NEVER commit:**
- `.env` files with real passwords
- `node_modules/` folder
- Database files
- Personal access tokens

## Adding a GitHub Actions Workflow (Optional)

If you want to set up CI/CD later, you can add a `.github/workflows` folder with workflows for testing, building, etc.

## Need Help?

- Git documentation: https://git-scm.com/doc
- GitHub Guides: https://guides.github.com
- Git troubleshooting: Check that Git is installed and in your PATH





