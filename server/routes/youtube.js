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
        refresh_token: user.refresh_token,
    });

    // Listen for token refresh events.
    // The googleapis library automatically uses the refresh token to get a new
    // access token when it's expired. This event allows us to capture that new token.
    oauth2Client.on('tokens', (tokens) => {
        if (tokens.access_token) {
            console.log('--- DEBUG: Access token was refreshed. Updating database. ---');
            const updateTokenStmt = db.prepare(
                'UPDATE users SET access_token = ? WHERE id = ?'
            );
            updateTokenStmt.run(tokens.access_token, userId);
        }
    });

    return oauth2Client;
};

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

        console.log("--- DEBUG: Fetching channel details to find watch history playlist ---");
        const channelResponse = await youtube.channels.list({
            part: 'contentDetails',
            mine: true,
        });

        const relatedPlaylists = channelResponse.data.items[0]?.contentDetails?.relatedPlaylists;
        if (!relatedPlaylists) {
            console.error("--- DEBUG: Could not find relatedPlaylists for the user's channel. ---");
            return res.status(404).send('Could not find channel details for the user.');
        }

        // The watch history playlist is special and often referred to by the alias 'HL'.
        // We log the returned playlists for debugging but will use 'HL' as it's the standard.
        console.log("--- DEBUG: Full relatedPlaylists object ---");
        console.log(JSON.stringify(relatedPlaylists, null, 2));
        console.log("-----------------------------------------");

        const historyPlaylistId = 'HL'; // 'HL' is the de-facto standard ID for watch history.

        console.log(`--- DEBUG: Attempting to fetch watch history with playlistId: '${historyPlaylistId}' ---`);
        const playlistResponse = await youtube.playlistItems.list({
            playlistId: historyPlaylistId,
            part: 'snippet,contentDetails',
            maxResults: 50,
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
