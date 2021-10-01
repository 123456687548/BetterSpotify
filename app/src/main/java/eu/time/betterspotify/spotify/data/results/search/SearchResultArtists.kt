package eu.time.betterspotify.spotify.data.results.search

import eu.time.betterspotify.spotify.data.types.Artist

data class SearchResultArtists(
    val href: String,
    val items: List<Artist>,
    val limit: Int,
    val next: String,
    val offset: Int,
    val previous: Any,
    val total: Int
)