package yz.telogger;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelMatcher;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Client manager
 *
 * @author yziyz
 * 2017/11/21 23:46
 */
public enum ClientManager {

    INSTANCE;

    /**
     * Channel group
     */
    private final ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * Counter
     */
    private final AtomicInteger count = new AtomicInteger(0);

    /**
     * Write the specific message to channels which matches the specific matcher
     *
     * @param msg     the specific message
     * @param matcher the specific matcher
     */
    void writeLine(final String msg, final ChannelMatcher matcher) {
        group.writeAndFlush(msg.concat(Constant.NEW_LINE), matcher);
    }

    /**
     * Add the specific channel to channel group
     *
     * @param channel the specific channel
     */
    void add(final Channel channel) {
        group.add(channel);
        count.incrementAndGet();
    }

    /**
     * Remove the specific channel form channel group
     *
     * @param channel the specific channel
     */
    void remove(final Channel channel) {
        //unsubscribes before remove
        LogWriterManager.INSTANCE.unsubscribe(channel);
        //remove
        group.remove(channel);
        //decrement counter
        count.decrementAndGet();
    }

    int count() {
        return count.get();
    }
}
