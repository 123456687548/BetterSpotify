package eu.time.betterspotify.spotify.data.spotifyApi

import android.content.Context
import com.android.volley.VolleyError
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import eu.time.betterspotify.spotify.data.types.Album
import eu.time.betterspotify.spotify.data.types.ResultContainer
import eu.time.betterspotify.spotify.data.types.Track
import eu.time.betterspotify.util.sendGetRequest
import java.lang.reflect.Type

fun getAlbum(
    context: Context, album: com.spotify.protocol.types.Album, onSuccess: (result: Album) -> Unit, onError: (error: VolleyError) -> Unit = {
        SpotifyApi.getInstance().refreshTokenIfNeeded(context, it) {
            getAlbum(context, album, onSuccess)
        }
    }
) {
    val albumId = album.uri.substringAfterLast(':')

    sendGetRequest(context, "https://api.spotify.com/v1/albums/$albumId", { response ->
        val result = Gson().fromJson(response, Album::class.java)
        onSuccess(result)
    }, onError)
}

fun getAlbumTracks(
    context: Context, albumId: String, onSuccess: (response: List<Track>) -> Unit, onError: (error: VolleyError) -> Unit = {
        SpotifyApi.getInstance().refreshTokenIfNeeded(context, it) {
            getAlbumTracks(context, albumId, onSuccess)
        }
    }
) {
    getAlbumTracks(context, url = "https://api.spotify.com/v1/albums/${albumId}/tracks?market=${SpotifyApi.getInstance().getCurrentUser().country}", onSuccess, onError)
}

private fun getAlbumTracks(
    context: Context, url: String, onSuccess: (response: List<Track>) -> Unit, onError: (error: VolleyError) -> Unit = {
        SpotifyApi.getInstance().refreshTokenIfNeeded(context, it) {
            getAlbumTracks(context, url = url, onSuccess)
        }
    }, trackList: MutableList<Track> = mutableListOf()
) {
    sendGetRequest(context, url, { response ->
        val type: Type = object : TypeToken<ResultContainer<Track>>() {}.type
        val result = Gson().fromJson<ResultContainer<Track>>(response, type)

        trackList.addAll(result.items)
        if (result.next == null) {
            onSuccess(trackList)
        } else {
            getAlbumTracks(context, result.next, onSuccess, onError, trackList)
        }
    }, onError)
}