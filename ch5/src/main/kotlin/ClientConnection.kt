import java.io.BufferedReader
import java.io.Closeable
import java.io.InputStreamReader
import java.net.Socket

class ClientConnection(
    private val client: Socket
): Runnable {
    companion object {
        private val RESPONSE: ByteArray = """
            HTTP/1.1 200 OK
            Content-length: 2
            
            OK
        """.trimIndent().toByteArray()

        private val SERVICE_UNAVAILABLE: ByteArray = """
            HTTP/1.1 503 Service unavailable
        """.trimIndent().toByteArray()
    }

    override fun run() {
        runCatching {
            while (!Thread.currentThread().isInterrupted) {
                readFullRequest(client)
                client.getOutputStream().write(RESPONSE)
            }
        }.onFailure {
            it.printStackTrace()
            closeQuietly(client)
        }
    }

    fun serviceUnavailable() {
        client.getOutputStream().write(SERVICE_UNAVAILABLE)
    }

    private fun readFullRequest(client: Socket) {
        val reader = BufferedReader(InputStreamReader(client.getInputStream()))
        while (!reader.readLine().isNullOrEmpty()) { }
    }

    private fun closeQuietly(closeable: Closeable) {
        runCatching {
            closeable.close()
        }
    }
}
