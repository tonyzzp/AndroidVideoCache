package me.izzp.androidvideocache

import java.io.File

/**
 * Created by zzp on 2017-07-15.
 */
class Storage(val videoCache: AndroidVideoCache) {

    var cacheDir: File = File(".")

    fun getFileName(url: String): String {
        return md5(url)
    }

    fun getCacheFile(url: String): File {
        val name = getFileName(url)
        val file = File(cacheDir, "$name.cache")
        return file
    }

    fun getTmpFile(url: String): File {
        val name = getFileName(url)
        val file = File(cacheDir, "$name.tmp")
        return file
    }
}