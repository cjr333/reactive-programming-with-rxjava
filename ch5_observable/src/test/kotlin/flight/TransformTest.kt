package flight

import io.reactivex.rxjava3.core.Observable
import org.junit.jupiter.api.Test
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class TransformTest {
    @Test
    fun `CompletableFuture to Observable`() {
        val cf = CompletableFuture<Int>()
        Executors.newCachedThreadPool().submit {
            Thread.sleep(1000)
            cf.complete(100)
        }
        cf.toObservable().blockingForEach {
            println("Completed - $it")
        }
    }

    @Test
    fun `Observable to CompletableFuture`() {
        Observable.just(100).delay(1, TimeUnit.SECONDS)
            .toCompletableFuture()
            .thenAccept { println("Completed - $it") }
            .get()

        Observable.interval(100, TimeUnit.MILLISECONDS)
            .take(10)
            .toCompletableFutureList()
            .thenAccept { println("Completed - $it") }
            .get()
    }
}
