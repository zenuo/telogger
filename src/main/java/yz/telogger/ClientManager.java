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

    private int count = 0;

    void write(final String msg) {
        group.writeAndFlush(msg);
    }

    void add(final Channel channel) {
        group.add(channel);
        ++count;
    }

    void remove(final Channel channel) {
        group.remove(channel);
        --count;
    }

    int count() {
        return count;
    }
}
