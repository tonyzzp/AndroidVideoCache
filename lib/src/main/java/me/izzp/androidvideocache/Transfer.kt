package me.izzp.androidvideocache

import java.io.IOException

/**
 * Created by zzp on 2017-07-15.
 */
class Transfer(val videoCache: AndroidVideoCache, val client: Client) {

    private var responseSource: Source? = null
    private lateinit var fileCache: FileCache
    private val lock = Any()
    private val wait = java.lang.Object()
    private var isDownloading = false
    private var downloadThread: Thread? = null

    fun process() {
        val file = videoCache.storage.getCacheFile(client.url)
        if (file.exists()) {
            responseWithCache()
        } else {
            fileCache = FileCache(videoCache, client.url)
            log("fileCache:${fileCache.accessFile}")
            continueDownload()
            sendResponseHeader(client.offset)
            if (fileCache.available() > client.offset) {
                val buff = ByteArray(1024 * 4)
                var len = 0
                var offset = client.offset

                fun read() {
                    synchronized(lock) {
                        fileCache.seek(offset)
                        len = fileCache.read(buff)
                    }
                    if (len > 0) {
                        offset += len
                    }
                }

                fun canRead() = fileCache.available() - offset > 1024 * 2

                read()
                while (len > -1) {
                    client.outStream.write(buff, 0, len)
                    if (canRead()) {
                        read()
                    } else {
                        log("cache is unavailable waiting..")
                        len = 0
                        synchronized(wait) {
                            try {
                                wait.wait(2000)
                            } catch(e: Exception) {
                            }
                        }
                        if (canRead()) {
                            read()
                        }
                    }
                }
                finish()
            } else {
                responseWithHttp()
            }
        }
    }

    private fun responseWithCache() {
        log("responseWithCache")
        sendResponseHeader(client.offset)
        responseSource = FileSource(videoCache, client.url)
        val source = responseSource!!
        source.open(client.offset)
        val buff = ByteArray(1024 * 4)
        while (true) {
            val len = source.read(buff)
            if (len > -1) {
                client.outStream.write(buff, 0, len)
            } else {
                break
            }
        }
        finish()
    }

    private fun responseWithHttp() {
        sendResponseHeader(client.offset)
        responseSource = HttpSource(client.url)
        val source = responseSource!!
        source.open(client.offset)
        val buff = ByteArray(1024 * 4)
        while (true) {
            val len = source.read(buff)
            if (len > -1) {
                client.outStream.write(buff, 0, len)
            } else {
                break
            }
        }
        finish()
    }

    private fun sendResponseHeader(offset: Long) {
        log("sendResponseHeader: offset=$offset")
        val writer = client.outStream.writer()
        with(writer) {
            if (offset == 0L) {
                write("HTTP/1.1 200 OK\r\n")
            } else {
                write("HTTP/1.1 206 Partial Content\r\n")
            }
            write("Content-Type: video/*\r\n")
            if (offset > 0L) {
                write("Content-Range: bytes $offset-\r\n")
            }
            write("\r\n")
            flush()
        }
    }

    fun finish() {
        log("transfer.finish")
        responseSource?.close()
        client.close()
        isDownloading = false
        downloadThread?.interrupt()
    }

    private fun continueDownload() {
        downloadThread = Thread {
            log("continueDownload")
            var source: Source? = null
            try {
                isDownloading = true
                var offset = fileCache.available()
                source = HttpSource(client.url)
                source.open(offset)
                val buff = ByteArray(1024 * 4)
                var len = source.read(buff)
                while (isDownloading && len > -1) {
                    synchronized(lock) {
                        fileCache.seek(offset)
                        fileCache.write(buff, len)
                    }
                    synchronized(wait) {
                        wait.notify()
                    }
                    offset += len
                    len = source.read(buff)
                }
                synchronized(lock) {
                    fileCache.complete()
                }
            } catch(e: IOException) {
                e.printStackTrace()
            } finally {
                isDownloading = false
                source?.close()
                finish()
            }
        }
        downloadThread!!.start()
    }
}