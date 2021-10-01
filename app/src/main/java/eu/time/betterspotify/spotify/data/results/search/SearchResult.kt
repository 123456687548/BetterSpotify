package eu.time.betterspotify.spotify.data.results.search

data class SearchResult(
    val tracks: SearchResultTracks,
    val artists: SearchResultArtists,
    val playlists: SearchResultArtists,
    val shows: SearchResultArtists,
    val episodes: SearchResultArtists
)