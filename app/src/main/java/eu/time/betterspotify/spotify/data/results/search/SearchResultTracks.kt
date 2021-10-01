package eu.time.betterspotify.spotify.data.results.search

import eu.time.betterspotify.spotify.data.types.Track

data class SearchResultTracks(
    val href: String,
    val items: List<Track>,
    val limit: Int,
    val next: String,
    val offset: Int,
    val previous: Any,
    val total: Int
)