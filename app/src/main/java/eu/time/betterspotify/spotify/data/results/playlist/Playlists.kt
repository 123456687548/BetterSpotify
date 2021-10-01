package eu.time.betterspotify.spotify.data.results.playlist

import eu.time.betterspotify.spotify.data.types.Playlist

data class Playlists(
    val href: String,
    val items: List<Playlist>,
    val limit: Int,
    val next: String,
    val offset: Int,
    val previous: Any,
    val total: Int
)