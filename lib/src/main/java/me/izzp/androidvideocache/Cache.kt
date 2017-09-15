package me.izzp.androidvideocache

/**
 * Created by zzp on 2017-07-15.
 */
interface Cache {
    fun available(): Long

    fun seek(offset: Long)

    fun write(buffer: ByteArray, size: Int)

    fun read(buffer: ByteArray): Int

    fun complete()
}