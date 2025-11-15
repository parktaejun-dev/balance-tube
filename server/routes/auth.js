const router = require('express').Router();
const { google } = require('googleapis');
const db = require('../database'); // Import the database connection

const oauth2Client = new google.auth.OAuth2(
  process.env.GOOGLE_CLIENT_ID,
  process.env.GOOGLE_CLIENT_SECRET,
  process.env.OAUTH2_REDIRECT_URI
);

const scopes = [
  'https://www.googleapis.com/auth/youtube.readonly',
  'profile',
  'email'
];

// 1. Redirect user to Google's consent screen
router.get('/google', (req, res) => {
  const url = oauth2Client.generateAuthUrl({
    access_type: 'offline',
    scope: scopes,
    prompt: 'consent' // Important to get a refresh token every time
  });
  res.redirect(url);
});

// 2. Handle the callback from Google
router.get('/google/callback', async (req, res) => {
  const { code } = req.query;
  try {
    // Exchange authorization code for tokens
    const { tokens } = await oauth2Client.getToken(code);
    oauth2Client.setCredentials(tokens);

    // Get user's profile information from Google
    const oauth2 = google.oauth2({
      auth: oauth2Client,
      version: 'v2'
    });
    const { data } = await oauth2.userinfo.get();

    // --- Database Interaction ---
    // Check if user exists in our database
    const findUserStmt = db.prepare('SELECT * FROM users WHERE google_id = ?');
    let user = findUserStmt.get(data.id);

    let userId;
    if (user) {
      // If user exists, update their tokens and get their ID
      const updateUserStmt = db.prepare('UPDATE users SET access_token = ?, refresh_token = ? WHERE google_id = ?');
      updateUserStmt.run(tokens.access_token, tokens.refresh_token, data.id);
      userId = user.id;
      console.log(`User ${data.email} updated.`);
    } else {
      // If user does not exist, create a new user entry and get their ID
      const insertUserStmt = db.prepare('INSERT INTO users (google_id, email, name, picture, access_token, refresh_token) VALUES (?, ?, ?, ?, ?, ?)');
      const result = insertUserStmt.run(data.id, data.email, data.name, data.picture, tokens.access_token, tokens.refresh_token);
      userId = result.lastInsertRowid;
      console.log(`User ${data.email} created.`);
    }

    // Redirect back to the frontend with the user's database ID
    res.redirect(`http://localhost:5173/dashboard?userId=${userId}`);

  } catch (error) {
    console.error('Error during Google authentication callback:', error.message);
    res.redirect(`http://localhost:5173/?login_success=false`);
  }
});

// 3. Logout (Concept)
// In a real app with sessions/JWT, this would clear the session or token.
router.post('/logout', (req, res) => {
    // res.clearCookie('session_token'); // Example for cookie-based sessions
    res.status(200).json({ message: 'Logout successful.' });
});

// 4. Delete all user data
router.post('/delete-me', (req, res) => {
    // In a real app, you MUST get the user ID from a secure session/JWT, not the request body.
    const { userId } = req.body;
    if (!userId) {
        return res.status(400).send('User ID is required.');
    }
    try {
        // We use a transaction to ensure both deletions succeed or fail together.
        const deleteFn = db.transaction(() => {
            const deleteVideosStmt = db.prepare('DELETE FROM videos WHERE user_id = ?');
            deleteVideosStmt.run(userId);

            const deleteUserStmt = db.prepare('DELETE FROM users WHERE id = ?');
            deleteUserStmt.run(userId);
        });

        deleteFn();

        console.log(`All data for user ${userId} has been deleted.`);
        res.status(200).json({ message: 'All user data has been successfully deleted.' });
    } catch (error) {
        console.error(`Error deleting data for user ${userId}:`, error.message);
        res.status(500).send('Failed to delete user data.');
    }
});

module.exports = router;
