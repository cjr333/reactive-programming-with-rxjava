import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class ConnectionPoolServer(
    port: Int
): BlockingServer(
    port,
    ThreadPoolExecutor(
        100,
        100,
        0L,
        TimeUnit.MILLISECONDS,
        ArrayBlockingQueue(1000),
    ) { r: Runnable, _: ThreadPoolExecutor ->
        (r as ClientConnection).serviceUnavailable()
    }
)
