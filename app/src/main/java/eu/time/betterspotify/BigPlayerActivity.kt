package eu.time.betterspotify

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.*
import com.spotify.protocol.types.PlayerState
import com.spotify.protocol.types.Track
import eu.time.betterspotify.spotify.data.SpotifyPlayer
import eu.time.betterspotify.util.loadImageFromUri
import eu.time.betterspotify.util.toTimestampString

import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener


class BigPlayerActivity : AppCompatActivity() {
    private lateinit var spotifyPlayer: SpotifyPlayer
    private var activeColor = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_big_player)

        activeColor = Color.valueOf(getColor(R.color.green_900)).toArgb()
    }

    override fun onStart() {
        super.onStart()

        spotifyPlayer = SpotifyPlayer.getInstance()

        spotifyPlayer.connect(this)

        initPlayer()
    }

    private fun initPlayer() {
        val btnClose = findViewById<ImageButton>(R.id.btnClose)

        val btnRepeat = findViewById<ImageButton>(R.id.btnRepeat)
        val btnShuffle = findViewById<ImageButton>(R.id.btnShuffle)
        val btnPlay = findViewById<ImageButton>(R.id.btnPlay)
        val btnPrevious = findViewById<ImageButton>(R.id.btnPrevious)
        val btnSkip = findViewById<ImageButton>(R.id.btnSkip)
        val pbProgress = findViewById<SeekBar>(R.id.pbProgress)

        btnClose.setOnClickListener {
            finish()
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

        btnPrevious.setOnClickListener {
            SpotifyPlayer.getInstance().getRemote()?.playerApi?.skipPrevious()
        }

        btnSkip.setOnClickListener {
            SpotifyPlayer.getInstance().getRemote()?.playerApi?.skipNext()
        }

        pbProgress.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    SpotifyPlayer.getInstance().getRemote()?.playerApi?.seekTo(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        val mainHandler = Handler(Looper.getMainLooper())

        val context = this

        mainHandler.post(object : Runnable {
            @SuppressLint("SetTextI18n")
            override fun run() {
                spotifyPlayer.connect(context)

                SpotifyPlayer.getInstance().getRemote()?.playerApi
                    ?.subscribeToPlayerState()
                    ?.setEventCallback { playerState: PlayerState ->
                        val track: Track? = playerState.track
                        if (track != null) {
                            val ivPlayerCover = findViewById<ImageView>(R.id.ivBigPlayerCover)
                            val tvPlayerTitle = findViewById<TextView>(R.id.tvBigPlayerTitle)
                            val tvPlayerArtist = findViewById<TextView>(R.id.tvBigPlayerArtist)
                            val pbProgress = findViewById<SeekBar>(R.id.pbProgress)
                            val tvBigPlayerCurrentProgress = findViewById<TextView>(R.id.tvBigPlayerCurrentProgress)
                            val tvBigPlayerMaxProgress = findViewById<TextView>(R.id.tvBigPlayerMaxProgress)
                            val tvPlayerDevice = findViewById<TextView>(R.id.tvBigPlayerDevice)
                            val tvPlayerContext = findViewById<TextView>(R.id.tvPlayerContext)

                            val trackDuration = track.duration
                            val trackProgress = playerState.playbackPosition

                            pbProgress.max = trackDuration.toInt()
                            pbProgress.progress = trackProgress.toInt()


                            tvBigPlayerCurrentProgress.text = trackProgress.toTimestampString()
                            tvBigPlayerMaxProgress.text = trackDuration.toTimestampString()

                            tvPlayerTitle.isSelected = true

                            if (tvPlayerTitle.text != track.name) {
                                SpotifyPlayer.getInstance().getRemote()?.playerApi?.subscribeToPlayerContext()?.setEventCallback { playerContext ->
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
                        }
                    }

                mainHandler.postDelayed(this, 1000)
            }
        })
    }

    override fun onStop() {
        super.onStop()

//        if (::spotifyPlayer.isInitialized) {
//            spotifyPlayer.disconnect()
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::spotifyPlayer.isInitialized) {
            spotifyPlayer.disconnect()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, R.anim.slide_down)
    }
}