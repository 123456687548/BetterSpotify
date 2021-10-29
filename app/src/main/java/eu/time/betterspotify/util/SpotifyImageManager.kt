package eu.time.betterspotify.util

import android.graphics.Bitmap
import android.util.LruCache
import android.widget.ImageView
import com.spotify.protocol.types.ImageUri
import eu.time.betterspotify.R
import eu.time.betterspotify.spotify.SpotifyPlayer

class SpotifyImageManager private constructor() {
    private val memoryCache: LruCache<String, Bitmap>

    companion object {
        private val INSTANCE = SpotifyImageManager()

        fun getInstance(): SpotifyImageManager = INSTANCE
    }

    init {
        memoryCache = object : LruCache<String, Bitmap>(getCacheSize()) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                return bitmap.byteCount / 1024
            }
        }
    }

    //todo cache to disk?
    fun loadBitmap(imageUri: ImageUri, imageView: ImageView, callback: () -> Unit = {}) {
        if (imageUri.raw == null) return
        if (imageUri.raw!!.contains("localfileimage")) {
            imageView.setImageResource(R.drawable.ic_no_cover_24)
            callback()
        }

        val bitmap: Bitmap? = getBitmapFromMemCache(imageUri.raw!!)?.also {
            imageView.setImageBitmap(it)
            callback()
        } ?: run {
            SpotifyPlayer.getInstance(imageView.context).getImage(imageUri) {
                memoryCache.put(imageUri.raw, it)
                imageView.setImageBitmap(it)
                callback()
            }
            null
        }
    }

    private fun getBitmapFromMemCache(imageKey: String): Bitmap? = memoryCache[imageKey]

    private fun getCacheSize(): Int {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        return maxMemory / 8
    }
}