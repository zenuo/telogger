package yz.telogger;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public final class Server {

    public static void main(String[] args) throws Exception {
        if (Constant.LOG_FILE == null) {
            System.out.println("Usage: java -DlogFile=/var/log/syslog -jar telogger");
            System.exit(1);
        }

        final SslContext sslContext;
        if (Constant.SSL) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslContext = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } else {
            sslContext = null;
        }

        final NioEventLoopGroup boosGroup = new NioEventLoopGroup(1);
        final NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            final ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boosGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            final ChannelPipeline pipeline = socketChannel.pipeline();

                            if (sslContext != null) {
                                pipeline.addLast(sslContext.newHandler(socketChannel.alloc()));
                            }

                            pipeline.addLast(new StringDecoder(StandardCharsets.UTF_8))
                                    .addLast(new StringEncoder(StandardCharsets.UTF_8))
                                    .addLast(new Handler());
                        }
                    });

            //绑定端口，并阻塞至绑定完成
            final ChannelFuture channelFuture = bootstrap.bind(Constant.PORT).sync();

            final LogWriter logWriter = new LogWriter();

            CompletableFuture.supplyAsync(logWriter::work);

            //阻塞直至服务器套节字被关闭
            channelFuture.channel().closeFuture().sync();
        } finally {
            boosGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
