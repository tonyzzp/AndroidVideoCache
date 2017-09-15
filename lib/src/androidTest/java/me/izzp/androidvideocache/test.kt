package me.izzp.androidvideocache

import android.support.test.InstrumentationRegistry
import org.junit.Test
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset

class Test {

    private fun open(url: URL, len: Int = 1024 * 2) {
        val conn = url.openConnection() as HttpURLConnection
        conn.inputStream.read(kotlin.ByteArray(len))
        conn.disconnect()
    }

    @Test
    fun asfd() {
        val context = InstrumentationRegistry.getTargetContext()
        val waiter = Waiter()
        val videoCache = AndroidVideoCache()
        videoCache.setSorageDir(context.cacheDir)
        videoCache.clearCache()
        videoCache.start {
            waiter.resume()
        }
        waiter.await()
        val ourl = "http://tool.oschina.net/"
        val url = videoCache.getProxyUrl(ourl)
        open(url)
        open(url, 100)
        open(url, 2055)
        val bytes = url.openConnection().getInputStream().use { it.readBytes() }
        val md5 = md5(bytes)
        val s = bytes.toString(Charset.defaultCharset())

        println(bytes.size)

        assert(bytes.size == 30886)
        assert(md5 == "4a6ea8a33c75b33614cede1352a210")
        assert(s.first() == '<')
        assert(s.last() == '>')
        assert(s.contains("分享按钮"))
        println("缓存成功: ${videoCache.hasCache(ourl)}")
    }
}