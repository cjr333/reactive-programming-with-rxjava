import java.util.concurrent.Executors

class ThreadPerConnectionServer(
    port: Int
): BlockingServer(port, Executors.newCachedThreadPool())
