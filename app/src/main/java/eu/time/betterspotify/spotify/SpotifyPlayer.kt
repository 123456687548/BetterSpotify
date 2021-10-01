package eu.time.betterspotify.spotify

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import eu.time.betterspotify.MainActivity
import eu.time.betterspotify.spotify.data.types.Track

class SpotifyPlayer private constructor() {
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

    fun queueTrack(context: Context, track: Track) {
        getRemote()?.playerApi?.queue(track.uri)?.setResultCallback {
            Toast.makeText(context, "${track.name} queued!", Toast.LENGTH_SHORT).show()
        }
    }

    fun getRemote(): SpotifyAppRemote? {
        if (!isConnected) return null
        return mSpotifyAppRemote
    }

    fun connect(context: Context) {
        if (isConnected) return

        val connectionParams = ConnectionParams.Builder(MainActivity.CLIENT_ID)
            .setRedirectUri(MainActivity.REDIRECT_URI)
            .showAuthView(true)
            .build()
        SpotifyAppRemote.connect(context, connectionParams,
            object : Connector.ConnectionListener {
                override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                    mSpotifyAppRemote = spotifyAppRemote
                    Log.d("MainActivity", "Connected! Yay!")

                    isConnected = true
                }

                override fun onFailure(throwable: Throwable) {
                    Log.e("MyActivity", throwable.message, throwable)
                    Toast.makeText(context, throwable.message, Toast.LENGTH_LONG).show()

                    // Something went wrong when attempting to connect! Handle errors here
                }
            })
    }

    fun disconnect() {
        if (!isConnected) return

        SpotifyAppRemote.disconnect(mSpotifyAppRemote)
        isConnected = false
    }
}