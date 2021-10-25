package eu.time.betterspotify.spotify

import android.content.Context
import com.android.volley.AuthFailureError
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.util.*
import kotlin.collections.HashMap
import androidx.core.content.ContextCompat.startActivity

import android.content.Intent
import android.net.Uri
import android.util.Log
import com.google.gson.Gson
import eu.time.betterspotify.MainActivity
import eu.time.betterspotify.R
import eu.time.betterspotify.spotify.data.TokenResult
import eu.time.betterspotify.spotify.data.results.playlist.PlaylistItem
import eu.time.betterspotify.spotify.data.results.playlist.PlaylistTracksResult
import eu.time.betterspotify.util.sha256

class SpotifyApi private constructor() {
    companion object {
        const val CLIENT_ID = "46d14dadfde64caaaf171e15245a9fe6"
        const val REDIRECT_URI = "http://localhost/Spotify"

        private lateinit var INSTANCE: SpotifyApi

        fun getInstance(): SpotifyApi {
            if (!::INSTANCE.isInitialized) {
                INSTANCE = SpotifyApi()
            }

            return INSTANCE
        }
    }

    private var initialized = false

    private val codeVerifier = generateNonce()

    private lateinit var token: TokenResult

    private val scopes = listOf(
        "playlist-modify-private",
        "playlist-read-private",
        "playlist-modify-public",
        "playlist-read-collaborative",
        "user-read-playback-state",
        "user-modify-playback-state",
        "user-read-currently-playing",
        "user-library-modify",
        "user-library-read",
        "user-read-playback-position",
        "user-read-recently-played",
        "user-top-read",
        "app-remote-control",
        "streaming",
        "user-follow-modify",
        "user-follow-read"
    )

    fun requestToken(context: Context, code: String) {
        val param: MutableMap<String, String> = HashMap()
        param["client_id"] = CLIENT_ID
        param["grant_type"] = "authorization_code"
        param["code"] = code
        param["redirect_uri"] = REDIRECT_URI
        param["code_verifier"] = codeVerifier

        val bodyBuilder = StringBuilder()

        param.forEach { (key, value) ->
            bodyBuilder.append(key).append("=").append(value).append("&")
        }

        val body = bodyBuilder.substring(0, bodyBuilder.length - 1)

        val queue = Volley.newRequestQueue(context)

        val stringRequest = object : StringRequest(Method.POST, "https://accounts.spotify.com/api/token", {
            token = Gson().fromJson(it, TokenResult::class.java)

            val sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
            if (sharedPref != null) {
                with(sharedPref.edit()) {
                    putString(context.getString(R.string.spotify_access_token), token.accessToken)
                    putString(context.getString(R.string.spotify_refresh_token), token.refreshToken)
                    apply()
                }
            }

            startMainActivity(context)
        }, {
            it.message?.let { it1 -> Log.d("request", it1) }
        }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                return param
            }

            override fun getBody(): ByteArray {
                return body.toByteArray()
            }

            override fun getBodyContentType(): String {
                return "application/x-www-form-urlencoded"
            }
        }

