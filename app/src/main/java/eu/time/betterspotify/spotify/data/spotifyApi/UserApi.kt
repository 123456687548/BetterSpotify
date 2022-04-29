package eu.time.betterspotify.spotify.data.spotifyApi

import android.content.Context
import com.android.volley.VolleyError
import com.google.gson.Gson
import eu.time.betterspotify.spotify.data.types.User
import eu.time.betterspotify.util.sendGetRequest

fun getCurrentUser(
    context: Context, onSuccess: (result: User) -> Unit = {}, onError: (error: VolleyError) -> Unit = {
        SpotifyApi.getInstance().refreshTokenIfNeeded(context, it) {
            getCurrentUser(context, onSuccess)
        }
    }
) {
    val url = "https://api.spotify.com/v1/me"

    sendGetRequest(context, url, { response ->
        val result = Gson().fromJson(response, User::class.java)
        onSuccess(result)
    }, onError)
}