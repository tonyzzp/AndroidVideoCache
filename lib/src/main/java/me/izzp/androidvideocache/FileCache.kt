package me.izzp.androidvideocache

import java.io.File
import java.io.RandomAccessFile

/**
 * Created by zzp on 2017-07-15.
 */
class FileCache(val videoCache: AndroidVideoCache, surl: String) : Cache {

    private val tmpFile: File
    var accessFile: RandomAccessFile
        private set

    init {
        val name = videoCache.storage.getFileName(surl)
        tmpFile = File(videoCache.storage.cacheDir, "$name.tmp")
        if (!tmpFile.exists()) {
            tmpFile.mkParentDirs()
            tmpFile.createNewFile()
        }
        accessFile = RandomAccessFile(tmpFile, "rw")
    }

    override fun available(): Long {
        return accessFile.length()
    }

    override fun seek(offset: Long) {
        accessFile.seek(offset)
    }

    override fun write(buffer: ByteArray, size: Int) {
        accessFile.write(buffer, 0, size)
    }

    override fun read(buffer: ByteArray): Int {
        return accessFile.read(buffer)
    }

    override fun complete() {
        accessFile.close()
        val name = tmpFile.name.replace(".tmp", ".cache")
        val dst = File(tmpFile.parentFile, name)
        tmpFile.renameTo(dst)
        accessFile = RandomAccessFile(dst, "rw")
        log("FileCache.complete: $dst, fileSize:${dst.length()}")
    }
}