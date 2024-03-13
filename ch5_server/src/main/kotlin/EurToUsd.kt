import rx.Observable
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

private val RATE = BigDecimal("1.06488")

fun eurToUsd(eur: BigDecimal): Observable<String> =
    Observable.just(eur.multiply(RATE))
        .map { "$eur EUR is $it USD\n" }
        .delay(1, TimeUnit.SECONDS)
