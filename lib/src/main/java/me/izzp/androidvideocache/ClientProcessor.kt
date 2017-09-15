package me.izzp.androidvideocache

import java.io.IOException
import java.net.Socket
import java.net.URLDecoder

/**
 * Created by zzp on 2017-07-15.
 */
class ClientProcessor(val videoCache: AndroidVideoCache) {

    private val clients = ArrayList<Client>()

    fun accept(socket: Socket) {
        val client = Client(socket)
        clients.add(client)
        Thread {
            startProcess(client)
        }.start()
    }

    fun shutdown() {
        clients.forEach {
            it.close()
        }
    }

    private fun startProcess(client: Client) {
        val reader = client.inStream.bufferedReader()

        fun processHeader(line: String) {
            if (line.startsWith("GET ")) {
                var line = line.substringAfter("GET ")
                line = line.substringBefore(" HTTP/")
                client.url = line.substringAfter("?url=", "")
                client.url = URLDecoder.decode(client.url, "utf-8")
            } else {
                val key = line.substringBefore(":", "").trim()
                val value = line.substringAfter(":", "").trim()
                if (key != "") {
                    client.headers[key] = value
                }
                if (key.equals("RANGE", true)) {
                    val v = value.substringAfter("bytes=", "").substringBefore("-", "")
                    client.offset = v.toLong()
                }
            }
        }

        while (true) {
            val line = reader.readLine()
            if (line == "") {
                break
            } else {
                processHeader(line)
            }
        }

        with(client) {
            log("url:$url")
            log("offset:$offset")
        }

        Thread {
            try {
                Transfer(videoCache, client).process()
            } catch(e: IOException) {
            }
            client.close()
            clients -= client
        }.start()
    }
}