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
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import eu.time.betterspotify.R
import eu.time.betterspotify.activities.LibraryActivity
import eu.time.betterspotify.activities.SpotifyAuthenticationActivity
import eu.time.betterspotify.spotify.data.TokenResult
import eu.time.betterspotify.spotify.data.types.PlaylistItem
import eu.time.betterspotify.spotify.data.types.SearchResult
import eu.time.betterspotify.spotify.data.types.*
import eu.time.betterspotify.util.NetworkHandler
import eu.time.betterspotify.util.sha256
import java.lang.reflect.Type

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

    private lateinit var currentUser: User

    private val scopes = listOf(
        "ugc-image-upload",
        "playlist-modify-private",
        "playlist-read-private",
        "playlist-modify-public",
        "playlist-read-collaborative",
        "user-read-private",
        "user-read-email",
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

            LibraryActivity.openLibrary(context)
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

    private fun needsToRefreshToken(error: VolleyError): Boolean = error.networkResponse.statusCode == 401

    private fun refreshTokenIfNeeded(context: Context, error: VolleyError, retryCallback: () -> Unit) {
        if (!NetworkHandler.isNetworkConnected) {
            Toast.makeText(context, "No Internet connection", Toast.LENGTH_LONG).show()
            return
        }
        if (!needsToRefreshToken(error)) return

        refreshToken(context, retryCallback)
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

    fun getCurrentUser(): User = currentUser

    private fun getCurrentUser(
        context: Context, onSuccess: (result: User) -> Unit = {}, onError: (error: VolleyError) -> Unit = {
            refreshTokenIfNeeded(context, it) {
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

    fun getPlayerState(
        context: Context, onSuccess: (result: PlayerState) -> Unit = {}, onError: (error: VolleyError) -> Unit = {
            refreshTokenIfNeeded(context, it) {
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

    fun playContext(
        context: Context, contextUri: String, offset: Int, onSuccess: (response: String) -> Unit = {}, onError: (error: VolleyError) -> Unit = {
            refreshTokenIfNeeded(context, it) {
                playContext(context, contextUri, offset, onSuccess)
            }
        }
    ) {
        val header: MutableMap<String, String> = createHeader()
        val body = "{\"context_uri\":\"$contextUri\",\"offset\":{\"position\":$offset}}"

        val url = "https://api.spotify.com/v1/me/player/play"

        sendPutRequest(context, url, header, body, onSuccess, onError)
    }

    fun search(context: Context, query: String, types: List<String>, onSuccess: (result: SearchResult) -> Unit) {
        val urlEncodedTypes = types.joinToString("%2C")

        sendSearch(context, query, urlEncodedTypes, { response ->
            val result = Gson().fromJson(response, SearchResult::class.java)
            onSuccess(result)
        })
    }

    fun getArtist(
        context: Context, artistId: String, onSuccess: (result: Artist) -> Unit, onError: (error: VolleyError) -> Unit = {
            refreshTokenIfNeeded(context, it) {
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
            refreshTokenIfNeeded(context, it) {
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
            refreshTokenIfNeeded(context, it) {
                getArtistAlbums(context, artistId, onSuccess)
            }
        }
    ) {
        val url = "https://api.spotify.com/v1/artists/$artistId/albums?include_groups=album,single&market=${currentUser.country}&limit=50"

        sendGetRequest(context, url, { response ->
            val type: Type = object : TypeToken<ResultContainer<Album>>() {}.type
            val result = Gson().fromJson<ResultContainer<Album>>(response, type)
            onSuccess(result)
        }, onError)
    }

    fun getArtistTopTracks(
        context: Context, artistId: String, onSuccess: (result: ArtistTopTracks) -> Unit, onError: (error: VolleyError) -> Unit = {
            refreshTokenIfNeeded(context, it) {
                getArtistTopTracks(context, artistId, onSuccess)
            }
        }
    ) {
        val url = "https://api.spotify.com/v1/artists/$artistId/top-tracks?market=${currentUser.country}"

        sendGetRequest(context, url, { response ->
            val result = Gson().fromJson(response, ArtistTopTracks::class.java)
            onSuccess(result)
        }, onError)
    }

    private fun sendSearch(
        context: Context, query: String, types: String, onSuccess: (response: String) -> Unit, onError: (error: VolleyError) -> Unit = {
            refreshTokenIfNeeded(context, it) {
                sendSearch(context, query, types, onSuccess)
            }
        }
    ) {
        if (!::currentUser.isInitialized) return

        val url = "https://api.spotify.com/v1/search?q=$query&type=$types&market=${currentUser.country}&limit=10"

        sendGetRequest(context, url, onSuccess, onError)
    }

    fun addTracksToPlaylist(
        context: Context, tracks: List<String>, playlist: Playlist, onSuccess: (response: String) -> Unit = {}, onError: (error: VolleyError) -> Unit = {
            refreshTokenIfNeeded(context, it) {
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
            refreshTokenIfNeeded(context, it) {
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
        context: Context, url: String = "https://api.spotify.com/v1/me/playlists?limit=50", onSuccess: (result: List<Playlist>) -> Unit, onError: (error: VolleyError) -> Unit = {
            refreshTokenIfNeeded(context, it) {
                getUsersPlaylists(context, onSuccess = onSuccess)
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

    fun getTrack(
        context: Context, track: com.spotify.protocol.types.Track, onSuccess: (result: Track) -> Unit, onError: (error: VolleyError) -> Unit = {
            refreshTokenIfNeeded(context, it) {
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

    fun getSavedTracks(
        context: Context, url: String = "https://api.spotify.com/v1/me/tracks?limit=50", onSuccess: (response: List<Track>) -> Unit, onError: (error: VolleyError) -> Unit = {
            refreshTokenIfNeeded(context, it) {
                getSavedTracks(context, url, onSuccess)
            }
        }, trackList: MutableList<Track> = mutableListOf()
    ) {
        sendGetRequest(context, url, { response ->
            val type: Type = object : TypeToken<ResultContainer<PlaylistItem>>() {}.type
            val result = Gson().fromJson<ResultContainer<PlaylistItem>>(response, type)

            val tracks = mutableListOf<Track>()

            result.items.forEach { tracks.add(it.track) }

            trackList.addAll(tracks)
            if (result.next == null) {
                onSuccess(trackList)
            } else {
                getSavedTracks(context, result.next, onSuccess, onError, trackList)
            }
        }, onError)
    }

    fun getPlaylistTracks(
        context: Context, url: String, onSuccess: (response: List<Track>) -> Unit, onError: (error: VolleyError) -> Unit = {
            refreshTokenIfNeeded(context, it) {
                getPlaylistTracks(context, url, onSuccess)
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

    fun getAlbum(
        context: Context, album: com.spotify.protocol.types.Album, onSuccess: (result: Album) -> Unit, onError: (error: VolleyError) -> Unit = {
            refreshTokenIfNeeded(context, it) {
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
        context: Context, url: String, onSuccess: (response: List<Track>) -> Unit, onError: (error: VolleyError) -> Unit = {
            refreshTokenIfNeeded(context, it) {
                getAlbumTracks(context, url, onSuccess)
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

    fun getPlaylistByName(
        context: Context, name: String, onSuccess: (response: Playlist?) -> Unit, onError: (error: VolleyError) -> Unit = {
            refreshTokenIfNeeded(context, it) {
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
            refreshTokenIfNeeded(context, it) {
                createPlaylist(context, name, onSuccess)
            }
        }
    ) {
        val body = "{\"name\":\"$name\"}"
        sendPostRequest(context, "https://api.spotify.com/v1/users/${currentUser.id}/playlists", body, { response ->
            val playlist = Gson().fromJson(response, Playlist::class.java)
            onSuccess(playlist)
        })
    }

    fun addToTemp(
        context: Context, uri: String, onSuccess: (response: String) -> Unit, onError: (error: VolleyError) -> Unit = {
            refreshTokenIfNeeded(context, it) {
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

    fun initialize(context: Context, onSuccess: () -> Unit = {}) {
        if (initialized) {
            onSuccess()
        }

        val token = getToken(context)

        if (token != null) {
            this.token = token

            getCurrentUser(context, { result ->
                currentUser = result
                initialized = true
                onSuccess()
            })
        } else {
            startLoginActivity(context)
            return
        }
    }

    private fun startLoginActivity(context: Context) {
        val intent = Intent(context, SpotifyAuthenticationActivity::class.java)
        context.startActivity(intent)
    }

    private fun getToken(context: Context): TokenResult? {
        val sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val accessToken = sharedPref.getString(context.getString(R.string.spotify_access_token), "").toString()
        val refreshToken = sharedPref.getString(context.getString(R.string.spotify_refresh_token), "").toString()

        if (accessToken.isNotBlank() && refreshToken.isNotBlank()) {
            return TokenResult(accessToken, refreshToken)
        }

        return null
    }

    private fun createHeader(): MutableMap<String, String> {
        val header: MutableMap<String, String> = HashMap()
        header["Accept"] = "application/json"
        header["Content-Type"] = "application/json"
        header["Authorization"] = "Bearer ${token.accessToken}"
        return header
    }

    private fun sendPutRequest(
        context: Context,
        url: String,
        header: MutableMap<String, String>,
        body: String,
        onSuccess: (response: String) -> Unit,
        onError: (error: VolleyError) -> Unit = {}
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

//    private fun sendGetRequest(context: Context, url: String, onSuccess: (response: String) -> Unit, onError: (error: VolleyError) -> Unit = {}) {
//        val queue = Volley.newRequestQueue(context)
//
//        val stringRequest = object : StringRequest(Method.GET, url, onSuccess, onError) {}
//
//        queue.add(stringRequest)
//    }

    private fun sendGetRequest(
        context: Context,
        url: String,
        onSuccess: (response: String) -> Unit,
        onError: (error: VolleyError) -> Unit = {},
        header: MutableMap<String, String> = createHeader()
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

    private fun sendPostRequest(
        context: Context,
        url: String,
        body: String,
        onSuccess: (response: String) -> Unit,
        onError: (error: VolleyError) -> Unit = {},
        header: MutableMap<String, String> = createHeader()
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

    private fun sendPostRequest(
        context: Context,
        url: String,
        onSuccess: (response: String) -> Unit,
        onError: (error: VolleyError) -> Unit = {},
        header: MutableMap<String, String> = createHeader()
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
}