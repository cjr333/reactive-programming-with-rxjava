import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.Unpooled
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

    override fun start() {
        ServerBootstrap()
            .option(ChannelOption.SO_BACKLOG, 50_000)
            .group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel::class.java)
            .childHandler(HttpInitializer())
            .bind(port)
            .sync()
            .channel()
            .closeFuture()
            .sync()
    }

    override fun close() {
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
            .addLast(HttpHandler())
    }
}

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
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        println("Error - ${cause.message}")
        ctx.close()
    }
}
