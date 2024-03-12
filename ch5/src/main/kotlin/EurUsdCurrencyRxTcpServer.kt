import io.netty.handler.codec.LineBasedFrameDecoder
import io.netty.handler.codec.string.StringDecoder
import io.reactivex.netty.protocol.tcp.server.TcpServer
import rx.Observable
import java.math.BigDecimal
import java.nio.charset.StandardCharsets.UTF_8

class EurUsdCurrencyRxTcpServer(
    port: Int
): Server {
    private val server = TcpServer
        .newServer(port)
        .pipelineConfigurator<String, String> { pipeline ->
            pipeline.addLast(LineBasedFrameDecoder(1024))
            pipeline.addLast(StringDecoder(UTF_8))
        }

    override fun start() {
        server.start { connection ->
            val output: Observable<String> = connection.input
                .map { BigDecimal(it) }
                .flatMap { eurToUsd(it) }
            connection.writeAndFlushOnEach(output)
        }
    }

    override fun close() {
        server.awaitShutdown()
    }
}
