package eu.time.betterspotify.util

import android.graphics.Bitmap
import android.util.LruCache
import android.widget.ImageView
import com.spotify.protocol.types.ImageUri
import eu.time.betterspotify.spotify.data.SpotifyPlayer

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

    fun loadBitmap(imageUri: ImageUri, imageView: ImageView) {
        if(imageUri.raw == null) return

        val bitmap: Bitmap? = getBitmapFromMemCache(imageUri.raw!!)?.also {
            imageView.setImageBitmap(it)
        } ?: run {
            SpotifyPlayer.getInstance().getRemote()?.imagesApi?.getImage(imageUri)?.setResultCallback {
                memoryCache.put(imageUri.raw, it)
                imageView.setImageBitmap(it)
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