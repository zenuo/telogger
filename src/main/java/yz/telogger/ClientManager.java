package yz.telogger;

import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * 客户端管理器
 *
 * @author 袁臻
 * 2017/11/21 23:46
 */
public enum ClientManager {

    INSTANCE;

    public final ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public void write(final String msg) {
        if (!group.isEmpty()) {
            group.writeAndFlush(msg);
        }
    }

}
