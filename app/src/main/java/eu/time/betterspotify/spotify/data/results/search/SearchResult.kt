package eu.time.betterspotify.spotify.data.results.search

import eu.time.betterspotify.spotify.data.types.*

data class SearchResult(
    val tracks: ResultContainer<Track>,
    val artists: ResultContainer<Artist>,
    val playlists: ResultContainer<Playlist>,
    val albums: ResultContainer<Album>,
    val shows: ResultContainer<Track>,
    val episodes: ResultContainer<Track>
)