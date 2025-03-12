package downloader

import java.nio.charset.StandardCharsets
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DownloaderTest {

    @Test
    fun testServerNotRunning() {
        val writer = System.out.writer()
        assertFailsWith<DownloadException>("Expected exception when connecting to a non-running server") {
            getExpectedLengthAndInitialChunk("http://127.0.0.1:9999/", 2000, writer)
        }
    }

    @Test
    fun testSha256WithKnownValue() {
        val data = "The quick brown fox jumps over the lazy dog".toByteArray(StandardCharsets.UTF_8)
        val expected = "d7a8fbb307d7809469ca9abcb0082e4f8d5651e46d3cdb762d02d0bf37c9e592"
        assertEquals(expected, sha256(data))
    }

    @Test
    fun testSha256EmptyInput() {
        val data = "".toByteArray(StandardCharsets.UTF_8)
        val expected = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        assertEquals(expected, sha256(data))
    }

    @Test
    fun testSha256Hello() {
        val data = "hello".toByteArray(StandardCharsets.UTF_8)
        val expected = "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824"
        assertEquals(expected, sha256(data))
    }
}
