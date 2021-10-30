package eu.time.betterspotify

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.CompoundButton
import androidx.appcompat.widget.SwitchCompat
import eu.time.betterspotify.spotify.SpotifyPlayer

class BigPlayerActivity : AppCompatActivity() {
    private lateinit var spotifyPlayer: SpotifyPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_big_player)

        val swKeepAlive = findViewById<SwitchCompat>(R.id.swKeepAwake)

        swKeepAlive.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
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