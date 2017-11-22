package yz.telogger;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.logging.Logger;

@ChannelHandler.Sharable
public final class Handler extends ChannelInboundHandlerAdapter {

    private Logger logger = Logger.getLogger(Handler.class.getName());

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        logger.info("上线-" + ctx.channel().remoteAddress() + "\n目前客户端数量-" + ClientManager.INSTANCE.count());
        ClientManager.INSTANCE.add(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        logger.info("离线-" + ctx.channel().remoteAddress() + "\n目前客户端数量-" + ClientManager.INSTANCE.count());
        ClientManager.INSTANCE.remove(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //super.channelRead(ctx, msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        //super.channelReadComplete(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.warning("客户端-" + ctx.channel().remoteAddress() + "发生异常：");
        cause.printStackTrace();
        ctx.close();
    }
}
