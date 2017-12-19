package yz.telogger;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelMatcher;
import io.netty.util.internal.ConcurrentSet;

/**
 * Matcher of set
 *
 * @author yziyz
 * 2017/12/09 21:37
 */
public final class SetChannelMatcher implements ChannelMatcher {

    private final ConcurrentSet<Channel> set = new ConcurrentSet<>();

    void add(final Channel channel) {
        set.add(channel);
    }

    void remove(final Channel channel) {
        set.remove(channel);
    }

    boolean isEmpty() {
        return set.isEmpty();
    }

    @Override
    public boolean matches(Channel channel) {
        return set.contains(channel);
    }
}

