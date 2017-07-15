package me.izzp.androidvideocache

import java.net.Socket

/**
 * Created by zzp on 2017-07-15.
 */


class Client(val socket: Socket) {
    val inStream = socket.getInputStream()
    val outStream = socket.getOutputStream()
    lateinit var url: String
    val headers = HashMap<String, String>()
    var offset = 0L

    fun close() {
        inStream?.close()
        outStream?.close()
        socket.close()
    }
}