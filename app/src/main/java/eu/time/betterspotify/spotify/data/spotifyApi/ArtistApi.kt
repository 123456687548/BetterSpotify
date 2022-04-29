package eu.time.betterspotify.spotify.data.spotifyApi

import android.content.Context
import com.android.volley.VolleyError
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import eu.time.betterspotify.spotify.data.types.Album
import eu.time.betterspotify.spotify.data.types.Artist
import eu.time.betterspotify.spotify.data.types.ArtistTopTracks
import eu.time.betterspotify.spotify.data.types.ResultContainer
import eu.time.betterspotify.util.sendGetRequest
import java.lang.reflect.Type

fun getArtist(
    context: Context, artistId: String, onSuccess: (result: Artist) -> Unit, onError: (error: VolleyError) -> Unit = {
        SpotifyApi.getInstance().refreshTokenIfNeeded(context, it) {
            getArtist(context, artistId, onSuccess)
        }
    }
) {
    val url = "https://api.spotify.com/v1/artists/$artistId"

    sendGetRequest(context, url, { response ->
        val result = Gson().fromJson(response, Artist::class.java)
        onSuccess(result)
    }, onError)
}

fun getArtist(
    context: Context, artist: com.spotify.protocol.types.Artist, onSuccess: (result: Artist) -> Unit, onError: (error: VolleyError) -> Unit = {
        SpotifyApi.getInstance().refreshTokenIfNeeded(context, it) {
            getArtist(context, artist, onSuccess)
        }
    }
) {
    val artistId = artist.uri.substringAfterLast(':')

    val url = "https://api.spotify.com/v1/artists/$artistId"

    sendGetRequest(context, url, { response ->
        val result = Gson().fromJson(response, Artist::class.java)
        onSuccess(result)
    }, onError)
}

fun getArtistAlbums(
    context: Context, artistId: String, onSuccess: (result: ResultContainer<Album>) -> Unit, onError: (error: VolleyError) -> Unit = {
        SpotifyApi.getInstance().refreshTokenIfNeeded(context, it) {
            getArtistAlbums(context, artistId, onSuccess)
        }
    }
) {
    val url = "https://api.spotify.com/v1/artists/$artistId/albums?include_groups=album,single&market=${SpotifyApi.getInstance().getCurrentUser().country}&limit=50"

    sendGetRequest(context, url, { response ->
        val type: Type = object : TypeToken<ResultContainer<Album>>() {}.type
        val result = Gson().fromJson<ResultContainer<Album>>(response, type)
        onSuccess(result)
    }, onError)
}

fun getArtistTopTracks(
    context: Context, artistId: String, onSuccess: (result: ArtistTopTracks) -> Unit, onError: (error: VolleyError) -> Unit = {
        SpotifyApi.getInstance().refreshTokenIfNeeded(context, it) {
            getArtistTopTracks(context, artistId, onSuccess)
        }
    }
) {
    val url = "https://api.spotify.com/v1/artists/$artistId/top-tracks?market=${SpotifyApi.getInstance().getCurrentUser().country}"

    sendGetRequest(context, url, { response ->
        val result = Gson().fromJson(response, ArtistTopTracks::class.java)
        onSuccess(result)
    }, onError)
}