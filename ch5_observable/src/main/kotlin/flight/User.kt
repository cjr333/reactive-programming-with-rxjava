package flight

import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.CompletableFuture

data class User(
    val id: Long
)

object UserRepository {
    fun findById(id: Long) = User(id)

    fun findByIdAsync(id: Long): CompletableFuture<User> = CompletableFuture.supplyAsync { findById(id) }

    fun findByIdReactive(id: Long): Observable<User> = findByIdAsync(id).toObservable()

    fun findByIdFromFuture(id: Long): Observable<User> = Observable.fromFuture(findByIdAsync(id))
}
