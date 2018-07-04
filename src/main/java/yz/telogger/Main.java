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
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLException;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;

@Slf4j
public final class Main {
    public static void main(String[] args) throws CertificateException, SSLException, InterruptedException {
        log.info("Telogger booting");

        //初始化配置
        Config.INSTANCE.init();
        //初始化LogWriterManager
        LogWriterManager.INSTANCE.init();
        //初始化CommandManager
        CommandManager.INSTANCE.init();

        //SSL加密
        final SslContext sslContext;
        if (Config.INSTANCE.isSsl()) {
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
                        protected void initChannel(SocketChannel socketChannel) {
                            final ChannelPipeline pipeline = socketChannel.pipeline();
                            if (sslContext != null) {
                                pipeline.addLast(sslContext.newHandler(socketChannel.alloc()));
                            }
                            pipeline.addLast(new IdleStateHandler(0, Constants.WRITER_IDLE_TIME_SECONDS, 0))
                                    .addLast(new StringDecoder(StandardCharsets.UTF_8))
                                    .addLast(new StringEncoder(StandardCharsets.UTF_8))
                                    .addLast(new Handler());
                        }
                    });
            //绑定端口，并接受连接
            log.info("Bind port " + Config.INSTANCE.getPort() + ", start");
            final ChannelFuture channelFuture = bootstrap.bind("0.0.0.0", Config.INSTANCE.getPort()).sync();
            log.info("Bind port " + Config.INSTANCE.getPort() + ", completed");
            //阻塞直至socket关闭
            channelFuture.channel().closeFuture().sync();
        } finally {
            //关闭线程池
            boosGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            log.info("Shutdown");
        }
    }
}
