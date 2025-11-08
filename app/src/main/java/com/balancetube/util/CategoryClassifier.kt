package com.balancetube.util

import com.balancetube.domain.model.Category

/**
 * Rule-based classifier for categorizing videos based on title and channel name
 */
object CategoryClassifier {

    // Channel whitelist - priority matching
    private val channelCategoryMap = mapOf(
        // Knowledge
        "Kurzgesagt" to Category.KNOWLEDGE,
        "Veritasium" to Category.KNOWLEDGE,
        "Vsauce" to Category.KNOWLEDGE,
        "TED" to Category.KNOWLEDGE,
        "TED-Ed" to Category.KNOWLEDGE,
        "CrashCourse" to Category.KNOWLEDGE,
        "National Geographic" to Category.KNOWLEDGE,
        "History Channel" to Category.KNOWLEDGE,
        "BBC" to Category.KNOWLEDGE,
        "Khan Academy" to Category.KNOWLEDGE,

        // Entertainment
        "Netflix" to Category.ENTERTAINMENT,
        "Comedy Central" to Category.ENTERTAINMENT,
        "SNL" to Category.ENTERTAINMENT,
        "The Tonight Show" to Category.ENTERTAINMENT,
        "CollegeHumor" to Category.ENTERTAINMENT,

        // Lifestyle
        "Bon Appétit" to Category.LIFESTYLE,
        "Tasty" to Category.LIFESTYLE,
        "Tastemade" to Category.LIFESTYLE,
        "Refinery29" to Category.LIFESTYLE,
        "Vogue" to Category.LIFESTYLE,

        // Arts & Music
        "NPR Music" to Category.ARTS_MUSIC,
        "VEVO" to Category.ARTS_MUSIC,
        "MTV" to Category.ARTS_MUSIC,
        "The Museum of Modern Art" to Category.ARTS_MUSIC,

        // Self-Improvement
        "Improvement Pill" to Category.SELF_IMPROVEMENT,
        "Matt D'Avella" to Category.SELF_IMPROVEMENT,
        "Thomas Frank" to Category.SELF_IMPROVEMENT,
        "Ali Abdaal" to Category.SELF_IMPROVEMENT,

        // Social/Creator
        "MrBeast" to Category.SOCIAL_CREATOR,
        "PewDiePie" to Category.SOCIAL_CREATOR,
        "Casey Neistat" to Category.SOCIAL_CREATOR
    )

    // Title keywords with scores
    private val keywordCategoryMap = mapOf(
        Category.KNOWLEDGE to listOf(
            "science", "history", "documentary", "lecture", "explained", "education",
            "tutorial", "learn", "how it works", "research", "study", "theory",
            "physics", "chemistry", "biology", "mathematics", "astronomy", "space",
            "ted talk", "lecture", "professor", "university", "academic"
        ),
        Category.ENTERTAINMENT to listOf(
            "funny", "comedy", "laugh", "hilarious", "meme", "prank", "challenge",
            "reaction", "gaming", "game", "play", "let's play", "walkthrough",
            "movie", "trailer", "review", "episode", "series", "show", "tv"
        ),
        Category.LIFESTYLE to listOf(
            "cooking", "recipe", "food", "travel", "vlog", "daily", "routine",
            "fashion", "style", "outfit", "makeup", "skincare", "beauty",
            "home", "diy", "craft", "design", "interior", "garden", "pet",
            "fitness", "workout", "exercise", "yoga", "health", "nutrition"
        ),
        Category.ARTS_MUSIC to listOf(
            "music", "song", "album", "concert", "live", "performance", "cover",
            "art", "painting", "drawing", "artist", "gallery", "museum",
            "dance", "ballet", "choreography", "opera", "theater", "film",
            "photography", "sculpture", "exhibition"
        ),
        Category.SELF_IMPROVEMENT to listOf(
            "productivity", "motivation", "self help", "personal development",
            "habits", "goals", "success", "mindfulness", "meditation", "focus",
            "discipline", "book review", "learning", "skills", "career",
            "business", "entrepreneurship", "leadership", "growth mindset"
        ),
        Category.SOCIAL_CREATOR to listOf(
            "vlog", "day in the life", "behind the scenes", "q&a", "qa",
            "storytime", "update", "announcement", "community", "fan",
            "meetup", "collab", "collaboration", "podcast", "interview",
            "personal", "my story", "life update", "chat", "talk"
        )
    )

    /**
     * Classify a video based on its title and channel name
     * Returns the category with the highest matching score
     */
    fun classify(title: String, channelTitle: String): Category {
        // Priority 1: Channel whitelist
        channelCategoryMap.entries.forEach { (channel, category) ->
            if (channelTitle.contains(channel, ignoreCase = true)) {
                return category
            }
        }

        // Priority 2: Keyword scoring
        val titleLower = title.lowercase()
        val categoryScores = mutableMapOf<Category, Int>()

        keywordCategoryMap.forEach { (category, keywords) ->
            var score = 0
            keywords.forEach { keyword ->
                if (titleLower.contains(keyword.lowercase())) {
                    score += 1
                }
            }
            if (score > 0) {
                categoryScores[category] = score
            }
        }

        // Return category with highest score
        val bestMatch = categoryScores.maxByOrNull { it.value }
        return bestMatch?.key ?: Category.ENTERTAINMENT // Default fallback
    }

    /**
     * Get search keywords for a specific category
     */
    fun getSearchKeywords(category: Category): List<String> {
        return when (category) {
            Category.KNOWLEDGE -> listOf(
                "science explained", "educational documentary", "ted talk",
                "how it works", "history explained"
            )
            Category.ENTERTAINMENT -> listOf(
                "funny videos", "comedy sketches", "entertainment",
                "best moments", "highlights"
            )
            Category.LIFESTYLE -> listOf(
                "lifestyle tips", "cooking tutorial", "travel guide",
                "home improvement", "daily routine"
            )
            Category.ARTS_MUSIC -> listOf(
                "music performance", "art tutorial", "museum tour",
                "classical music", "artistic inspiration"
            )
            Category.SELF_IMPROVEMENT -> listOf(
                "productivity tips", "personal development", "success habits",
                "motivation", "skill building"
            )
            Category.SOCIAL_CREATOR -> listOf(
                "vlog", "day in the life", "behind the scenes",
                "creator interview", "podcast"
            )
        }
    }
}
