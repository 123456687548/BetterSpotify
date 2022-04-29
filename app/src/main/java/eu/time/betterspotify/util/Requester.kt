package eu.time.betterspotify.util

import android.content.Context
import com.android.volley.AuthFailureError
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import eu.time.betterspotify.spotify.data.spotifyApi.SpotifyApi

fun sendPutRequest(
    context: Context,
    url: String,
    body: String,
    onSuccess: (response: String) -> Unit,
    onError: (error: VolleyError) -> Unit = {},
    header: Map<String, String> = SpotifyApi.getInstance().createHeader()
) {
    val queue = Volley.newRequestQueue(context)

    val stringRequest = object : StringRequest(Method.PUT, url, onSuccess, onError) {
        @Throws(AuthFailureError::class)
        override fun getHeaders(): Map<String, String> {
            return header
        }

        override fun getBody(): ByteArray {
            return body.toByteArray()
        }
    }

    queue.add(stringRequest)
}

fun sendGetRequest(
    context: Context,
    url: String,
    onSuccess: (response: String) -> Unit,
    onError: (error: VolleyError) -> Unit = {},
    header: Map<String, String> = SpotifyApi.getInstance().createHeader()
) {
    val queue = Volley.newRequestQueue(context)

    val stringRequest = object : StringRequest(Method.GET, url, onSuccess, onError) {
        @Throws(AuthFailureError::class)
        override fun getHeaders(): Map<String, String> {
            return header
        }
    }

    queue.add(stringRequest)
}

fun sendPostRequest(
    context: Context,
    url: String,
    body: String,
    onSuccess: (response: String) -> Unit,
    onError: (error: VolleyError) -> Unit = {},
    header: Map<String, String> = SpotifyApi.getInstance().createHeader()
) {
    val queue = Volley.newRequestQueue(context)

    val stringRequest = object : StringRequest(Method.POST, url, onSuccess, onError) {
        @Throws(AuthFailureError::class)
        override fun getHeaders(): Map<String, String> {
            return header
        }

        override fun getBody(): ByteArray {
            return body.toByteArray()
        }
    }

    queue.add(stringRequest)
}

fun sendPostRequest(
    context: Context,
    url: String,
    onSuccess: (response: String) -> Unit,
    onError: (error: VolleyError) -> Unit = {},
    header: Map<String, String> = SpotifyApi.getInstance().createHeader()
) {
    val queue = Volley.newRequestQueue(context)

    val stringRequest = object : StringRequest(Method.POST, url, onSuccess, onError) {
        @Throws(AuthFailureError::class)
        override fun getHeaders(): Map<String, String> {
            return header
        }

        override fun getBodyContentType(): String {
            return "application/x-www-form-urlencoded"
        }
    }

    queue.add(stringRequest)
}