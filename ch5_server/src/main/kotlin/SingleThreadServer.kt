import java.util.concurrent.Executors

class SingleThreadServer(
    port: Int
): BlockingServer(port, Executors.newSingleThreadExecutor())
