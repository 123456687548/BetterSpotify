package eu.time.betterspotify

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import com.spotify.protocol.types.PlayerState
import com.spotify.protocol.types.Track
import eu.time.betterspotify.spotify.SpotifyPlayer
import eu.time.betterspotify.util.loadImageFromUri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MiniPlayerController private constructor() {
    companion object {
        private val INSTANCE = MiniPlayerController()

        fun getInstance(): MiniPlayerController {
            return INSTANCE
        }
    }

    private var activeColor = -1
    private var active = false

    fun start(context: Context) {
        activeColor = Color.valueOf(context.getColor(R.color.green_900)).toArgb()

        initListeners(context)

        active = true

        val mainHandler = Handler(Looper.getMainLooper())

        mainHandler.post(object : Runnable {
            override fun run() {
                CoroutineScope(Dispatchers.IO).launch {
                    SpotifyPlayer.getInstance().getRemote()?.playerApi?.playerState?.setResultCallback { playerState ->
                        updateMiniPlayerUI(context, playerState)
                    }
                }

                if (active) {
                    mainHandler.postDelayed(this, 1000)
                }
            }
        })
    }

    fun stop(){
        active = false
    }

    private fun initListeners(context: Context) {
        val activity = context as Activity

        val llPlayerInfo = activity.findViewById<LinearLayout>(R.id.llPlayerInfo)
        val btnRepeat = activity.findViewById<ImageButton>(R.id.btnRepeat)
        val btnShuffle = activity.findViewById<ImageButton>(R.id.btnShuffle)
        val btnPlay = activity.findViewById<ImageButton>(R.id.btnPlay)

        llPlayerInfo.setOnClickListener {
            stop()
            val intent = Intent(context, BigPlayerActivity::class.java)
            context.startActivity(intent)
            context.overridePendingTransition(R.anim.slide_up, 0)
        }

        btnRepeat.setOnClickListener {
            val playerApi = SpotifyPlayer.getInstance().getRemote()?.playerApi

            playerApi?.playerState?.setResultCallback {
                when (it.playbackOptions.repeatMode) {
                    0 -> { //is no repeat -> repeat all
                        playerApi.setRepeat(2)
                        btnRepeat.setImageResource(R.drawable.ic_baseline_repeat_48)
                        btnRepeat.setColorFilter(activeColor)
                    }
                    1 -> { // is repeat one -> no repeat
                        playerApi.setRepeat(0)
                        btnRepeat.setImageResource(R.drawable.ic_baseline_repeat_48)
                        btnRepeat.clearColorFilter()
                    }
                    2 -> { // is repeat all -> repeat one
                        playerApi.setRepeat(1)
                        btnRepeat.setImageResource(R.drawable.ic_baseline_repeat_one_48)
                    }
                }
            }
        }

        btnShuffle.setOnClickListener {
            val playerApi = SpotifyPlayer.getInstance().getRemote()?.playerApi

            playerApi?.playerState?.setResultCallback {
                if (it.playbackOptions.isShuffling) {
                    playerApi.setShuffle(false)
                    btnShuffle.clearColorFilter()
                } else {
                    playerApi.setShuffle(true)
                    btnShuffle.setColorFilter(activeColor)
                }
            }
        }

        btnPlay.setOnClickListener {
            val playerApi = SpotifyPlayer.getInstance().getRemote()?.playerApi

            playerApi?.playerState?.setResultCallback {
                if (it.isPaused) {
                    playerApi.resume()
                    btnPlay.setImageResource(R.drawable.ic_baseline_pause_48)
                } else {
                    playerApi.pause()
                    btnPlay.setImageResource(R.drawable.ic_baseline_play_arrow_48)
                }
            }
        }
    }

    private fun updateMiniPlayerUI(context: Context, playerState: PlayerState) {
        val activity = context as Activity

        val btnRepeat = activity.findViewById<ImageButton>(R.id.btnRepeat)
        val btnShuffle = activity.findViewById<ImageButton>(R.id.btnShuffle)
        val btnPlay = activity.findViewById<ImageButton>(R.id.btnPlay)
        val ivPlayerCover = activity.findViewById<ImageView>(R.id.ivPlayerCover)
        val tvPlayerTitle = activity.findViewById<TextView>(R.id.tvPlayerTitle)
        val tvPlayerArtist = activity.findViewById<TextView>(R.id.tvPlayerArtist)
        val pbProgress = activity.findViewById<ProgressBar>(R.id.pbProgress)

        val miniPlayer = activity.findViewById<View>(R.id.miniPlayer)

        context.runOnUiThread {
            val track: Track? = playerState.track
            if (track != null) {
                miniPlayer.visibility = View.VISIBLE

                pbProgress.max = track.duration.toInt()
                pbProgress.progress = playerState.playbackPosition.toInt()

                if (tvPlayerTitle.text != track.name) {
                    tvPlayerTitle.text = track.name
                    tvPlayerArtist.text = track.artist.name

                    ivPlayerCover.loadImageFromUri(track.imageUri)
                }

                if (playerState.isPaused) {
                    btnPlay.setImageResource(R.drawable.ic_baseline_play_arrow_48)
                } else {
                    btnPlay.setImageResource(R.drawable.ic_baseline_pause_48)
                }

                if (playerState.playbackOptions.isShuffling) {
                    btnShuffle.setColorFilter(activeColor)
                } else {
                    btnShuffle.clearColorFilter()
                }

                when (playerState.playbackOptions.repeatMode) {
                    0 -> {
                        btnRepeat.setImageResource(R.drawable.ic_baseline_repeat_48)
                        btnRepeat.clearColorFilter()
                    }
                    1 -> {
                        btnRepeat.setImageResource(R.drawable.ic_baseline_repeat_one_48)
                    }
                    2 -> {
                        btnRepeat.setImageResource(R.drawable.ic_baseline_repeat_48)
                        btnRepeat.setColorFilter(activeColor)
                    }
                }
            } else {
                miniPlayer.visibility = View.GONE
            }
        }
    }
}