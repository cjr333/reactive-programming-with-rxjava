package flight

import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.CompletableFuture

fun <T: Any> CompletableFuture<T>.toObservable(): Observable<T> {
    return Observable.create { subscriber ->
        this.whenComplete { value, throwable ->
            if (throwable != null) {
                subscriber.onError(throwable)
            } else {
                subscriber.onNext(value)
                subscriber.onComplete()
            }
        }
    }
}

fun <T: Any> Observable<T>.toCompletableFuture(): CompletableFuture<T> {
    return CompletableFuture<T>().also {
        this.singleOrError()
            .subscribe(it::complete, it::completeExceptionally)
    }
}

fun <T: Any> Observable<T>.toCompletableFutureList(): CompletableFuture<List<T>> =
    toList().toObservable().toCompletableFuture()

