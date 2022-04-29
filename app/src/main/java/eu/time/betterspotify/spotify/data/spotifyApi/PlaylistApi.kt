package eu.time.betterspotify.spotify.data.spotifyApi

import android.content.Context
import com.android.volley.VolleyError
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import eu.time.betterspotify.spotify.data.types.Playlist
import eu.time.betterspotify.spotify.data.types.PlaylistItem
import eu.time.betterspotify.spotify.data.types.ResultContainer
import eu.time.betterspotify.spotify.data.types.Track
import eu.time.betterspotify.util.sendGetRequest
import eu.time.betterspotify.util.sendPostRequest
import java.lang.reflect.Type

fun addTracksToPlaylist(
    context: Context, tracks: List<String>, playlist: Playlist, onSuccess: (response: String) -> Unit = {}, onError: (error: VolleyError) -> Unit = {
        SpotifyApi.getInstance().refreshTokenIfNeeded(context, it) {
            addTracksToPlaylist(context, tracks, playlist, onSuccess)
        }
    }
) {
    val url = "https://api.spotify.com/v1/playlists/${playlist.id}/tracks"

    val uris = tracks.joinToString { "\"$it\"" }

    val body = "{\"uris\":[$uris]}"

    sendPostRequest(context, url, body, onSuccess, onError)
}

fun getPlaylist(
    context: Context, playlistId: String, onSuccess: (result: Playlist) -> Unit, onError: (error: VolleyError) -> Unit = {
        SpotifyApi.getInstance().refreshTokenIfNeeded(context, it) {
            getPlaylist(context, playlistId, onSuccess)
        }
    }
) {

    sendGetRequest(context, "https://api.spotify.com/v1/playlists/$playlistId", { response ->
        val result = Gson().fromJson(response, Playlist::class.java)
        onSuccess(result)
    }, onError)
}

fun getUsersPlaylists(
    context: Context, onSuccess: (result: List<Playlist>) -> Unit, onError: (error: VolleyError) -> Unit = {
        SpotifyApi.getInstance().refreshTokenIfNeeded(context, it) {
            getUsersPlaylists(context, onSuccess)
        }
    }
) {
    getUsersPlaylists(context, "https://api.spotify.com/v1/me/playlists?limit=50", onSuccess, onError)
}

private fun getUsersPlaylists(
    context: Context, url: String, onSuccess: (result: List<Playlist>) -> Unit, onError: (error: VolleyError) -> Unit = {
        SpotifyApi.getInstance().refreshTokenIfNeeded(context, it) {
            getUsersPlaylists(context, url, onSuccess)
        }
    }, playlistList: MutableList<Playlist> = mutableListOf()
) {
    sendGetRequest(context, url, { response ->
        val type: Type = object : TypeToken<ResultContainer<Playlist>>() {}.type
        val result = Gson().fromJson<ResultContainer<Playlist>>(response, type)

        val playlists = mutableListOf<Playlist>()

        result.items.forEach { playlists.add(it) }

        playlistList.addAll(playlists)
        if (result.next == null) {
            onSuccess(playlistList)
        } else {
            getUsersPlaylists(context, result.next, onSuccess, onError, playlistList)
        }
    }, onError)
}

fun getSavedTracks(
    context: Context, onSuccess: (response: List<Track>) -> Unit, onError: (error: VolleyError) -> Unit = {
        SpotifyApi.getInstance().refreshTokenIfNeeded(context, it) {
            getSavedTracks(context, onSuccess)
        }
    }
) {
    getPlaylistTracks(context, url = "https://api.spotify.com/v1/me/tracks?limit=50", onSuccess, onError)
}

fun getPlaylistTracks(
    context: Context, trackId: String, onSuccess: (response: List<Track>) -> Unit, onError: (error: VolleyError) -> Unit = {
        SpotifyApi.getInstance().refreshTokenIfNeeded(context, it) {
            getPlaylistTracks(context, trackId, onSuccess)
        }
    }
) {
    getPlaylistTracks(context, url = "https://api.spotify.com/v1/playlists/${trackId}/tracks", onSuccess, onError)
}

private fun getPlaylistTracks(
    context: Context, url: String, onSuccess: (response: List<Track>) -> Unit, onError: (error: VolleyError) -> Unit = {
        SpotifyApi.getInstance().refreshTokenIfNeeded(context, it) {
            getPlaylistTracks(context, url = url, onSuccess)
        }
    }, trackList: MutableList<Track> = mutableListOf()
) {
    sendGetRequest(context, url, { response ->
        if (response.isBlank()) onSuccess(trackList)

        val type: Type = object : TypeToken<ResultContainer<PlaylistItem>>() {}.type
        val result = Gson().fromJson<ResultContainer<PlaylistItem>>(response, type)

        val tracks = mutableListOf<Track>()

        result.items.forEach { tracks.add(it.track) }

        trackList.addAll(tracks)
        if (result.next == null) {
            onSuccess(trackList)
        } else {
            getPlaylistTracks(context, result.next, onSuccess, onError, trackList)
        }
    }, onError)
}

fun getPlaylistByName(
    context: Context, name: String, onSuccess: (response: Playlist?) -> Unit, onError: (error: VolleyError) -> Unit = {
        SpotifyApi.getInstance().refreshTokenIfNeeded(context, it) {
            getPlaylistByName(context, name, onSuccess)
        }
    }
) {
    getUsersPlaylists(context, onSuccess = { playlists ->
        val playlist = playlists.firstOrNull { it.name == name }
        onSuccess(playlist)
    })
}

fun createPlaylist(
    context: Context, name: String, onSuccess: (response: Playlist) -> Unit, onError: (error: VolleyError) -> Unit = {
        SpotifyApi.getInstance().refreshTokenIfNeeded(context, it) {
            createPlaylist(context, name, onSuccess)
        }
    }
) {
    val body = "{\"name\":\"$name\"}"
    sendPostRequest(context, "https://api.spotify.com/v1/users/${SpotifyApi.getInstance().getCurrentUser().id}/playlists", body, { response ->
        val playlist = Gson().fromJson(response, Playlist::class.java)
        onSuccess(playlist)
    })
}

fun addToTemp(
    context: Context, uri: String, onSuccess: (response: String) -> Unit, onError: (error: VolleyError) -> Unit = {
        SpotifyApi.getInstance().refreshTokenIfNeeded(context, it) {
            addToTemp(context, uri, onSuccess)
        }
    }
) {
    val playlistName = "temp"
    getPlaylistByName(context, playlistName, { playlist ->
        if (playlist == null) {
            createPlaylist(context, playlistName, onSuccess = { playlist ->
                addTracksToPlaylist(context, listOf(uri), playlist, { resp ->
                    onSuccess(resp)
                })
            })
        } else {
            addTracksToPlaylist(context, listOf(uri), playlist, { resp ->
                onSuccess(resp)
            })
        }
    })
}