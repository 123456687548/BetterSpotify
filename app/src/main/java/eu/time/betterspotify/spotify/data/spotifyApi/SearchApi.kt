package eu.time.betterspotify.spotify.data.spotifyApi

import android.content.Context
import com.android.volley.VolleyError
import com.google.gson.Gson
import eu.time.betterspotify.spotify.data.types.SearchResult
import eu.time.betterspotify.util.sendGetRequest

fun sendSearch(context: Context, query: String, types: List<String>, onSuccess: (result: SearchResult) -> Unit) {
    val urlEncodedTypes = types.joinToString("%2C")

    sendSearch(context, query, urlEncodedTypes, { response ->
        val result = Gson().fromJson(response, SearchResult::class.java)
        onSuccess(result)
    })
}

private fun sendSearch(
    context: Context, query: String, types: String, onSuccess: (response: String) -> Unit, onError: (error: VolleyError) -> Unit = {
        SpotifyApi.getInstance().refreshTokenIfNeeded(context, it) {
            sendSearch(context, query, types, onSuccess)
        }
    }
) {
    val url = "https://api.spotify.com/v1/search?q=$query&type=$types&market=${SpotifyApi.getInstance().getCurrentUser().country}&limit=10"

    sendGetRequest(context, url, onSuccess, onError)
}