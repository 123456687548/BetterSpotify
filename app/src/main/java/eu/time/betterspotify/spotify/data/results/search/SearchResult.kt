package eu.time.betterspotify.spotify.data.results.search

import eu.time.betterspotify.spotify.data.types.Artist
import eu.time.betterspotify.spotify.data.types.Playlist
import eu.time.betterspotify.spotify.data.types.ResultContainer
import eu.time.betterspotify.spotify.data.types.Track

data class SearchResult(
    val tracks: ResultContainer<Track>,
    val artists: ResultContainer<Artist>,
    val playlists: ResultContainer<Playlist>,
    val shows: ResultContainer<Track>,
    val episodes: ResultContainer<Track>
)