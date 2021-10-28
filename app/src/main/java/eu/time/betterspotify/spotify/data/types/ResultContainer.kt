package eu.time.betterspotify.spotify.data.types

data class ResultContainer <T>(
    val href: String,
    val items: List<T>,
    val limit: Int,
    val next: String,
    val offset: Int,
    val previous: Any,
    val total: Int
)