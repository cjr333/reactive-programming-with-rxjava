package flight

import io.reactivex.rxjava3.core.Observable
import org.junit.jupiter.api.Test
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
            agencies.stream()
                .map { it.searchAsync(us, loc) }
                .reduce { f1, f2 -> f1.applyToEither(f2, Function.identity()) }
                .get()
        }
            .thenCompose(Function.identity())
            .get()
        println(flight)
    }

    @Test
    fun `reactive - nice way using completableFuture`() {
        val user: Observable<User> = UserRepository.findByIdReactive(100)
        val geoLocation: Observable<GeoLocation> = GeoLocationRepository.findByIdReactive(100)
        val flight: Flight = user.zipWith(geoLocation) { us, loc -> us to loc }
            .flatMap { (us, loc) ->
                Observable.fromIterable(agencies).flatMap { agency ->
                    agency.searchReactive(us, loc)
                }
            }.blockingFirst()
        println(flight)
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
