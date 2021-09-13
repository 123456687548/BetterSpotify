package eu.time.betterspotify.spotify.data

import android.content.Context
import android.util.Log
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import eu.time.betterspotify.MainActivity

class SpotifyPlayer private constructor() {
    companion object {
        private lateinit var INSTANCE: SpotifyPlayer

        fun getInstance(): SpotifyPlayer {
            if (!::INSTANCE.isInitialized) {
                INSTANCE = SpotifyPlayer()
            }

            return INSTANCE
        }
    }

    private lateinit var mSpotifyAppRemote: SpotifyAppRemote

    private var isConnected = false

    fun getRemote(context: Context): SpotifyAppRemote {
        if(!isConnected) connect(context)
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

                    // Now you can start interacting with App Remote
//                    connected()
                    isConnected = true
                }

                override fun onFailure(throwable: Throwable) {
                    Log.e("MyActivity", throwable.message, throwable)

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