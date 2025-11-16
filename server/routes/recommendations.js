const router = require('express').Router();
const { google } = require('googleapis');
const db = require('../database');

// This is a placeholder for a function that would give us a client that doesn't require user tokens
// for general searches. We'll use an API key for this.
const getYoutubeClient = () => {
    return google.youtube({
        version: 'v3',
        auth: process.env.GEMINI_API_KEY, // Using a generic API key for public searches
    });
}

// Route to get video recommendations for a user
router.get('/:userId', async (req, res) => {
    const { userId } = req.params;

    try {
        // 1. Find the user's least watched category from the database
        const stmt = db.prepare(`
            SELECT category, COUNT(*) as count
            FROM videos
            WHERE user_id = ?
            GROUP BY category
            ORDER BY count ASC
            LIMIT 1
        `);
        const result = stmt.get(userId);

        // If no data is found, return an empty but valid response
        if (!result) {
            return res.status(200).json({
                leastWatchedCategory: 'N/A',
                recommendations: [],
            });
        }

        const leastWatchedCategory = result.category;

        // 2. Search for videos in that category using the YouTube API
        const youtube = getYoutubeClient();
        const searchResponse = await youtube.search.list({
            part: 'snippet',
            q: `introduction to ${leastWatchedCategory}`, // Search for beginner-friendly content
            type: 'video',
            videoDuration: 'medium', // 'medium' is for videos between 4 and 20 minutes
            maxResults: 10,
        });

        res.status(200).json({
            leastWatchedCategory: leastWatchedCategory,
            recommendations: searchResponse.data.items,
        });

    } catch (error) {
        console.error('Error generating recommendations:', error.message);
        res.status(500).send('Failed to generate recommendations.');
    }
});

module.exports = router;
