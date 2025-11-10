# How to Fix Gmail SMTP Error - Step by Step Guide

## The Error You're Seeing

```
535-5.7.8 Username and Password not accepted
```

This means Gmail rejected your login credentials. Here's how to fix it.

---

## Solution: Use Gmail App Password

Gmail doesn't allow regular passwords for SMTP. You need to use an **App Password**.

### Step 1: Enable 2-Factor Authentication

1. Go to your Google Account: https://myaccount.google.com
2. Click **Security** (left sidebar)
3. Under **"Signing in to Google"**, find **"2-Step Verification"**
4. Click **"2-Step Verification"** and follow the setup process
5. Enable 2-factor authentication (required for App Passwords)

### Step 2: Generate App Password

1. Go back to: https://myaccount.google.com/security
2. Under **"Signing in to Google"**, find **"App passwords"**
   - If you don't see it, make sure 2-Step Verification is enabled
3. Click **"App passwords"**
4. You might need to sign in again
5. Select **"Mail"** as the app type
6. Select **"Other (Custom name)"** as device
7. Type: `Timetable Management System` (or any name)
8. Click **"Generate"**
9. **Copy the 16-character password** (shown only once!)
   - Looks like: `abcd efgh ijkl mnop`
   - Remove spaces when using: `abcdefghijklmnop`

### Step 3: Update `.env` File

1. Open `backend/.env` file in VS Code
2. Update these lines:

```env
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_SECURE=false
SMTP_USER=your-email@gmail.com
SMTP_PASSWORD=abcdefghijklmnop
```

**Important:**
- `SMTP_USER`: Your full Gmail address (e.g., `john@gmail.com`)
- `SMTP_PASSWORD`: The 16-character App Password (no spaces)
- Use the App Password, NOT your regular Gmail password

### Step 4: Restart Backend Server

After updating `.env`, restart your backend:

1. Stop the backend (if running): Press `Ctrl + C` in the backend terminal
2. Start it again:
   ```bash
   cd backend
   npm run dev
   ```

### Step 5: Test Email

1. Go to your app: `http://localhost:3000`
2. Select a teacher from dropdown
3. Click **"Send Timetable via Email"**
4. Should see success message (no more error!)

---

## Quick Checklist

- [ ] 2-Step Verification enabled on Gmail
- [ ] App Password generated (16 characters)
- [ ] `.env` file updated with App Password (no spaces)
- [ ] Backend server restarted
- [ ] Email test successful

---

## Common Issues

### Issue 1: "App passwords" option not showing

**Solution:** Make sure 2-Step Verification is enabled first. It's a prerequisite.

### Issue 2: Still getting authentication error

**Check:**
- ✅ App Password copied correctly (no spaces)
- ✅ Using full email address in `SMTP_USER`
- ✅ Backend restarted after updating `.env`
- ✅ Not using regular Gmail password

### Issue 3: "Less secure app access" error

**Solution:** You don't need "Less secure app access" anymore. Use App Passwords instead (more secure).

### Issue 4: Works but emails go to spam

**Solution:** 
- This is normal for first-time senders
- Check spam folder
- Gmail may need time to trust your app
- Consider using a dedicated email service for production

---

## Alternative: Use a Different Email Provider

If you don't want to use Gmail, you can use other providers:

### Outlook/Hotmail

```env
SMTP_HOST=smtp-mail.outlook.com
SMTP_PORT=587
SMTP_SECURE=false
SMTP_USER=your-email@outlook.com
SMTP_PASSWORD=your-password
```

### Yahoo

```env
SMTP_HOST=smtp.mail.yahoo.com
SMTP_PORT=587
SMTP_SECURE=false
SMTP_USER=your-email@yahoo.com
SMTP_PASSWORD=app-password
```

### SendGrid (For Production)

For production apps, consider using SendGrid, Mailgun, or similar services.

---

## Security Notes

⚠️ **Important:**
- Never commit `.env` file to GitHub
- App Passwords are safer than regular passwords
- Each app should have its own App Password
- You can revoke App Passwords anytime from Google Account settings

---

## Testing Without Email

If you just want to test the app without email:

1. The email feature is **optional**
2. All other features work without email
3. You can test uploading and viewing timetables
4. Set up email later when needed

---

## Summary

The error happens because Gmail requires App Passwords for SMTP. Follow these steps:

1. Enable 2-Step Verification
2. Generate App Password
3. Update `.env` with App Password
4. Restart backend
5. Test again

That's it! Your email feature should work now. 🎉




