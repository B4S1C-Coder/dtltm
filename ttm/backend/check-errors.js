// Quick diagnostic script to check for common errors
const fs = require('fs');
const path = require('path');

console.log('=== Checking for Common Errors ===\n');

// Check .env file
const envPath = path.join(__dirname, '.env');
if (!fs.existsSync(envPath)) {
  console.log('❌ ERROR: .env file not found!');
  console.log('   Solution: Copy env.example to .env and update DATABASE_URL\n');
} else {
  console.log('✅ .env file exists');
  
  // Check DATABASE_URL
  require('dotenv').config();
  if (!process.env.DATABASE_URL) {
    console.log('❌ ERROR: DATABASE_URL not found in .env!');
    console.log('   Solution: Add DATABASE_URL to your .env file\n');
  } else {
    const dbUrl = process.env.DATABASE_URL;
    if (dbUrl.includes('username') || dbUrl.includes('password') || dbUrl.includes('localhost')) {
      console.log('⚠️  WARNING: DATABASE_URL appears to be a template');
      console.log('   Make sure you updated it with your actual Neon connection string\n');
    } else if (dbUrl.includes('neon.tech')) {
      console.log('✅ DATABASE_URL is set (appears to be Neon)');
    } else {
      console.log('✅ DATABASE_URL is set');
    }
  }
}

// Check required files
const requiredFiles = [
  'server.js',
  'routes/timetable.js',
  'routes/teachers.js',
  'prisma/schema.prisma'
];

console.log('\n=== Checking Required Files ===');
requiredFiles.forEach(file => {
  const filePath = path.join(__dirname, file);
  if (fs.existsSync(filePath)) {
    console.log(`✅ ${file}`);
  } else {
    console.log(`❌ ${file} - MISSING!`);
  }
});

// Check node_modules
const nodeModulesPath = path.join(__dirname, 'node_modules');
if (!fs.existsSync(nodeModulesPath)) {
  console.log('\n❌ ERROR: node_modules not found!');
  console.log('   Solution: Run "npm install" in the backend folder\n');
} else {
  console.log('\n✅ node_modules exists');
}

// Check Prisma Client
const prismaClientPath = path.join(__dirname, 'node_modules', '@prisma', 'client');
if (!fs.existsSync(prismaClientPath)) {
  console.log('\n❌ ERROR: Prisma Client not generated!');
  console.log('   Solution: Run "npx prisma generate"\n');
} else {
  console.log('✅ Prisma Client exists');
}

console.log('\n=== Checking Port ===');
const PORT = process.env.PORT || 5000;
console.log(`Server should run on port: ${PORT}`);

console.log('\n=== To see actual errors ===');
console.log('Run: node server.js');
console.log('Or: npm run dev');
console.log('\nIf you see errors, copy them and we can fix them!');




