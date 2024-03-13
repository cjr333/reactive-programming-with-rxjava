import io.reactivex.netty.protocol.http.server.HttpServer
import java.math.BigDecimal

class EurUsdCurrencyRxRestServer(
    port: Int
): Server {
    private val server = HttpServer.newServer(port)

    override fun start() {
        server.start { req, resp ->
            req.decodedPath.substring(1)
                .let { BigDecimal(it) }
                .let { eurToUsd(it) }
                .let {
                    resp.writeString(it)
                }
        }
    }

    override fun close() {
        server.awaitShutdown()
    }
}
