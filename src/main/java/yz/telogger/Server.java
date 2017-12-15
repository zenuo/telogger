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
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.handler.timeout.IdleStateHandler;

import javax.net.ssl.SSLException;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.util.logging.Logger;

public final class Server {

    /**
     * log
     */
    private static final Logger log = Logger.getLogger(Server.class.getName());

    /**
     * Main method
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) throws CertificateException, SSLException, InterruptedException {
        log.info("Telogger booting");

        //Initialize LogWriterManager
        LogWriterManager.INSTANCE.init();
        //Initialize CommandManager
        CommandManager.INSTANCE.init();

        //SSL encryption
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
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {

                            final ChannelPipeline pipeline = socketChannel.pipeline();

                            if (sslContext != null) {
                                pipeline.addLast(sslContext.newHandler(socketChannel.alloc()));
                            }
                            pipeline.addLast(new IdleStateHandler(0, Constant.WRITER_IDLE_TIME_SECONDS, 0))
                                    .addLast(new StringDecoder(StandardCharsets.UTF_8))
                                    .addLast(new StringEncoder(StandardCharsets.UTF_8))
                                    .addLast(new Handler());
                        }
                    });

            //Bind and start to accept incoming connections.
            log.info("Bind port " + Constant.PORT + ", start");
            final ChannelFuture channelFuture = bootstrap.bind(Constant.PORT).sync();
            log.info("Bind port " + Constant.PORT + ", completed");

            //Block until server is closed.
            channelFuture.channel().closeFuture().sync();
        } finally {
            //Shutdown thread pool
            boosGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            log.info("Telogger shutting down, bye");
        }
    }
}
