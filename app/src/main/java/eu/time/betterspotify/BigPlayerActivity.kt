package eu.time.betterspotify

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.*
import com.spotify.protocol.types.PlayerState
import com.spotify.protocol.types.Track
import eu.time.betterspotify.spotify.SpotifyPlayer
import eu.time.betterspotify.util.loadImageFromUri
import eu.time.betterspotify.util.toTimestampString

import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BigPlayerActivity : AppCompatActivity() {
    private lateinit var spotifyPlayer: SpotifyPlayer
    private var activeColor = -1

    private lateinit var btnClose: ImageButton
    private lateinit var btnRepeat: ImageButton
    private lateinit var btnShuffle: ImageButton
    private lateinit var btnPlay: ImageButton
    private lateinit var btnPrevious: ImageButton
    private lateinit var btnSkip: ImageButton
    private lateinit var ivPlayerCover: ImageView
    private lateinit var tvPlayerTitle: TextView
    private lateinit var tvPlayerArtist: TextView
    private lateinit var pbProgress: SeekBar
    private lateinit var tvBigPlayerCurrentProgress: TextView
    private lateinit var tvBigPlayerMaxProgress: TextView
    private lateinit var tvPlayerDevice: TextView
    private lateinit var tvPlayerContext: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_big_player)

        btnClose = findViewById(R.id.btnClose)
        btnRepeat = findViewById(R.id.btnRepeat)
        btnShuffle = findViewById(R.id.btnShuffle)
        btnPlay = findViewById(R.id.btnPlay)
        btnPrevious = findViewById(R.id.btnPrevious)
        btnSkip = findViewById(R.id.btnSkip)
        ivPlayerCover = findViewById(R.id.ivBigPlayerCover)
        tvPlayerTitle = findViewById(R.id.tvBigPlayerTitle)
        tvPlayerArtist = findViewById(R.id.tvBigPlayerArtist)
        pbProgress = findViewById(R.id.pbProgress)
        tvBigPlayerCurrentProgress = findViewById(R.id.tvBigPlayerCurrentProgress)
        tvBigPlayerMaxProgress = findViewById(R.id.tvBigPlayerMaxProgress)
        tvPlayerDevice = findViewById(R.id.tvBigPlayerDevice)
        tvPlayerContext = findViewById(R.id.tvPlayerContext)

        activeColor = Color.valueOf(getColor(R.color.green_900)).toArgb()
    }

    override fun onStart() {
        super.onStart()

        spotifyPlayer = SpotifyPlayer.getInstance(this)

        initPlayer()
    }

    private fun initPlayer() {
        var active = true

        btnClose.setOnClickListener {
            active = false
            finish()
        }

        btnRepeat.setOnClickListener {
            spotifyPlayer.changeRepeatMode()
        }

        btnShuffle.setOnClickListener {
            spotifyPlayer.toggleShuffle()
        }

        btnPlay.setOnClickListener {
            spotifyPlayer.togglePlay()
        }

        btnPrevious.setOnClickListener {
            spotifyPlayer.skipPrevious()
        }

        btnSkip.setOnClickListener {
            spotifyPlayer.skipNext()
        }

        pbProgress.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    spotifyPlayer.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        val mainHandler = Handler(Looper.getMainLooper())

        mainHandler.post(object : Runnable {
            override fun run() {
                CoroutineScope(Dispatchers.IO).launch {
                    spotifyPlayer.getPlayerState { playerState ->
                        updateBigPlayer(playerState)
                    }
                }

                if (active) {
                    mainHandler.postDelayed(this, 10)
                }
            }
        })
    }

    private fun updateBigPlayer(playerState: PlayerState) {
        runOnUiThread {
            val track: Track? = playerState.track
            if (track != null) {
                val trackDuration = track.duration
                val trackProgress = playerState.playbackPosition

                pbProgress.max = trackDuration.toInt()
                pbProgress.progress = trackProgress.toInt()

                tvBigPlayerCurrentProgress.text = trackProgress.toTimestampString()
                tvBigPlayerMaxProgress.text = trackDuration.toTimestampString()

                tvPlayerTitle.isSelected = true

                if (tvPlayerTitle.text != track.name) {
                    spotifyPlayer.getPlayerContext { playerContext ->
                        tvPlayerContext.text = playerContext.title
                    }
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

                when (spotifyPlayer.getRepeatMode(playerState)) {
                    SpotifyPlayer.RepeatMode.NONE -> {
                        btnRepeat.setImageResource(R.drawable.ic_baseline_repeat_48)
                        btnRepeat.clearColorFilter()
                    }
                    SpotifyPlayer.RepeatMode.REPEAT_ONE -> {
                        btnRepeat.setImageResource(R.drawable.ic_baseline_repeat_one_48)
                    }
                    SpotifyPlayer.RepeatMode.REPEAT_ALL -> {
                        btnRepeat.setImageResource(R.drawable.ic_baseline_repeat_48)
                        btnRepeat.setColorFilter(activeColor)
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (::spotifyPlayer.isInitialized) {
            spotifyPlayer.disconnect()
        }
    }

    override fun onStop() {
        super.onStop()

//        if (::spotifyPlayer.isInitialized) {
//            spotifyPlayer.disconnect()
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
//        if (::spotifyPlayer.isInitialized) {
//            spotifyPlayer.disconnect()
//        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, R.anim.slide_down)
    }
}