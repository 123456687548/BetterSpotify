package eu.time.betterspotify.util

import java.math.BigInteger
import java.security.MessageDigest

fun String.sha256(): String {
    val md = MessageDigest.getInstance("SHA-256")
    return BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32, '0')
}

fun String.newSha256():ByteArray{
    val bytes = this.toString().toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    return md.digest(bytes)
}