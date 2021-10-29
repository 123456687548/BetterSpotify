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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.graphics.drawable.GradientDrawable
import eu.time.betterspotify.util.*


class PlayerController private constructor() {
    companion object {
        private val INSTANCE = PlayerController()

        fun getInstance(): PlayerController {
            return INSTANCE
        }
    }

    private lateinit var spotifyPlayer: SpotifyPlayer

    private var activeColor = -1
    private var active = false

    fun start(context: Context) {
        spotifyPlayer = SpotifyPlayer.getInstance(context)

        activeColor = Color.valueOf(context.getColor(R.color.green_900)).toArgb()

        initListeners(context)

        active = true

        val mainHandler = Handler(Looper.getMainLooper())

        mainHandler.post(object : Runnable {
            override fun run() {
                CoroutineScope(Dispatchers.IO).launch {
                    spotifyPlayer.getPlayerState { playerState ->
                        updatePlayerUI(context, playerState)
                    }
                }

                if (active) {
                    mainHandler.postDelayed(this, 50)
                }
            }
        })
    }

    fun stop() {
        active = false
    }

    private fun initListeners(context: Context) {
        val activity = context as Activity

        val llPlayerInfo: LinearLayout? = activity.findViewById(R.id.llPlayerInfo)
        val btnRepeat: ImageButton? = activity.findViewById(R.id.btnRepeat)
        val btnShuffle: ImageButton? = activity.findViewById(R.id.btnShuffle)
        val btnPlay: ImageButton? = activity.findViewById(R.id.btnPlay)
        val btnClose: ImageButton? = activity.findViewById(R.id.btnClose)
        val btnPrevious: ImageButton? = activity.findViewById(R.id.btnPrevious)
        val btnSkip: ImageButton? = activity.findViewById(R.id.btnSkip)
        val btnLike: ImageButton? = activity.findViewById(R.id.btnLike)
        val sbProgress: SeekBar? = activity.findViewById(R.id.sbProgress)

        llPlayerInfo?.setOnClickListener {
            stop()
            val intent = Intent(context, BigPlayerActivity::class.java)
            context.startActivity(intent)
            context.overridePendingTransition(R.anim.slide_up, 0)
        }

        btnRepeat?.setOnClickListener {
            spotifyPlayer.changeRepeatMode()
        }

        btnShuffle?.setOnClickListener {
            spotifyPlayer.toggleShuffle()
        }

        btnPlay?.setOnClickListener {
            spotifyPlayer.togglePlay()
        }

        btnClose?.setOnClickListener {
            active = false
            context.finish()
        }

        btnPrevious?.setOnClickListener {
            spotifyPlayer.skipPrevious()
        }

        btnSkip?.setOnClickListener {
            spotifyPlayer.skipNext()
        }

        btnLike?.setOnClickListener {
            spotifyPlayer.getPlayerState { playerState ->
                val track = playerState.track
                if (track != null) {
                    spotifyPlayer.toggleLike(track.uri) { wasLiked ->
                        if (wasLiked) {
                            btnLike.setImageResource(R.drawable.ic_liked_48)
                        } else {
                            btnLike.setImageResource(R.drawable.ic_not_liked_48)
                        }
                    }
                }
            }
        }

        sbProgress?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    spotifyPlayer.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    var paused = false

    private fun shouldUpdate(playerState: PlayerState): Boolean {
        if (playerState.isPaused && !paused) {
            paused = true
            return true
        }

        if (!playerState.isPaused && paused) {
            paused = false
            return true
        }

        if (!playerState.isPaused && !paused) return true

        return false
    }

    private fun updatePlayerUI(context: Context, playerState: PlayerState) {
        val activity = context as Activity

        val btnRepeat: ImageButton? = activity.findViewById(R.id.btnRepeat)
        val btnShuffle: ImageButton? = activity.findViewById(R.id.btnShuffle)
        val btnPlay: ImageButton? = activity.findViewById(R.id.btnPlay)
        val btnLike: ImageButton? = activity.findViewById(R.id.btnLike)
        val btnClose: ImageButton? = activity.findViewById(R.id.btnClose)
        val btnOptions: ImageButton? = activity.findViewById(R.id.btnOptions)
        val ivPlayerCover: ImageView? = activity.findViewById(R.id.ivPlayerCover)
        val tvPlayerTitle: TextView? = activity.findViewById(R.id.tvPlayerTitle)
        val tvPlayerArtist: TextView? = activity.findViewById(R.id.tvPlayerArtist)
        val pbProgress: ProgressBar? = activity.findViewById(R.id.pbProgress)
        val sbProgress: SeekBar? = activity.findViewById(R.id.sbProgress)
        val tvPlayerCurrentProgress: TextView? = activity.findViewById(R.id.tvPlayerCurrentProgress)
        val tvPlayerMaxProgress: TextView? = activity.findViewById(R.id.tvPlayerMaxProgress)
        val tvPlayerDevice: TextView? = activity.findViewById(R.id.tvPlayerDevice)
        val tvPlayerContextTitle: TextView? = activity.findViewById(R.id.tvPlayerContextTitle)
        val tvPlayerContextSubtitle: TextView? = activity.findViewById(R.id.tvPlayerContextSubtitle)
        val vBackground: View? = activity.findViewById(R.id.vBackground)

        val miniPlayer: View? = activity.findViewById(R.id.miniPlayer)

        context.runOnUiThread {
            val track: Track? = playerState.track
            if (track != null && shouldUpdate(playerState)) {
                miniPlayer?.visibility = View.VISIBLE

                val trackDuration = track.duration
                val trackProgress = playerState.playbackPosition

                pbProgress?.max = trackDuration.toInt()
                pbProgress?.progress = trackProgress.toInt()
                sbProgress?.max = trackDuration.toInt()
                sbProgress?.progress = trackProgress.toInt()

                tvPlayerCurrentProgress?.text = trackProgress.toTimestampString()
                tvPlayerMaxProgress?.text = trackDuration.toTimestampString()

                tvPlayerTitle?.isSelected = true

                if (tvPlayerTitle?.text != track.name) {
                    if (tvPlayerContextTitle != null && tvPlayerContextSubtitle != null) {
                        spotifyPlayer.getPlayerContext { playerContext ->
                            tvPlayerContextTitle.text = playerContext.title
                            tvPlayerContextSubtitle.text = playerContext.subtitle
                        }
                    }

                    tvPlayerTitle?.text = track.name
                    tvPlayerArtist?.text = track.artist.name

                    ivPlayerCover?.loadImageFromUri(track.imageUri) {
                        if (vBackground != null) {
                            val dominantColor = ivPlayerCover.getDominantColor()

                            val gd = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(dominantColor, 0x000000, 0x000000))
                            gd.cornerRadius = 0f
                            vBackground.background = gd
                            val contrastColor = getContrastColor(dominantColor)
                            tvPlayerContextTitle?.setTextColor(contrastColor)
                            tvPlayerContextSubtitle?.setTextColor(contrastColor)
                            btnClose?.drawable?.setTint(contrastColor)
                            btnOptions?.drawable?.setTint(contrastColor)
                        }
                    }

                    if (btnLike != null) {
                        spotifyPlayer.getLibraryState(track.uri) { libraryState ->
                            if (libraryState.isAdded) {
                                btnLike.setImageResource(R.drawable.ic_liked_48)
                            } else {
                                btnLike.setImageResource(R.drawable.ic_not_liked_48)
                            }
                        }
                    }
                }

                if (btnPlay != null) {
                    if (playerState.isPaused) {
                        btnPlay.setImageResource(R.drawable.ic_baseline_play_arrow_48)
                    } else {
                        btnPlay.setImageResource(R.drawable.ic_baseline_pause_48)
                    }
                }

                if (btnShuffle != null) {
                    if (playerState.playbackOptions.isShuffling) {
                        btnShuffle.setImageResource(R.drawable.ic_shuffle_on_48)
                    } else {
                        btnShuffle.setImageResource(R.drawable.ic_shuffle_off_48)
                    }
                }

                if (btnRepeat != null) {
                    when (playerState.playbackOptions.repeatMode) {
                        0 -> {
                            btnRepeat.setImageResource(R.drawable.ic_repeat_off_48)
                        }
                        1 -> {
                            btnRepeat.setImageResource(R.drawable.ic_repeat_one_48)
                        }
                        2 -> {
                            btnRepeat.setImageResource(R.drawable.ic_repeat_all_48)
                        }
                    }
                }
            } else {
//                miniPlayer.visibility = View.GONE
            }
        }
    }
}