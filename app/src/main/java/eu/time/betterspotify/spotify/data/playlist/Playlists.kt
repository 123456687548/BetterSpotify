package eu.time.betterspotify.spotify.data.playlist

data class Playlists(
    val href: String,
    val items: List<Playlist>,
    val limit: Int,
    val next: String,
    val offset: Int,
    val previous: Any,
    val total: Int
)