package eu.time.betterspotify.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import eu.time.betterspotify.R
import eu.time.betterspotify.spotify.data.spotifyApi.SpotifyApi

class SpotifyAuthenticationActivity : AppCompatActivity() {
    companion object {
        fun startLoginActivity(context: Context) {
            val intent = Intent(context, SpotifyAuthenticationActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spotify_authentication)

        val sharedPref = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val accessToken = sharedPref.getString(getString(R.string.spotify_access_token), "").toString()
        val refreshToken = sharedPref.getString(getString(R.string.spotify_refresh_token), "").toString()

        if (accessToken.isNotBlank() && refreshToken.isNotBlank()) {
            SpotifyApi.getInstance().initialize(this) {
                startMainActivity()
            }
        }

        val data: Uri? = intent?.data

        if (data != null) {
            val code = data.getQueryParameter("code")
            if (code != null) {
                SpotifyApi.getInstance().requestToken(this, code)
            }
        }

        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            SpotifyApi.getInstance().requestAccess(this)
        }
    }

    private fun startMainActivity() {
        val intent = Intent(this, LibraryActivity::class.java)
        startActivity(intent)
    }
}