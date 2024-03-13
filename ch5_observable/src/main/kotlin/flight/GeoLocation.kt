package flight

import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.CompletableFuture

data class GeoLocation(
    val address: String
)

object GeoLocationRepository {
    fun findById(id: Long) = GeoLocation(id.toString())

    fun findByIdAsync(id: Long): CompletableFuture<GeoLocation> = CompletableFuture.supplyAsync { findById(id) }

    fun findByIdReactive(id: Long): Observable<GeoLocation> = findByIdAsync(id).toObservable()

    fun findByIdFromFuture(id: Long): Observable<GeoLocation> = Observable.fromFuture(findByIdAsync(id))
}
