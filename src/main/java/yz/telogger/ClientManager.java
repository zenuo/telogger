package yz.telogger;

import io.netty.channel.Channel;
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

    private final ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    void write(final String msg) {
        group.writeAndFlush(msg);
    }

    void add(final Channel channel) {
        group.add(channel);
    }

    void remove(final Channel channel) {
        group.remove(channel);
    }

    int count() {
        return group.size();
    }
}
