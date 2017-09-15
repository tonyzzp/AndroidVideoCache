package me.izzp.androidvideocache

import java.io.RandomAccessFile

/**
 * Created by zzp on 2017-07-15.
 */
class FileSource(val videoCache: AndroidVideoCache, val url: String) : Source {

    private val accessFile: RandomAccessFile

    init {
        val file = videoCache.storage.getCacheFile(url)
        if (file.exists()) {
            throw RuntimeException("文件不存在:" + file)
        }
        accessFile = RandomAccessFile(file, "r")
    }

    override fun open(offset: Long) {
        accessFile.seek(offset)
    }

    override fun read(buff: ByteArray): Int {
        return accessFile.read(buff)
    }

    override fun close() {
        accessFile.close()
    }
}