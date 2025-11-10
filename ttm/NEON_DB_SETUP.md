# Using Neon Database Instead of Local PostgreSQL

Neon is a serverless PostgreSQL database - perfect for development and production! You don't need to install PostgreSQL locally.

## What is Neon?

- ☁️ **Cloud-based PostgreSQL** - No installation needed!
- 🆓 **Free tier available** - Perfect for projects
- ⚡ **Fast and scalable**
- 🔗 **PostgreSQL compatible** - Works with Prisma, same queries!

---

## Step-by-Step Setup

### Step 1: Create a Neon Account

1. Go to: **https://neon.tech**
2. Click **"Sign Up"** (you can use GitHub, Google, or email)
3. Verify your email if needed

### Step 2: Create a New Project

1. After logging in, click **"Create Project"**
2. Fill in:
   - **Project name**: `timetable-management` (or any name)
   - **Region**: Choose closest to you (for better performance)
   - **PostgreSQL version**: Latest (usually 16)
3. Click **"Create Project"**

### Step 3: Get Your Connection String

After creating the project, you'll see a dashboard. Look for:

1. **Connection Details** section
2. You'll see a connection string that looks like:
   ```
   postgresql://username:password@ep-xxx-xxx.region.aws.neon.tech/neondb?sslmode=require
   ```
3. Click **"Copy"** to copy the connection string

**Or manually build it:**
- **Host**: `ep-xxx-xxx.region.aws.neon.tech`
- **Database**: Usually `neondb` (default)
- **User**: Your username
- **Password**: Your password (shown once, save it!)
- **Port**: `5432`

### Step 4: Update Your `.env` File

1. In your `backend` folder, create/update `.env` file
2. Replace the `DATABASE_URL` with your Neon connection string:

```env
PORT=5000
DATABASE_URL="postgresql://username:password@ep-xxx-xxx.region.aws.neon.tech/neondb?sslmode=require"
NODE_ENV=development

# Email Configuration (SMTP) - Optional
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_SECURE=false
SMTP_USER=your-email@gmail.com
SMTP_PASSWORD=your-app-password
```

**Important**: 
- Replace the entire connection string with your Neon connection string
- The `sslmode=require` is important for Neon (secure connection)
- Make sure to copy the FULL connection string from Neon dashboard

### Step 5: Run Database Migrations

Now that you have Neon configured, create your database tables:

```bash
cd backend
npx prisma migrate dev --name init
```

This will:
- Connect to your Neon database
- Create all tables (teachers, subjects, rooms, slots)
- Set up the schema

Then generate Prisma client:

```bash
npx prisma generate
```

### Step 6: Verify Connection

You can verify it's working:

```bash
npx prisma studio
```

This will open Prisma Studio in your browser where you can see your database tables!

---

## Advantages of Using Neon

✅ **No Installation Required** - Works immediately
✅ **Accessible Anywhere** - Not just on your computer
✅ **Easy Sharing** - Share connection with teammates
✅ **Automatic Backups** - Built-in backup system
✅ **Free Tier** - Great for development
✅ **Production Ready** - Can use for production too!

---

## Troubleshooting

### Connection Error: "SSL is required"

Make sure your connection string includes `?sslmode=require` at the end:
```
postgresql://user:pass@host/db?sslmode=require
```

### Connection Error: "Database does not exist"

The default database name in Neon is usually `neondb` or `main`. Check your Neon dashboard for the correct database name.

### Can't Find Connection String

1. Go to your Neon dashboard
2. Click on your project
3. Look for **"Connection Details"** or **"Connect"** button
4. Copy the connection string from there

### Password Issues

- Neon shows the password only once during setup
- If you forgot it, you can reset it in Neon dashboard
- Go to: Project → Settings → Reset Password

---

## Migration from Local PostgreSQL

If you already set up local PostgreSQL and want to switch to Neon:

1. Create Neon account and project (Steps 1-3 above)
2. Update `DATABASE_URL` in `.env` with Neon connection string
3. Run migrations:
   ```bash
   npx prisma migrate deploy
   ```
4. Or start fresh:
   ```bash
   npx prisma migrate dev --name init
   ```

---

## Security Notes

⚠️ **Never commit your `.env` file to GitHub!**

Your `.env` file should already be in `.gitignore`, but double-check:
- `.env` should NOT be committed
- Only share connection strings with trusted team members
- Neon allows you to create separate branches for dev/prod

---

## Next Steps

After setting up Neon:

1. ✅ Neon account created
2. ✅ Project created
3. ✅ Connection string in `.env`
4. ✅ Migrations run (`npx prisma migrate dev --name init`)
5. ✅ Prisma client generated (`npx prisma generate`)
6. ✅ Start backend: `npm run dev`
7. ✅ Start frontend: `npm start`

You're ready to go! 🚀

---

## Need Help?

- **Neon Documentation**: https://neon.tech/docs
- **Neon Dashboard**: https://console.neon.tech
- **Community**: https://neon.tech/community




