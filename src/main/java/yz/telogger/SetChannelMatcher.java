package yz.telogger;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelMatcher;
import io.netty.util.internal.ConcurrentSet;

/**
 * 一个由Set实现的通道匹配器
 *
 * @author zenuo
 * 2017/12/09 21:37
 */
final class SetChannelMatcher implements ChannelMatcher {

    /**
     * 保存channel实例的Set实例
     */
    private final ConcurrentSet<Channel> set = new ConcurrentSet<>();

    /**
     * 添加需要匹配的通道实例
     *
     * @param channel 通道实例
     */
    void add(final Channel channel) {
        set.add(channel);
    }

    /**
     * 移除需要匹配的通道实例
     *
     * @param channel 通道实例
     */
    void remove(final Channel channel) {
        set.remove(channel);
    }

    /**
     * 是否没有包含通道
     *
     * @return 若是，返回true，否则返回false
     */
    boolean isEmpty() {
        return set.isEmpty();
    }

    @Override
    public boolean matches(Channel channel) {
        return set.contains(channel);
    }
}

