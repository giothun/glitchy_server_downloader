package downloader

import java.nio.charset.StandardCharsets
import kotlin.test.Test
import kotlin.test.assertEquals


class DownloaderTest {

    @Test
    fun testSha256() {
        val data = "The quick brown fox jumps over the lazy dog".toByteArray(StandardCharsets.UTF_8)
        val expected = "d7a8fbb307d7809469ca9abcb0082e4f8d5651e46d3cdb762d02d0bf37c9e592"
        assertEquals(expected, sha256(data))
    }

}
