package eu.time.betterspotify.controllers

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
import android.os.Bundle
import eu.time.betterspotify.R
import eu.time.betterspotify.activities.ArtistActivity
import eu.time.betterspotify.activities.BigPlayerActivity
import eu.time.betterspotify.activities.PlaylistActivity
import eu.time.betterspotify.util.*
import android.widget.Toast
import com.spotify.protocol.types.Artist
import eu.time.betterspotify.spotify.data.spotifyApi.*

class PlayerController private constructor() {
    companion object {
        private val INSTANCE = PlayerController()

        fun getInstance(): PlayerController {
            INSTANCE.stop()
            return INSTANCE
        }
    }

    private lateinit var spotifyPlayer: SpotifyPlayer

    private val mainHandler = Handler(Looper.getMainLooper())
    private lateinit var updateUIRunnable: UpdateUIRunnable

    private var lastTrack: Track? = null

    class UpdateUIRunnable(private val mainHandler: Handler, val updatePlayerUIFunc: () -> Unit) : Runnable {
        override fun run() {
            updatePlayerUIFunc()

            mainHandler.postDelayed(this, 50)
        }
    }

    fun start(context: Context) {
        lastTrack = null

        spotifyPlayer = SpotifyPlayer.getInstance(context)

        initListeners(context)

        updateUIRunnable = UpdateUIRunnable(mainHandler) {
            CoroutineScope(Dispatchers.IO).launch {
                spotifyPlayer.getPlayerState { playerState ->
                    updatePlayerUI(context, playerState)
                }
            }
        }

        mainHandler.post(updateUIRunnable)
    }

    fun stop() {
        if (::updateUIRunnable.isInitialized) {
            mainHandler.removeCallbacks(updateUIRunnable)
        }
    }

