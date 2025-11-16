require('dotenv').config();
const express = require('express');
const cors = require('cors');

const app = express();
const port = process.env.PORT || 3001;

// Middleware
app.use(cors({
  origin: 'http://localhost:5173'
}));
app.use(express.json());

// Routes
const authRoutes = require('./routes/auth');
const youtubeRoutes = require('./routes/youtube');
const analysisRoutes = require('./routes/analysis');
const recommendationRoutes = require('./routes/recommendations');
app.use('/auth', authRoutes);
app.use('/api/youtube', youtubeRoutes);
app.use('/api/analysis', analysisRoutes);
app.use('/api/recommendations', recommendationRoutes);

// A simple test route
app.get('/api/hello', (req, res) => {
  res.json({ message: 'Hello from the BalanceTube server!' });
});

app.listen(port, () => {
  console.log(`Server is running on http://localhost:${port}`);
});
