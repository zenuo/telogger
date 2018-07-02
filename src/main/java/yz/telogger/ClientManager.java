package yz.telogger;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelMatcher;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Client manager
 *
 * @author zenuo
 * 2017/11/21 23:46
 */
@Slf4j
enum ClientManager {

    INSTANCE;

    /**
     * 通道组
     */
    private final ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * 计数器
     */
    private final AtomicInteger count = new AtomicInteger(0);

    /**
     * 将消息写到匹配的通道
     *
     * @param msg     指定的消息
     * @param matcher 目标通道的匹配器实例
     */
    void writeLine(final String msg, final ChannelMatcher matcher) {
        group.write(msg, matcher);
        group.writeAndFlush(Constant.NEW_LINE, matcher);
    }

    /**
     * 添加通道
     *
     * @param channel 指定的通道实例
     */
    void add(final Channel channel) {
        group.add(channel);
        count.incrementAndGet();
        log.info("Online: {}\nOnline client count: {}",
                channel.remoteAddress(),
                count.get());
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
        log.info("Offline: {}\nOnline client count: {}",
                channel.remoteAddress(),
                count.get());
    }
}
