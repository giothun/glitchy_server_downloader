package downloader

import java.net.HttpURLConnection
import java.net.URI

fun createConnection(url: String, method: String, timeout: Int): HttpURLConnection {
    val connection = URI.create(url).toURL().openConnection() as HttpURLConnection
    connection.requestMethod = method
    connection.connectTimeout = timeout
    connection.readTimeout = timeout
    connection.doInput = true
    return connection
}
