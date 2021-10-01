package eu.time.betterspotify.spotify.data.results.playlist

data class PlaylistTracksResult(
    val href: String,
    val items: List<PlaylistItem>,
    val limit: Int,
    val next: String,
    val offset: Int,
    val previous: Any,
    val total: Int
)