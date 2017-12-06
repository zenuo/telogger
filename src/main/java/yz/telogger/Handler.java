package yz.telogger;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.util.logging.Logger;

final class Handler extends SimpleChannelInboundHandler<String> {

    private Logger logger = Logger.getLogger(Handler.class.getName());

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ClientManager.INSTANCE.add(ctx.channel());
        ctx.channel().writeAndFlush(Constant.HELLO);
        logger.info("上线-" + ctx.channel().remoteAddress() + "\n当前客户端数量" + ClientManager.INSTANCE.count());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ClientManager.INSTANCE.remove(ctx.channel());
        logger.info("离线-" + ctx.channel().remoteAddress() + "\n当前客户端数量" + ClientManager.INSTANCE.count());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.warning("客户端-" + ctx.channel().remoteAddress() + "发生异常：");
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        final String trimMsg = msg.trim();
        if (trimMsg.length() != 0) {
            //若客户端发送的字符串去除两端的空白后非空，则执行该字符串
            logger.info("客户端-" + ctx.channel().remoteAddress() + "执行命令-" + trimMsg);
            final String output = CommandManager.INSTANCE.exec(trimMsg);
            ctx.writeAndFlush(output);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        //若发生通道空闲事件
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if (idleStateEvent.state() == IdleState.WRITER_IDLE) {
                ctx.channel().writeAndFlush(Constant.BYE);
                //关闭通道
                ctx.channel().close();
            }
        }
    }
}
