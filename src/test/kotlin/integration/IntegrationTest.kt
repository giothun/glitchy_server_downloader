package integration

import downloader.downloadAndVerify
import downloader.DownloadException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Duration
import java.util.regex.Pattern

@Testcontainers
class GlitchyServerIntegrationTestExtra {

    @Container
    val serverContainer = GenericContainer("glitchy-server:latest")
        .withExposedPorts(8080)
        .waitingFor(
            Wait.forHttp("/")
                .forStatusCode(200)
                .withStartupTimeout(Duration.ofSeconds(60))
        )

    @Test
    fun `test hash mismatch scenario`() {
        val port = serverContainer.getMappedPort(8080)
        val host = serverContainer.host
        val url = "http://$host:$port/"

        val logs = serverContainer.logs
        val pattern = Pattern.compile("SHA-256 hash of the data:\\s*([a-fA-F0-9]{64})")
        val matcher = pattern.matcher(logs)
        assertTrue(matcher.find(), "Could not find SHA-256 hash in container logs")
        val correctHash = matcher.group(1)

        val wrongHash = if (correctHash.first() == 'a') {
            "b" + correctHash.substring(1)
        } else {
            "a" + correctHash.substring(1)
        }

        val result: String = try {
            downloadAndVerify(url, wrongHash, System.out.writer())
        } catch (e: DownloadException) {
            "Download failed: ${e.message}"
        }

        val expectedResult = "Error: Hash mismatch! Expected: $wrongHash, computed: $correctHash"
        assertEquals(expectedResult, result)
    }

    @Test
    fun `test successful download scenario`() {
        val port = serverContainer.getMappedPort(8080)
        val host = serverContainer.host
        val url = "http://$host:$port/"

        val logs = serverContainer.logs
        val pattern = Pattern.compile("SHA-256 hash of the data:\\s*([a-fA-F0-9]{64})")
        val matcher = pattern.matcher(logs)
        assertTrue(matcher.find(), "Could not find SHA-256 hash in container logs")
        val expectedHash = matcher.group(1)

        val result: String = try {
            downloadAndVerify(url, expectedHash,System.out.writer())
        } catch (e: DownloadException) {
            "Download failed: ${e.message}"
        }

        assertEquals("Success: Downloaded data is correct (hash matches)!", result)
    }
}
