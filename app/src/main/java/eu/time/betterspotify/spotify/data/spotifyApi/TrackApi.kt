package eu.time.betterspotify.spotify.data.spotifyApi

import android.content.Context
import com.android.volley.VolleyError
import com.google.gson.Gson
import eu.time.betterspotify.spotify.data.types.Track
import eu.time.betterspotify.util.sendGetRequest

fun getTrack(
    context: Context, track: com.spotify.protocol.types.Track, onSuccess: (result: Track) -> Unit, onError: (error: VolleyError) -> Unit = {
        SpotifyApi.getInstance().refreshTokenIfNeeded(context, it) {
            getTrack(context, track, onSuccess)
        }
    }
) {
    val trackId = track.uri.substringAfterLast(':')

    sendGetRequest(context, "https://api.spotify.com/v1/tracks/$trackId", { response ->
        val result = Gson().fromJson(response, Track::class.java)
        onSuccess(result)
    }, onError)
}