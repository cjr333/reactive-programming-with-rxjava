package flight

import io.reactivex.rxjava3.core.Observable
import java.time.LocalDateTime
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

data class Flight(
    val id: String
)

class TravelAgency(
    private val delay: Long
) {
    private val executorService = Executors.newSingleThreadExecutor()

    fun search(user: User, location: GeoLocation): Flight {
        return executorService.submit(Callable {
            Thread.sleep(delay)
            Flight("From travel agency with delay = $delay")
        }).get()
    }

    fun searchAsync(user: User, location: GeoLocation): CompletableFuture<Flight> {
        println("[${LocalDateTime.now()}] background search started")
        return CompletableFuture.supplyAsync { search(user, location) }
    }

    fun searchReactive(user: User, location: GeoLocation): Observable<Flight> =
        searchAsync(user, location).toObservable()

    fun searchFromFuture(user: User, location: GeoLocation): Observable<Flight> =
        Observable.fromFuture(searchAsync(user, location))
}
