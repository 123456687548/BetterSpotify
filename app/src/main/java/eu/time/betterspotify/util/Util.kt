package eu.time.betterspotify.util

import android.widget.ImageView
import com.spotify.protocol.types.ImageUri
import com.squareup.picasso.Picasso
import eu.time.betterspotify.spotify.data.SpotifyApi
import eu.time.betterspotify.spotify.data.SpotifyPlayer
import java.math.BigInteger
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

fun String.sha256(): String {
    val md = MessageDigest.getInstance("SHA-256")
    return BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32, '0')
}

fun String.newSha256(): ByteArray {
    val bytes = this.toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    return md.digest(bytes)
}

fun ImageView.loadImageFromUrl(url: String) {
    Picasso.get().load(url).into(this)
}

fun ImageView.loadImageFromUri(uri: ImageUri) {

    SpotifyImageManager.getInstance().loadBitmap(uri, this)

//    SpotifyPlayer.getInstance().getRemote()?.imagesApi?.getImage(uri)?.setResultCallback {
//        setImageBitmap(it)
//    }
}

fun Long.toTimestampString(): String {
    val hours = TimeUnit.MILLISECONDS.toHours(this).toInt()
    val minutes = (TimeUnit.MILLISECONDS.toMinutes(this) % 60).toInt()
    val seconds = (TimeUnit.MILLISECONDS.toSeconds(this) % 60).toInt()

    val hoursString = if (hours == 0) "" else if (hours < 10) "0${hours}:" else "$hours:"
//    val minutesString = if(minutes < 10) "0$minutes:" else "$minutes:"
    val minutesString = "$minutes:"
    val secondsString = if(seconds < 10) "0$seconds" else "$seconds"

    return "$hoursString$minutesString$secondsString"
}