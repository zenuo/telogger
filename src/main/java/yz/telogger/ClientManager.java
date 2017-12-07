package yz.telogger;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelMatcher;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 客户端管理器
 *
 * @author 袁臻
 * 2017/11/21 23:46
 */
public enum ClientManager {

    INSTANCE;

    private final ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private final AtomicInteger count = new AtomicInteger(0);

    void writeLine(final String msg, final ChannelMatcher matcher) {
        group.writeAndFlush(msg.concat(Constant.NEW_LINE), matcher);
    }

    void add(final Channel channel) {
        group.add(channel);
        count.incrementAndGet();
    }

    void remove(final Channel channel) {
        LogWriterManager.INSTANCE.offline(channel);
        group.remove(channel);
        count.decrementAndGet();
    }

    int count() {
        return count.get();
    }
}
