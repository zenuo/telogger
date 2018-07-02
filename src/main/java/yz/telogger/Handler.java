package yz.telogger;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * 通道入站处理器
 *
 * @author zenuo
 * 2017/11/21 00:56
 */
@Slf4j
final class Handler extends SimpleChannelInboundHandler<String> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        //添加客户端
        ClientManager.INSTANCE.add(ctx.channel());
        //send welcome and help message to client
        ctx.channel().writeAndFlush(Constant.MESSAGE_HELLO + Constant.NEW_LINE + CommandManager.INSTANCE.help(false));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        //移除客户端
        ClientManager.INSTANCE.remove(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.warn("Error-" + ctx.channel().remoteAddress() + " exception occurred");
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
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
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
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
