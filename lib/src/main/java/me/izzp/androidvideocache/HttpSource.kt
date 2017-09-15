package me.izzp.androidvideocache

import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Created by zzp on 2017-07-15.
 */
class HttpSource(surl: String) : Source {

    private val url: URL
    private var conn: HttpURLConnection? = null
    private var inStream: InputStream? = null

    init {
        url = URL(surl)
    }

    override fun open(offset: Long) {
        conn = url.openConnection() as HttpURLConnection
        val conn = conn!!
        conn.connectTimeout = 5000
        conn.readTimeout = 10000
        if (offset > 0) {
            conn.setRequestProperty("RANGE", "bytes=$offset-")
        }
        conn.connect()
        inStream = conn.inputStream
    }

    override fun read(buff: ByteArray): Int {
        return inStream!!.read(buff)
    }

    override fun close() {
        inStream?.close()
        conn?.disconnect()
        inStream = null
        conn = null
    }
}