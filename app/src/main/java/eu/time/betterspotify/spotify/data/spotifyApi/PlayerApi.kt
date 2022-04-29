package eu.time.betterspotify.spotify.data.spotifyApi

import android.content.Context
import com.android.volley.VolleyError
import com.google.gson.Gson
import eu.time.betterspotify.spotify.data.types.PlayerState
import eu.time.betterspotify.util.sendGetRequest
import eu.time.betterspotify.util.sendPutRequest

fun getPlayerState(
    context: Context, onSuccess: (result: PlayerState) -> Unit = {}, onError: (error: VolleyError) -> Unit = {
        SpotifyApi.getInstance().refreshTokenIfNeeded(context, it) {
            getPlayerState(context, onSuccess)
        }
    }
) {
    val url = "https://api.spotify.com/v1/me/player"

    sendGetRequest(context, url, { response ->
        val result = Gson().fromJson(response, PlayerState::class.java)
        onSuccess(result)
    }, onError)
}

fun playContextApi(
    context: Context, contextUri: String, offset: Int, onSuccess: (response: String) -> Unit = {}, onError: (error: VolleyError) -> Unit = {
        SpotifyApi.getInstance().refreshTokenIfNeeded(context, it) {
            playContextApi(context, contextUri, offset, onSuccess)
        }
    }
) {
    val body = "{\"context_uri\":\"$contextUri\",\"offset\":{\"position\":$offset}}"

    val url = "https://api.spotify.com/v1/me/player/play"

    sendPutRequest(context, url, body, onSuccess, onError)
}