        queue.add(stringRequest)
    }

    private fun refreshToken(context: Context, retryCallback: () -> Unit) {
        val param: MutableMap<String, String> = HashMap()
        param["client_id"] = CLIENT_ID
        param["grant_type"] = "refresh_token"
        param["refresh_token"] = token.refreshToken

        val bodyBuilder = StringBuilder()

        param.forEach { (key, value) ->
            bodyBuilder.append(key).append("=").append(value).append("&")
        }

        val body = bodyBuilder.substring(0, bodyBuilder.length - 1)

        val queue = Volley.newRequestQueue(context)

        val stringRequest = object : StringRequest(Method.POST, "https://accounts.spotify.com/api/token", {
            token = Gson().fromJson(it, TokenResult::class.java)

            val sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
            if (sharedPref != null) {
                with(sharedPref.edit()) {
                    putString(context.getString(R.string.spotify_access_token), token.accessToken)
                    putString(context.getString(R.string.spotify_refresh_token), token.refreshToken)
                    apply()
                }
            }

            retryCallback()
//            startMainActivity(context)
        }, {
            it.message?.let { it1 -> Log.d("request", it1) }
        }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                return param
            }

            override fun getBody(): ByteArray {
                return body.toByteArray()
            }

            override fun getBodyContentType(): String {
                return "application/x-www-form-urlencoded"
            }
        }

        queue.add(stringRequest)
    }

    fun requestAccess(context: Context) {
        val scopes = this.scopes.joinToString(" ")
        val codeChallenge = generateCodeChallenge(codeVerifier)

        val param: MutableMap<String, String> = HashMap()
        param["client_id"] = CLIENT_ID
        param["response_type"] = "code"
        param["redirect_uri"] = REDIRECT_URI
        param["code_challenge_method"] = "S256"
        param["code_challenge"] = codeChallenge
        param["state"] = "1234567890"
        param["scope"] = scopes

        val urlHead = "https://accounts.spotify.com/authorize?"

        val url = StringBuilder(urlHead)

        param.forEach { (key, value) ->
            url.append("&").append(key).append("=").append(value)
        }

        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url.toString()))
        startActivity(context, browserIntent, null)
    }

    private fun generateNonce(): String {
        val chars = "abcdefghijklmnopqrstuvwxyz123456789"
        val nonce = StringBuilder()
        val random = Random()

        for (i in 0..127) {
            nonce.append(chars[random.nextInt(chars.length)])
        }

        return nonce.toString()
    }

    private fun generateCodeChallenge(codeVerifier: String): String {
        val hash = codeVerifier.sha256()
        val base64Hash = String(Base64.getUrlEncoder().encode(hash))
        var code = base64Hash.replace(Regex("\\+"), "-")
        code = code.replace(Regex("/"), "_")
        code = code.replace(Regex("=+$"), "")
        return code
    }

    fun search(context: Context, query: String, types: List<String>, onSuccess: (response: String) -> Unit) {
        val urlEncodedTypes = types.joinToString("%2C")

        sendSearch(context, query, urlEncodedTypes, onSuccess)
    }

    private fun sendSearch(
        context: Context, query: String, types: String, onSuccess: (response: String) -> Unit, onError: (error: VolleyError) -> Unit = {
            refreshToken(context) {
                sendSearch(context, query, types, onSuccess)
            }
        }
    ) {
        val header: MutableMap<String, String> = createHeader()

        val url = "https://api.spotify.com/v1/search?q=$query&type=$types"

        sendGetRequest(context, url, header, onSuccess, onError)
    }

    fun getPlaylists(
        context: Context, onSuccess: (response: String) -> Unit, onError: (error: VolleyError) -> Unit = {
            refreshToken(context) {
                getPlaylists(context, onSuccess)
            }
        }
    ) {
        val header: MutableMap<String, String> = createHeader()

        sendGetRequest(context, "https://api.spotify.com/v1/me/playlists", header, onSuccess, onError)
    }

    fun getPlaylistTracks(
        context: Context, url: String, onSuccess: (response: List<PlaylistItem>) -> Unit, onError: (error: VolleyError) -> Unit = {
            refreshToken(context) {
                getPlaylistTracks(context, url, onSuccess)
            }
        }, trackList: MutableList<PlaylistItem> = mutableListOf()
    ) {
        val header: MutableMap<String, String> = createHeader()

        sendGetRequest(context, url, header, { result ->
            val tracks = Gson().fromJson(result, PlaylistTracksResult::class.java)
            trackList.addAll(tracks.items)
            if (tracks.next == null) {
                onSuccess(trackList)
            } else {
                getPlaylistTracks(context, tracks.next, onSuccess, onError, trackList)
            }
        }, onError)
    }

    fun initialize(accessToken: String, refreshToken: String) {
        if (initialized) return

        token = TokenResult(accessToken, refreshToken)

        initialized = true
    }

    fun initialize(token: TokenResult) {
        if (initialized) return

        this.token = token

        initialized = true
    }

    fun initialize(context: Context) {
        if (initialized) return

        requestAccess(context)

        initialized = true
    }

    private fun createHeader(): MutableMap<String, String> {
        val header: MutableMap<String, String> = HashMap()
        header["Accept"] = "application/json"
        header["Content-Type"] = "application/json"
        header["Authorization"] = "Bearer ${token.accessToken}"
        return header
    }

    private fun sendGetRequest(context: Context, url: String, header: MutableMap<String, String>, onSuccess: (response: String) -> Unit, onError: (error: VolleyError) -> Unit = {}) {
        val queue = Volley.newRequestQueue(context)

        val stringRequest = object : StringRequest(Method.GET, url, onSuccess, onError) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                return header
            }
        }

        queue.add(stringRequest)
    }

    private fun sendPostRequest(context: Context, url: String, header: MutableMap<String, String>, onSuccess: (response: String) -> Unit, onError: (error: VolleyError) -> Unit = {}) {
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

    private fun startMainActivity(context: Context) {
        val intent = Intent(context, MainActivity::class.java)
        context.startActivity(intent)
    }
}