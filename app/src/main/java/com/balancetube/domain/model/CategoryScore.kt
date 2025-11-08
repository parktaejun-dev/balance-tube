package com.balancetube.domain.model

data class CategoryScore(
    val category: Category,
    val rawScore: Int, // Total duration in seconds
    val normalizedScore: Float // 0-100 scale
)

data class BalanceReport(
    val categoryScores: List<CategoryScore>,
    val lowestCategory: Category,
    val period: Period
)

enum class Period {
    LAST_7_DAYS,
    LAST_30_DAYS;

    fun getDaysCount(): Int = when (this) {
        LAST_7_DAYS -> 7
        LAST_30_DAYS -> 30
    }
}
