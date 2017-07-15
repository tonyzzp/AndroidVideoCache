package me.izzp.androidvideocache

/**
 * Created by zzp on 2017-07-15.
 */

interface Source {
    fun open(offset: Long)

    fun read(buff: ByteArray): Int

    fun close()
}