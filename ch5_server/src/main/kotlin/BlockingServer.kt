import java.net.ServerSocket
import java.util.concurrent.ExecutorService

open class BlockingServer(
    port: Int,
    private val executorService: ExecutorService
): Server {
    private val serverSocket = ServerSocket(port, 100)

    override fun start() {
        executorService.submit {
            while (!Thread.currentThread().isInterrupted) {
                ClientConnection(serverSocket.accept()).run()
            }
        }
    }

    override fun close() {
        serverSocket.close()
        executorService.shutdown()
    }
}
