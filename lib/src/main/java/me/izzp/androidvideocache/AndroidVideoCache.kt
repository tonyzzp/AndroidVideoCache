package me.izzp.androidvideocache

import java.io.File
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.URL
import java.net.URLEncoder

/**
 * Created by zzp on 2017-07-15.
 */
class AndroidVideoCache {

    private var running = false
    private var thread: Thread? = null
    private lateinit var ss: ServerSocket
    private val cp = ClientProcessor(this)
    val storage = Storage(this)

    fun setSorageDir(dir: File) {
        storage.cacheDir = dir
    }

    fun start(completeCb: () -> Unit) {
        if (running) {
            return
        }
        running = true
        Thread {
            ss = ServerSocket()
            ss.bind(InetSocketAddress("127.0.0.1", 0))
            startAccept()
            runOnMainThread(completeCb)
        }.start()
    }

    fun shutdown() {
        running = false
        ss.close()
        thread?.interrupt()
        thread = null
    }

    fun clearCache() {
        storage.cacheDir.deleteChildren()
    }

    fun hasCache(url: String) = storage.getCacheFile(url).exists()

    private fun startAccept() {
        thread = Thread {
            while (running) {
                val s = ss.accept()
                log("client connect:${s.remoteSocketAddress}")
                cp.accept(s)
            }
        }
        thread!!.start()
    }

    fun getProxyUrl(url: String): URL {
        var result: URL
        val cacheFile = storage.getCacheFile(url)
        if (cacheFile.exists()) {
            result = cacheFile.toURI().toURL()
        } else {
            val address = ss.localSocketAddress as InetSocketAddress
            result = URL("http://${address.hostName}:${address.port}/?url=${URLEncoder.encode(url, "utf-8")}")
        }
        log("proxyUrl=$result")
        return result
    }
}