package me.izzp.androidvideocache

import android.os.Handler
import android.os.Looper
import java.io.File
import java.security.MessageDigest

/**
 * Created by zzp on 2017-07-15.
 */

object H : Handler(Looper.getMainLooper())

class Waiter {

    private val lock = java.lang.Object()
    private var resumed = false

    fun resume() {
        resumed = true
        synchronized(lock) {
            lock.notify()
        }
    }

    fun await() {
        if (resumed) {
            return
        }
        synchronized(lock) {
            lock.wait()
        }
    }
}

inline fun runOnMainThread(crossinline block: () -> Unit) {
    H.post { block() }
}

fun md5(bytes: ByteArray): String {
    val md = MessageDigest.getInstance("MD5")
    val bytes = md.digest(bytes)
    val sb = StringBuilder()
    bytes.map { (it.toInt()) and 0xff }.forEach { sb.append(it.toString(16)) }
    return sb.toString().toLowerCase()
}

fun md5(s: String): String {
    return md5(s.toByteArray())
}

fun <K, E> Map<K, List<E>>.singleMap() = mapValues { it.value[0] }

fun File.mkParentDirs() {
    val f = absoluteFile.parentFile
    if (!f.exists()) {
        f.mkdirs()
    }
}

fun File.deleteChildren() {
    if (!isDirectory) {
        return
    }

    fun delete(f: File) {
        if (f.isFile) {
            f.delete()
        } else if (f.isDirectory) {
            f.listFiles()?.forEach(::delete)
        }
    }

    listFiles()?.forEach {
        delete(it)
    }
}