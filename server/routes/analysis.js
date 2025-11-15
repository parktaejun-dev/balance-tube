const router = require('express').Router();
const { GoogleGenerativeAI } = require('@google/generative-ai');
const db = require('../database');

// Initialize the Google Generative AI client
const genAI = new GoogleGenerativeAI(process.env.GEMINI_API_KEY);

const classificationPrompt = `
    Analyze the provided YouTube video title and description to classify it into one of the following 6 categories:
    'Knowledge', 'Entertainment', 'Lifestyle', 'Art/Music', 'Self-Development', 'Social/Creator'.

    Respond with ONLY the category name and nothing else.

    Title: {title}
    Description: {description}
`;

// Route to analyze a list of videos
router.post('/run', async (req, res) => {
    // TODO: Get user ID from session/JWT
    const { userId, videos } = req.body; // Expecting { userId: 1, videos: [...] }

    if (!userId || !videos || !Array.isArray(videos)) {
        return res.status(400).send('Invalid request body. Expecting userId and a videos array.');
    }

    try {
        const model = genAI.getGenerativeModel({ model: "gemini-pro" });

        const analysisPromises = videos.map(async (video) => {
            const prompt = classificationPrompt
                .replace('{title}', video.snippet.title)
                .replace('{description}', video.snippet.description);

            try {
                const result = await model.generateContent(prompt);
                const response = await result.response;
                const category = response.text().trim();

                // Save the result to the database
                const stmt = db.prepare('INSERT OR IGNORE INTO videos (user_id, video_id, title, published_at, category) VALUES (?, ?, ?, ?, ?)');
                stmt.run(userId, video.id, video.snippet.title, video.snippet.publishedAt, category);

                return { videoId: video.id, category: category };
            } catch (apiError) {
                console.error(`Error classifying video ${video.id}:`, apiError);
                return { videoId: video.id, category: 'Unclassified' };
            }
        });

        const results = await Promise.all(analysisPromises);
        res.status(200).json({ message: 'Analysis complete!', results: results });

    } catch (error) {
        console.error('Error during video analysis:', error);
        res.status(500).send('Failed to analyze videos.');
    }
});

// Route to get the analysis result for a user
router.get('/result/:userId', (req, res) => {
    const { userId } = req.params;
    try {
        const stmt = db.prepare(`
            SELECT category, COUNT(*) as count
            FROM videos
            WHERE user_id = ?
            GROUP BY category
        `);
        const results = stmt.all(userId);

        // Format the data for the radar chart
        const formattedResults = {
            'Knowledge': 0,
            'Entertainment': 0,
            'Lifestyle': 0,
            'Art/Music': 0,
            'Self-Development': 0,
            'Social/Creator': 0,
        };
        results.forEach(row => {
            if (formattedResults.hasOwnProperty(row.category)) {
                formattedResults[row.category] = row.count;
            }
        });

        res.status(200).json(formattedResults);
    } catch (error) {
        console.error('Error fetching analysis result:', error);
        res.status(500).send('Failed to fetch analysis result.');
    }
});

module.exports = router;
