package yz.telogger;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * The channel inbound handler
 *
 * @author yziyz
 * 2017/11/21 00:56
 */
final class Handler extends SimpleChannelInboundHandler<String> {

    /**
     * Log
     */
    private final Logger log = Logger.getLogger(Handler.class.getName());

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //add client
        ClientManager.INSTANCE.add(ctx.channel());
        //send welcome and help message to client
        ctx.channel().writeAndFlush(Constant.MESSAGE_HELLO + Constant.NEW_LINE + CommandManager.INSTANCE.help(false));
        log.info("Online-" + ctx.channel().remoteAddress() + "\nOnline client count: " + ClientManager.INSTANCE.count());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //remove client
        ClientManager.INSTANCE.remove(ctx.channel());
        log.info("Offline-" + ctx.channel().remoteAddress() + "\nOnline client count: " + ClientManager.INSTANCE.count());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.warning("Error-" + ctx.channel().remoteAddress() + " exception occurred");
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        final String trimMsg = msg.trim();
        if (trimMsg.length() != 0) {
            if (Objects.equals(trimMsg.toLowerCase(), "quit")) {
                //打印再见信息
                ctx.channel().writeAndFlush(Constant.MESSAGE_BYE);
                //关闭通道
                ctx.channel().close();
            } else {
                final String output = CommandManager.INSTANCE.exec(ctx.channel(), trimMsg);
                ctx.writeAndFlush(output);
            }
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        //Idle state event triggered
        if (evt instanceof IdleStateEvent) {
            final IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if (idleStateEvent.state() == IdleState.WRITER_IDLE) {
                ctx.channel().writeAndFlush(Constant.MESSAGE_BYE);
                //close channel
                ctx.channel().close();
            }
        }
    }
}
