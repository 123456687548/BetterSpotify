package eu.time.betterspotify.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.ImageView
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.toColor
import com.spotify.protocol.types.ImageUri
import com.spotify.protocol.types.Track
import com.squareup.picasso.Picasso
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import kotlin.math.abs

fun String.sha256(): ByteArray {
    val bytes = this.toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    return md.digest(bytes)
}

fun ImageView.loadImageFromUrl(url: String) {
    Picasso.get().load(url).into(this)
}

fun ImageView.loadImageFromUri(uri: ImageUri, callback: () -> Unit = {}) {
    SpotifyImageManager.getInstance().loadBitmap(uri, this, callback)
}

fun ImageView.getDominantColor(): Int {
    val bitmap = drawable.toBitmap()
    val height = bitmap.height
    val width = bitmap.width

    val colors = hashMapOf<Color, Int>()

    for (x in 0 until width step 20) {
        for (y in 0 until height step 20) {
            val pixel = bitmap.getPixel(x, y).toColor()

            if (isWhiteOrBlack(pixel)) continue

            val currentAmount = colors[pixel] ?: 0
            colors[pixel] = currentAmount + 1
        }
    }

    var mostOccurringColor = Color.BLACK.toColor()
    var biggestAmount = 0

    colors.entries.forEach { (color, amount) ->
        if (biggestAmount < amount) {
            mostOccurringColor = color
            biggestAmount = amount
        }
    }

    return mostOccurringColor.toArgb()
}

fun <T> openActivity(context: Context, clazz: Class<T>) {
    val intent = Intent(context, clazz)
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
    context.startActivity(intent)
}

fun isWhiteOrBlack(color: Color): Boolean {
    val red = color.red()
    val green = color.green()
    val blue = color.blue()

    val added = red + green + blue
    val div = added / 3

    val redDiff = abs(red - div)
    val greenDiff = abs(green - div)
    val blueDiff = abs(blue - div)

    val combinedDiff = redDiff + greenDiff + blueDiff
    val avgCombinedDiff = combinedDiff / 3

    return avgCombinedDiff < 0.1
}

fun getContrastColor(color: Int): Int {
    val whiteContrast = ColorUtils.calculateContrast(Color.WHITE, color)
    val blackContrast = ColorUtils.calculateContrast(Color.BLACK, color)
    return if (whiteContrast > blackContrast) Color.WHITE else Color.BLACK
}

fun Color.similarTo(c: Color): Boolean {
    val distance = (c.red() - this.red()) * (c.red() - this.red()) + (c.green() - this.green()) * (c.green() - this.green()) + (c.blue() - this.blue()) * (c.blue() - this.blue())
    return distance > 0.2
}

fun Int.toTimestampString(): String {
    return this.toLong().toTimestampString()
}

fun Long.toTimestampString(): String {
    val hours = TimeUnit.MILLISECONDS.toHours(this).toInt()
    val minutes = (TimeUnit.MILLISECONDS.toMinutes(this) % 60).toInt()
    val seconds = (TimeUnit.MILLISECONDS.toSeconds(this) % 60).toInt()

    val hoursString = if (hours == 0) "" else if (hours < 10) "0${hours}:" else "$hours:"
//    val minutesString = if(minutes < 10) "0$minutes:" else "$minutes:"
    val minutesString = "$minutes:"
    val secondsString = if (seconds < 10) "0$seconds" else "$seconds"

    return "$hoursString$minutesString$secondsString"
}

fun Track.getArtistsString(): String = this.artists.joinToString(", ") { it.name }