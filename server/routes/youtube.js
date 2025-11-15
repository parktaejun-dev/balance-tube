const router = require('express').Router();
const { google } = require('googleapis');
const db = require('../database');

// Gets an authenticated OAuth2 client for a specific user from the database
const getAuthenticatedClient = (userId) => {
    const stmt = db.prepare('SELECT access_token, refresh_token FROM users WHERE id = ?');
    const user = stmt.get(userId);
    if (!user) {
        throw new Error('User not found');
    }

    const oauth2Client = new google.auth.OAuth2(
        process.env.GOOGLE_CLIENT_ID,
        process.env.GOOGLE_CLIENT_SECRET,
        process.env.OAUTH2_REDIRECT_URI
    );
    oauth2Client.setCredentials({
        access_token: user.access_token,
        refresh_token: user.refresh_token
    });
    return oauth2Client;
}

// Route to get the user's actual YouTube watch history
router.get('/history/:userId', async (req, res) => {
    const { userId } = req.params;
    if (!userId) {
        return res.status(400).send('User ID is required.');
    }

    try {
        const oauth2Client = getAuthenticatedClient(userId);
        const youtube = google.youtube({
            version: 'v3',
            auth: oauth2Client,
        });

        console.log("--- DEBUG: Attempting to fetch watch history (playlistId: 'HL') ---");
        const playlistResponse = await youtube.playlistItems.list({
            playlistId: 'HL',
            part: 'snippet,contentDetails',
            maxResults: 50
        });

        // --- Start of new debug logging ---
        console.log("--- DEBUG: Full response from YouTube API ---");
        console.log(JSON.stringify(playlistResponse, null, 2));
        console.log("---------------------------------------------");
        // --- End of new debug logging ---

        res.status(200).json(playlistResponse.data.items);

    } catch (error) {
        // --- Enhanced error logging ---
        console.error("--- DEBUG: An error occurred while fetching history ---");
        console.error("Error Message:", error.message);
        if (error.response) {
            console.error("Error Status:", error.response.status);
            console.error("Error Data:", JSON.stringify(error.response.data, null, 2));
        }
        console.error("----------------------------------------------------");
        // --- End of enhanced error logging ---

        if (error.message.includes('User not found')) {
            return res.status(404).send('User not found.');
        }
        res.status(500).send('Failed to fetch YouTube watch history.');
    }
});

module.exports = router;
