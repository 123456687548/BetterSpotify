package eu.time.betterspotify.spotify

import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.*
import eu.time.betterspotify.spotify.SpotifyApi.Companion.CLIENT_ID
import eu.time.betterspotify.spotify.SpotifyApi.Companion.REDIRECT_URI
import eu.time.betterspotify.spotify.data.types.Track

class SpotifyPlayer private constructor() {
    enum class RepeatMode(val value: Int) {
        NONE(0), REPEAT_ONE(1), REPEAT_ALL(2);

        companion object {
            fun fromInt(value: Int) = values().first { it.value == value }
        }
    }

    companion object {
        private lateinit var INSTANCE: SpotifyPlayer

        fun getInstance(context: Context): SpotifyPlayer {
            if (!Companion::INSTANCE.isInitialized) {
                INSTANCE = SpotifyPlayer()
            }

            INSTANCE.connect(context)

            return INSTANCE
        }
    }

    private lateinit var mSpotifyAppRemote: SpotifyAppRemote

    private var isConnected = false

    fun changeRepeatMode() {
        getPlayerState { playerState ->
            when (getRepeatMode(playerState)) {
                RepeatMode.NONE -> { //is no repeat -> repeat all
                    getRemote()?.playerApi?.setRepeat(RepeatMode.REPEAT_ALL.value)
                }
                RepeatMode.REPEAT_ONE -> { // is repeat one -> no repeat
                    getRemote()?.playerApi?.setRepeat(RepeatMode.NONE.value)
                }
                RepeatMode.REPEAT_ALL -> { // is repeat all -> repeat one
                    getRemote()?.playerApi?.setRepeat(RepeatMode.REPEAT_ONE.value)
                }
            }
        }
    }

    fun getRepeatMode(playerState: PlayerState): RepeatMode {
        return RepeatMode.fromInt(playerState.playbackOptions.repeatMode)
    }

    fun toggleShuffle() {
        getPlayerState {
            if (it.playbackOptions.isShuffling) {
                getRemote()?.playerApi?.setShuffle(false)
            } else {
                getRemote()?.playerApi?.setShuffle(true)
            }
        }
    }

    fun skipNext() {
        getRemote()?.playerApi?.skipNext()
    }

    fun skipPrevious() {
        getRemote()?.playerApi?.skipPrevious()
    }

    fun seekTo(value: Int) {
        getRemote()?.playerApi?.seekTo(value.toLong())
    }

    fun togglePlay() {
        getPlayerState {
            if (it.isPaused) {
                getRemote()?.playerApi?.resume()
            } else {
                getRemote()?.playerApi?.pause()
            }
        }
    }

    fun playUri(uri: String) {
        getRemote()?.playerApi?.play(uri)
    }

    fun queueTrack(context: Context, track: Track) {
        getRemote()?.playerApi?.queue(track.uri)?.setResultCallback {
            Toast.makeText(context, "${track.name} queued!", Toast.LENGTH_SHORT).show()
        }
    }

    fun toggleLike(uri: String, callback: (Boolean) -> Unit = {}) {
        getLibraryState(uri) { libraryState ->
            if (libraryState.isAdded) {
                getRemote()?.userApi?.removeFromLibrary(uri)?.setResultCallback {
                    callback(false)
                }
            } else {
                getRemote()?.userApi?.addToLibrary(uri)?.setResultCallback{
                    callback(true)
                }
            }
        }
    }

    fun getLibraryState(uri: String, callback: (libraryState: LibraryState) -> Unit) {
        getRemote()?.userApi?.getLibraryState(uri)?.setResultCallback { libraryState ->
            callback(libraryState)
        }
    }

    fun getPlayerState(callback: (playerState: PlayerState) -> Unit) {
        getRemote()?.playerApi?.playerState?.setResultCallback { playerState ->
            callback(playerState)
        }
    }

    fun getPlayerContext(callback: (playerContext: PlayerContext) -> Unit) {
        getRemote()?.playerApi?.subscribeToPlayerContext()?.setEventCallback { playerContext ->
            callback(playerContext)
        }
    }

    fun getImage(imageUri: ImageUri, callback: (bitmap: Bitmap) -> Unit) {
        getRemote()?.imagesApi?.getImage(imageUri)?.setResultCallback {
            callback(it)
        }
    }

    private fun getRemote(): SpotifyAppRemote? {
        if (!isConnected) return null
        return mSpotifyAppRemote
    }

    fun connect(context: Context) {
        if (isConnected) return

        val connectionParams = ConnectionParams.Builder(CLIENT_ID)
            .setRedirectUri(REDIRECT_URI)
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect(context, connectionParams,
            object : Connector.ConnectionListener {
                override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                    mSpotifyAppRemote = spotifyAppRemote

                    isConnected = true
                }

                override fun onFailure(throwable: Throwable) {
                    Toast.makeText(context, throwable.message, Toast.LENGTH_LONG).show()
                }
            })
    }

    fun disconnect() {
        if (!isConnected) return

        SpotifyAppRemote.disconnect(mSpotifyAppRemote)
        isConnected = false
    }
}