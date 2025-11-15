const Database = require('better-sqlite3');
const path = require('path');
const fs = require('fs');

const dbPath = path.join(__dirname, '../balancetube.db'); // Create DB in the root folder
const dbExists = fs.existsSync(dbPath);

// Ensure the database file exists before trying to connect
if (!dbExists) {
    console.log('Database file not found, creating a new one in the project root...');
}

const db = new Database(dbPath, { verbose: console.log });

// --- Schema Initialization ---
function initializeSchema() {
    console.log('Initializing database schema...');

    const createUserTable = `
    CREATE TABLE IF NOT EXISTS users (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        google_id TEXT UNIQUE NOT NULL,
        email TEXT UNIQUE NOT NULL,
        name TEXT,
        picture TEXT,
        access_token TEXT,
        refresh_token TEXT,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP
    );`;

    const createVideosTable = `
    CREATE TABLE IF NOT EXISTS videos (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        user_id INTEGER NOT NULL,
        video_id TEXT NOT NULL,
        title TEXT,
        published_at DATETIME,
        category TEXT,
        analyzed_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (user_id) REFERENCES users (id),
        UNIQUE (user_id, video_id)
    );`;

    db.exec(createUserTable);
    db.exec(createVideosTable);

    console.log('Database schema initialized successfully.');
}

initializeSchema();

// Close the DB connection when the app is closing
process.on('exit', () => db.close());
process.on('SIGHUP', () => process.exit(128 + 1));
process.on('SIGINT', () => process.exit(128 + 2));
process.on('SIGTERM', () => process.exit(128 + 15));

module.exports = db;
