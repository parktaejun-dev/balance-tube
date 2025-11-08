package com.balancetube.domain.model

enum class Category(val displayName: String) {
    KNOWLEDGE("Knowledge"),
    ENTERTAINMENT("Entertainment"),
    LIFESTYLE("Lifestyle"),
    ARTS_MUSIC("Arts & Music"),
    SELF_IMPROVEMENT("Self-Improvement"),
    SOCIAL_CREATOR("Social / Creator");

    companion object {
        fun fromString(value: String?): Category {
            return values().find { it.name == value } ?: ENTERTAINMENT
        }
    }
}
