package eu.time.betterspotify

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import eu.time.betterspotify.spotify.SpotifyPlayer

class BigPlayerActivity : AppCompatActivity() {
    private lateinit var spotifyPlayer: SpotifyPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_big_player)
    }

    override fun onStart() {
        super.onStart()

        spotifyPlayer = SpotifyPlayer.getInstance(this)

        PlayerController.getInstance().start(this)
    }

    override fun onPause() {
        super.onPause()
        PlayerController.getInstance().stop()

        if (::spotifyPlayer.isInitialized) {
            spotifyPlayer.disconnect()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, R.anim.slide_down)
    }
}