package downloader

import cli.writeln
import java.io.ByteArrayOutputStream
import java.io.Writer
import java.net.HttpURLConnection
import java.security.MessageDigest
import kotlin.math.min

data class DownloadConfig(
    val chunkSize: Int = 65536,         // 64 KB
    val timeout: Int = 5000,            // 5 seconds for connection/read
    val maxRetries: Int = 3,            // Number of attempts per chunk
    val maxConsecutiveEmpty: Int = 3,   // Maximum allowed consecutive empty responses
    val retryDelay: Long = 100L         // Delay between retries in milliseconds
)

fun sha256(data: ByteArray): String =
    MessageDigest.getInstance("SHA-256")
        .digest(data)
        .joinToString("") { "%02x".format(it) }

@Throws(DownloadException::class)
fun getExpectedLengthAndInitialChunk(url: String, timeout: Int, progress: Writer): Pair<Int, ByteArray> {
    var connection: HttpURLConnection? = null
    try {
        connection = createConnection(url, "GET", timeout)
        val responseCode = connection.responseCode
        progress.writeln("Response code: $responseCode")
        if (responseCode != 200 && responseCode != 206) {
            throw DownloadException("Server responded with HTTP code $responseCode.")
        }

        val contentLengthStr = connection.getHeaderField("Content-Length")
            ?: throw DownloadException("Server did not return a Content-Length header.")

        val expectedLength = contentLengthStr.toIntOrNull()
            ?: throw DownloadException("Invalid Content-Length header value: '$contentLengthStr'. Expected a numeric value.")

        val initialChunk = connection.inputStream.use { it.readBytes() }
        progress.writeln("Expected total length: $expectedLength bytes. Initial chunk size: ${initialChunk.size} bytes.")
        return Pair(expectedLength, initialChunk)
    } catch (e: Exception) {
        throw DownloadException("Failed initial request: ${e.message}.", e)
    } finally {
        connection?.disconnect()
    }
}

@Throws(DownloadException::class)
fun downloadGlitchyFile(
    url: String,
    config: DownloadConfig = DownloadConfig(),
    progress: Writer
): ByteArray {
    val (expectedLength, initialChunk) = getExpectedLengthAndInitialChunk(url, config.timeout, progress)
    val output = ByteArrayOutputStream()
    output.write(initialChunk)
    var offset = initialChunk.size
    progress.writeln("Initial chunk received: ${initialChunk.size} bytes.")

    var consecutiveEmptyFailures = 0

    while (offset < expectedLength) {
        val end = min(offset + config.chunkSize, expectedLength)
        var chunk: ByteArray? = null

        for (attempt in 1..config.maxRetries) {
            var conn: HttpURLConnection? = null
            try {
                conn = createConnection(url, "GET", config.timeout)
                conn.setRequestProperty("Range", "bytes=$offset-$end")
                chunk = conn.inputStream.use { it.readBytes() }
                if (chunk.isNotEmpty()) {
                    break
                } else {
                    progress.writeln("Received empty chunk at offset $offset, attempt $attempt/${config.maxRetries}.")
                }
            } catch (e: Exception) {
                progress.writeln("Error downloading chunk at offset $offset, attempt $attempt/${config.maxRetries}: ${e.message}")
            } finally {
                conn?.disconnect()
            }
            Thread.sleep(config.retryDelay)
        }

        if (chunk == null || chunk.isEmpty()) {
            consecutiveEmptyFailures++
            progress.writeln("Consecutive empty responses: $consecutiveEmptyFailures")
            if (consecutiveEmptyFailures >= config.maxConsecutiveEmpty) {
                throw DownloadException("Max consecutive empty responses reached at offset $offset. Aborting download.")
            }
        } else {
            consecutiveEmptyFailures = 0
            output.write(chunk)
            offset += chunk.size
            progress.writeln("Downloaded chunk of size ${chunk.size} bytes, new offset: $offset")
        }
    }

    val downloaded = output.toByteArray()
    if (downloaded.size != expectedLength) {
        progress.writeln("Warning: Downloaded size (${downloaded.size}) does not match expected size ($expectedLength). Data may be incomplete.")
    }
    return downloaded
}

@Throws(DownloadException::class)
fun downloadAndVerify(url: String, expectedHash: String, progress: Writer): String {
    val downloadedData = downloadGlitchyFile(url, progress = progress)
    val computedHash = sha256(downloadedData)
    return if (computedHash.equals(expectedHash, ignoreCase = true)) {
        "Success: Downloaded data is correct (hash matches)!"
    } else {
        "Error: Hash mismatch! Expected: $expectedHash, computed: $computedHash"
    }
}
