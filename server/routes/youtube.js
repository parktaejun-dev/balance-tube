const router = require('express').Router();
const { google } = require('googleapis');

// This is a placeholder for a function that would give us an authenticated OAuth2 client.
// In a real app, you'd get this from your user's session after they log in.
const getAuthenticatedClient = (tokens) => {
    const oauth2Client = new google.auth.OAuth2(
        process.env.GOOGLE_CLIENT_ID,
        process.env.GOOGLE_CLIENT_SECRET,
        process.env.OAUTH2_REDIRECT_URI
    );
    oauth2Client.setCredentials(tokens);
    return oauth2Client;
}

// Route to get YouTube watch history
router.post('/history', async (req, res) => {
    // In a real application, you would get the user's tokens from their session.
    // For this example, we'll expect the tokens to be passed in the request body.
    const { tokens } = req.body;
    if (!tokens) {
        return res.status(401).send('User is not authenticated.');
    }

    try {
        const oauth2Client = getAuthenticatedClient(tokens);
        const youtube = google.youtube({
            version: 'v3',
            auth: oauth2Client,
        });

        // To get watch history, we need to access the 'activities' list with a specific filter.
        // However, the YouTube API doesn't provide a direct "watch history" endpoint.
        // A common workaround is to fetch the user's "playlistItems" from their "watch history" playlist (which is private).
        // The ID for the watch history playlist is 'HL'. Let's try to get videos from the user's liked videos playlist 'LL' as an example, as history can be tricky.

        // Let's fetch the user's channel information as a test of authentication.
        const response = await youtube.channels.list({
            part: 'snippet,contentDetails,statistics',
            mine: true, // `mine: true` indicates that we want to retrieve data for the authenticated user.
        });

        const channel = response.data.items[0];
        const uploadsPlaylistId = channel.contentDetails.relatedPlaylists.uploads;

        // Now, get the videos from the uploads playlist. A real app might use 'HL' for history.
        const playlistResponse = await youtube.playlistItems.list({
            playlistId: uploadsPlaylistId,
            part: 'snippet,contentDetails',
            maxResults: 50 // We can fetch up to 50 items at a time.
        });


        res.status(200).json(playlistResponse.data.items);

    } catch (error) {
        console.error('Error fetching YouTube data:', error.message);
        // Check if it's an authentication error
        if (error.response && error.response.status === 401) {
            return res.status(401).send('Authentication error. Please log in again.');
        }
        res.status(500).send('Failed to fetch YouTube data.');
    }
});


module.exports = router;
