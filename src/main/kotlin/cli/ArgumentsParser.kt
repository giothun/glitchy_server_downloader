package cli

import java.io.Writer

fun Writer.writeln(message: String) {
    write(message + "\n")
    flush()
}

fun printHelp(writer: Writer) {
    writer.writeln("Glitchy Server Data Downloader")
    writer.writeln("Usage: ./gradlew run --args=\"<url> <expected-hash>\"")
    writer.writeln("  <url> must be a valid http(s) URL, e.g., http://127.0.0.1:8080/")
    writer.writeln("  <expected-hash> must be exactly 64 hexadecimal characters (SHA‑256 hash).")
}

fun isValidUrl(url: String): Boolean {
    val regex = Regex("^https?://.+")
    return regex.matches(url)
}

fun isValidHash(hash: String): Boolean {
    val regex = Regex("^[a-fA-F0-9]{64}$")
    return regex.matches(hash)
}

fun parseArguments(args: Array<String>, writer: Writer): Pair<String, String>? {
    if (args.size < 2) {
        printHelp(writer)
        return null
    }
    val url = args[0]
    val expectedHash = args[1].lowercase()

    if (!isValidUrl(url)) {
        writer.writeln("Error: The provided URL '$url' is not valid.")
        printHelp(writer)
        return null
    }

    if (!isValidHash(expectedHash)) {
        writer.writeln("Error: The provided hash '$expectedHash' is not valid. It must be exactly 64 hexadecimal characters.")
        printHelp(writer)
        return null
    }

    return Pair(url, expectedHash)
}
