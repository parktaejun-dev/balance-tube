package com.balancetube.util

/**
 * Parses ISO 8601 duration format (e.g., PT1H2M10S) to seconds
 */
object DurationParser {

    fun parseIso8601Duration(duration: String): Int {
        if (duration.isEmpty() || !duration.startsWith("PT")) {
            return 0
        }

        var seconds = 0
        var currentNumber = ""

        // Remove PT prefix
        val durationStr = duration.substring(2)

        for (char in durationStr) {
            when {
                char.isDigit() -> {
                    currentNumber += char
                }
                char == 'H' -> {
                    seconds += (currentNumber.toIntOrNull() ?: 0) * 3600
                    currentNumber = ""
                }
                char == 'M' -> {
                    seconds += (currentNumber.toIntOrNull() ?: 0) * 60
                    currentNumber = ""
                }
                char == 'S' -> {
                    seconds += currentNumber.toIntOrNull() ?: 0
                    currentNumber = ""
                }
            }
        }

        return seconds
    }

    fun formatDuration(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60

        return when {
            hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, secs)
            else -> String.format("%d:%02d", minutes, secs)
        }
    }
}
