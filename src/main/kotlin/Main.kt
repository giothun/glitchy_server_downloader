import cli.parseArguments
import downloader.downloadAndVerify
import downloader.DownloadException
import downloader.writeln

fun main(args: Array<String>) {
    val out = System.out.writer()
    val (url, expectedHash) = parseArguments(args, out) ?: return

    try {
        out.writeln(downloadAndVerify(url, expectedHash, out))
    } catch (e: DownloadException) {
        out.writeln("Download error: ${e.message}")
    }
}
