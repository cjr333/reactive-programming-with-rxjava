import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.codec.http.HttpVersion

class HttpTcpNettyServer(
    private val port: Int
): Server {
    private val bossGroup: EventLoopGroup = NioEventLoopGroup(1)
    private val workerGroup: EventLoopGroup = NioEventLoopGroup()
    private var channel: Channel? = null

    override fun start() {
        channel = ServerBootstrap()
            .option(ChannelOption.SO_BACKLOG, 50_000)
            .group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel::class.java)
            .childHandler(HttpInitializer())
            .bind(port)
            .sync()
            .channel()
    }

    override fun close() {
        channel?.closeFuture()?.sync()
        bossGroup.shutdownGracefully()
        workerGroup.shutdownGracefully()
    }
}

class HttpInitializer: ChannelInitializer<SocketChannel>() {
    private val httpHandler = HttpHandler()

    override fun initChannel(ch: SocketChannel) {
        ch
            .pipeline()
            .addLast(HttpServerCodec())
            .addLast(httpHandler)
    }
}

@Sharable
class HttpHandler: ChannelInboundHandlerAdapter() {
    override fun channelReadComplete(ctx: ChannelHandlerContext) {
        ctx.flush()
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg is HttpRequest) {
            sendResponse(ctx)
        }
    }

    private fun sendResponse(ctx: ChannelHandlerContext) {
        DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1,
            HttpResponseStatus.OK,
            Unpooled.wrappedBuffer("OK".toByteArray())
        ).apply {
            headers().add("Content-length", 2)
        }.also {
            ctx.writeAndFlush(it)
                // .addListener(ChannelFutureListener.CLOSE)
            // Sharable -> 연결을 유지하며 정상 동작
            // Sharable + Close listener -> 매번 새로 연결을 맺으면서 정상 동작 (후 연결은 바로 끊김)
            // No Sharable -> 기존 연결을 유지하며 요청마다 새 연결을 맺으려다 실패. 기존 연결로 응답해서 정상 동작
            // No Sharable + Close listener -> 기존 연결이 끊김 요청마다 새 연결을 맺으려다 실패. 기존 연결도 끊겨 있어서 응답 불가
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        println("Error - ${cause.message}")
        ctx.close()
    }
}