    private fun initListeners(context: Context) {
        val activity = context as Activity

        val llPlayerInfo: LinearLayout? = activity.findViewById(R.id.llPlayerInfo)
        val llPlayerInfoBig: LinearLayout? = activity.findViewById(R.id.llPlayerInfoBig)
        val btnRepeat: ImageButton? = activity.findViewById(R.id.btnRepeat)
        val btnShuffle: ImageButton? = activity.findViewById(R.id.btnShuffle)
        val btnPlay: ImageButton? = activity.findViewById(R.id.btnPlay)
        val btnClose: ImageButton? = activity.findViewById(R.id.btnClose)
        val btnOptions: ImageButton? = activity.findViewById(R.id.btnOptions)
        val btnPrevious: ImageButton? = activity.findViewById(R.id.btnPrevious)
        val btnSkip: ImageButton? = activity.findViewById(R.id.btnSkip)
        val btnLike: ImageButton? = activity.findViewById(R.id.btnLike)
        val btnShare: ImageButton? = activity.findViewById(R.id.btnShare)
        val btnQueueBigPlayer: ImageButton? = activity.findViewById(R.id.btnQueueBigPlayer)
        val btnAddToTemp: ImageButton? = activity.findViewById(R.id.btnAddToTemp)
        val sbProgress: SeekBar? = activity.findViewById(R.id.sbProgress)
        val tvPlayerContextTitle: TextView? = activity.findViewById(R.id.tvPlayerContextTitle)
        val tvPlayerContextSubtitle: TextView? = activity.findViewById(R.id.tvPlayerContextSubtitle)

        llPlayerInfo?.setOnClickListener {
            stop()
            val intent = Intent(context, BigPlayerActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            context.startActivity(intent)
            context.overridePendingTransition(R.anim.slide_up, 0)
        }

        llPlayerInfoBig?.setOnClickListener {
            if (lastTrack != null) {
                stop()
                ArtistActivity.openArtist(it.context, lastTrack!!.artist)
            }
        }

        tvPlayerContextTitle?.setOnClickListener {
            openPlayerContext(context, lastTrack)
        }

        tvPlayerContextSubtitle?.setOnClickListener {
            openPlayerContext(context, lastTrack)
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
            stop()
            context.finish()
        }

        btnOptions?.setOnClickListener {
            if (lastTrack != null) {
                MenuController.getInstance().openMenuTrack(context, it, lastTrack!!)
            }
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

        btnShare?.setOnClickListener {
            lastTrack?.share(context)
        }

        btnQueueBigPlayer?.setOnClickListener {
            if (lastTrack != null) {
                val context = it.context

                SpotifyPlayer.getInstance(context).queueUri(lastTrack!!.uri) {
                    Toast.makeText(context, "${lastTrack!!.name} queued!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnAddToTemp?.setOnClickListener {
            if (lastTrack != null) {
                val context = it.context

                addToTemp(context, lastTrack!!.uri, {
                    Toast.makeText(context, "${lastTrack!!.name} added to temp Playlist!", Toast.LENGTH_SHORT).show()
                })
            }
        }

        sbProgress?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    updateCurrentTrackProgressText(activity)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                spotifyPlayer.seekTo(seekBar.progress)
            }
        })
    }

    private fun updatePlayerUI(context: Context, playerState: PlayerState) {
        val activity = context as Activity

        val tvPlayerTitle: TextView? = activity.findViewById(R.id.tvPlayerTitle)
        val miniPlayer: View? = activity.findViewById(R.id.miniPlayer)

        context.runOnUiThread {
            val currentTrack: Track? = playerState.track
            if (currentTrack != null) {
                miniPlayer?.visibility = View.VISIBLE

                updateTrackProgress(activity, playerState)

                if (currentTrack != lastTrack) {
                    onTrackChange(activity, playerState)
                }

                tvPlayerTitle?.isSelected = true
            }

            updateButtonImages(activity, playerState)
        }
    }

    private fun onTrackChange(activity: Activity, playerState: PlayerState) {
        val currentTrack = playerState.track

        lastTrack = currentTrack

        updateTrackInfo(activity, currentTrack)
        updateContextText(activity)
        updateCover(activity)
        updateDeviceInfo(activity)
    }

    private fun updateDeviceInfo(activity: Activity) {
        val tvPlayerDevice: TextView? = activity.findViewById(R.id.tvPlayerDevice)

        if (tvPlayerDevice != null) {
            getPlayerState(activity, { ownPlayerState ->
                tvPlayerDevice.text = ownPlayerState.device.name
            })
        }
    }

    private fun updateTrackInfo(activity: Activity, currentTrack: Track) {
        val tvPlayerTitle: TextView? = activity.findViewById(R.id.tvPlayerTitle)
        val tvPlayerArtist: TextView? = activity.findViewById(R.id.tvPlayerArtist)

        tvPlayerTitle?.text = currentTrack.name
        tvPlayerArtist?.text = currentTrack.getArtistsString()

        updateMaxTrackProgress(activity, currentTrack)
    }

    private fun updateContextText(activity: Activity) {
        val tvPlayerContextTitle: TextView? = activity.findViewById(R.id.tvPlayerContextTitle)
        val tvPlayerContextSubtitle: TextView? = activity.findViewById(R.id.tvPlayerContextSubtitle)

        if (tvPlayerContextTitle != null && tvPlayerContextSubtitle != null) {
            spotifyPlayer.getPlayerContext { playerContext ->
                tvPlayerContextTitle.text = playerContext.title
                tvPlayerContextSubtitle.text = playerContext.subtitle
            }
        }
    }

    private fun updateCover(activity: Activity) {
        val ivPlayerCover: ImageView? = activity.findViewById(R.id.ivPlayerCover)

        ivPlayerCover?.loadImageFromUri(lastTrack!!.imageUri) {
            updateBackground(activity)
        }
    }

    private fun updateBackground(activity: Activity) {
        val vBackgroundBig: View? = activity.findViewById(R.id.vBackgroundBig)
        val vBackgroundMini: View? = activity.findViewById(R.id.vBackgroundMini)

        if (vBackgroundBig != null) {
            updateBackgroundBig(activity, vBackgroundBig)
        } else if (vBackgroundMini != null) {
            updateBackgroundMini(activity, vBackgroundMini)
        }
    }

    private fun updateBackgroundMini(activity: Activity, vBackgroundMini: View) {
        val ivPlayerCover: ImageView = activity.findViewById(R.id.ivPlayerCover)
        val tvPlayerTitle: TextView? = activity.findViewById(R.id.tvPlayerTitle)
        val tvPlayerArtist: TextView? = activity.findViewById(R.id.tvPlayerArtist)

        val dominantColor = ivPlayerCover.getDominantColor()

        val gd = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(dominantColor, 0x000000))
        gd.cornerRadius = 0f
        vBackgroundMini.background = gd
        val contrastColor = getContrastColor(dominantColor)
        tvPlayerTitle?.setTextColor(contrastColor)
        tvPlayerArtist?.setTextColor(contrastColor)
    }

    private fun updateBackgroundBig(activity: Activity, vBackgroundBig: View) {
        val ivPlayerCover: ImageView = activity.findViewById(R.id.ivPlayerCover)
        val btnClose: ImageButton? = activity.findViewById(R.id.btnClose)
        val btnOptions: ImageButton? = activity.findViewById(R.id.btnOptions)
        val tvPlayerContextTitle: TextView? = activity.findViewById(R.id.tvPlayerContextTitle)
        val tvPlayerContextSubtitle: TextView? = activity.findViewById(R.id.tvPlayerContextSubtitle)

        val dominantColor = ivPlayerCover.getDominantColor()

        val gd = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(dominantColor, 0x000000, 0x000000))
        gd.cornerRadius = 0f
        vBackgroundBig.background = gd
        val contrastColor = getContrastColor(dominantColor)
        tvPlayerContextTitle?.setTextColor(contrastColor)
        tvPlayerContextSubtitle?.setTextColor(contrastColor)
        btnClose?.drawable?.setTint(contrastColor)
        btnOptions?.drawable?.setTint(contrastColor)
        activity.window.statusBarColor = dominantColor
        if (contrastColor == Color.WHITE) {
            activity.window.decorView.systemUiVisibility = activity.window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        } else {
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    private fun updateButtonImages(activity: Activity, playerState: PlayerState) {
        val btnRepeat: ImageButton? = activity.findViewById(R.id.btnRepeat)
        val btnShuffle: ImageButton? = activity.findViewById(R.id.btnShuffle)
        val btnPlay: ImageButton? = activity.findViewById(R.id.btnPlay)
        val btnLike: ImageButton? = activity.findViewById(R.id.btnLike)

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

        if (btnLike != null) {
            spotifyPlayer.getLibraryState(playerState.track.uri) { libraryState ->
                if (libraryState.isAdded) {
                    btnLike.setImageResource(R.drawable.ic_liked_48)
                } else {
                    btnLike.setImageResource(R.drawable.ic_not_liked_48)
                }
            }
        }
    }

    private fun updateTrackProgress(activity: Activity, playerState: PlayerState) {
        updateCurrentTrackProgress(activity, playerState)
        updateCurrentTrackProgressText(activity)
    }

    private fun updateMaxTrackProgress(activity: Activity, currentTrack: Track) {
        val pbProgress: ProgressBar? = activity.findViewById(R.id.pbProgress)
        val sbProgress: SeekBar? = activity.findViewById(R.id.sbProgress)
        val tvPlayerMaxProgress: TextView? = activity.findViewById(R.id.tvPlayerMaxProgress)

        val trackDuration = currentTrack.duration

        pbProgress?.max = trackDuration.toInt()
        sbProgress?.max = trackDuration.toInt()
        tvPlayerMaxProgress?.text = trackDuration.toTimestampString()
    }

    private fun updateCurrentTrackProgress(activity: Activity, playerState: PlayerState) {
        val pbProgress: ProgressBar? = activity.findViewById(R.id.pbProgress)
        val sbProgress: SeekBar? = activity.findViewById(R.id.sbProgress)
        val trackProgress = playerState.playbackPosition

        pbProgress?.progress = trackProgress.toInt()
        sbProgress?.progress = trackProgress.toInt()

        updateCurrentTrackProgressText(activity)
    }

    private fun updateCurrentTrackProgressText(activity: Activity) {
        val tvPlayerCurrentProgress: TextView? = activity.findViewById(R.id.tvPlayerCurrentProgress)
        val sbProgress: SeekBar? = activity.findViewById(R.id.sbProgress)

        tvPlayerCurrentProgress?.text = sbProgress?.progress?.toTimestampString()
    }

    private fun openPlayerContext(context: Context, currentTrack: com.spotify.protocol.types.Track?) {
        SpotifyPlayer.getInstance(context).getPlayerContext { playerContext ->
            if (playerContext.uri == null) return@getPlayerContext

            val id = playerContext.uri.substringAfterLast(':')

            when (playerContext.type) {
                "artist" -> ArtistActivity.openArtistFromPlayerContext(context, id)
                "playlist" -> PlaylistActivity.openPlaylistFromPlayerContext(context, id, currentTrack)
            }
        }
    }
}