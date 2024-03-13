package flight

import io.reactivex.rxjava3.core.Observable
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture
import java.util.function.Function

class TravalAgencyTest {
    private val agencies = listOf(
        TravelAgency(2000),
        TravelAgency(500),
        TravelAgency(100),
        TravelAgency(1000)
    )

    @Test
    fun sync() {
        val user = UserRepository.findById(100)
        val geoLocation = GeoLocationRepository.findById(100)
        val flight = agencies.map { it.search(user, geoLocation) }.first()
        println(flight)
    }

    @Test
    fun `async - completableFuture`() {
        val user: CompletableFuture<User> = UserRepository.findByIdAsync(100)
        val geoLocation: CompletableFuture<GeoLocation> = GeoLocationRepository.findByIdAsync(100)
        val flight: Flight = user.thenCombine(geoLocation) { us, loc ->
            agencies.map { it.searchAsync(us, loc) }
                .let { CompletableFuture.anyOf(*it.toTypedArray()) }
                .thenApply { it as Flight }
                .get()
        }.get()
        println(flight)
    }

    @Test
    fun `async - completableFuture2`() {
        val user: CompletableFuture<User> = UserRepository.findByIdAsync(100)
        val geoLocation: CompletableFuture<GeoLocation> = GeoLocationRepository.findByIdAsync(100)
        val flight: CompletableFuture<Flight> = user.thenCombine(geoLocation) { us, loc ->
            agencies.map { it.searchAsync(us, loc) }
                .reduce { f1, f2 -> f1.applyToEither(f2, Function.identity()) }
                .get()
        }
        Thread.sleep(2000)
        flight.get().also {
            println("[${LocalDateTime.now()}] $it")
        }
    }

    @Test
    fun `reactive - nice way using completableFuture`() {
        val user: Observable<User> = UserRepository.findByIdReactive(100)
        val geoLocation: Observable<GeoLocation> = GeoLocationRepository.findByIdReactive(100)
        val flight: Observable<Flight> = user.zipWith(geoLocation) { us, loc -> us to loc }
            .flatMap { (us, loc) ->
                Observable.fromIterable(agencies).flatMap { agency ->
                    agency.searchReactive(us, loc)
                }
            }
        Thread.sleep(2000)
        flight.blockingFirst().also {
            println("[${LocalDateTime.now()}] $it")
        }
    }

    @Test
    fun fromFuture() {
        val user: Observable<User> = UserRepository.findByIdFromFuture(100)
        val geoLocation: Observable<GeoLocation> = GeoLocationRepository.findByIdFromFuture(100)
        val flight: Flight = user.zipWith(geoLocation) { us, loc -> us to loc }
            .flatMap { (us, loc) ->
                Observable.fromIterable(agencies).flatMap { agency ->
                    agency.searchFromFuture(us, loc)
                }
            }.blockingFirst()
        println(flight)
    }
}
