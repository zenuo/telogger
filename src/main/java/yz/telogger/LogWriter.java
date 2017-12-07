package yz.telogger;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelMatcher;
import io.netty.util.internal.ConcurrentSet;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

/**
 * 日志书写器
 *
 * @author 袁臻
 * 2017/11/21 23:52
 */
final class LogWriter {

    private Thread thread;

    private final String filePath;

    private boolean isWorking = false;

    private final Path path;

    private final Logger logger = Logger.getLogger(LogWriter.class.getName());

    /**
     * Channel匹配器
     */
    private final SetChannelMatcher setChannelMatcher = new SetChannelMatcher();

    /**
     * 将指定的channel加入匹配器
     *
     * @param channel 指定的channel实例
     */
    boolean subscribe(Channel channel) {
        return setChannelMatcher.add(channel);
    }

    /**
     * 将指定的channel从匹配器中移除
     *
     * @param channel 指定的channel实例
     */
    boolean unsubscribe(Channel channel) {
        return setChannelMatcher.remove(channel);
    }

    /**
     * 构造方法
     */
    LogWriter(String filePath) {
        this.filePath = filePath;
        this.path = Paths.get(filePath);
    }

    /**
     * 是否需要启动
     *
     * @return 若需要被启动，返回true，否则返回false
     */
    boolean isNeedBoot() {
        //当且仅当文件不为空且文件存在且未在工作中且存在订阅客户端时返回true
        return path != null && Files.exists(path) && !isWorking && !setChannelMatcher.isEmpty();
    }

    boolean isNeedShutDown() {
        return isWorking && setChannelMatcher.isEmpty();
    }

    void boot() {
        if (thread == null) {
            thread = new Thread(this::work);
            thread.setName("Thread-" + filePath);
            //thread.setDaemon(true);
            thread.start();
        } else {
            thread.start();
        }
    }

    void shutdown() {
        if (!thread.isInterrupted()) {
            isWorking = false;
            thread.interrupt();
        }
    }

    /**
     * 监听文件直至异常发生
     */
    private void work() {
        if (Files.exists(path)) {
            //设置isWorking为true
            isWorking = true;
            //进程引用
            Process process = null;
            try {
                //创建进程
                process = Runtime.getRuntime()
                        .exec(String.format(Constant.TAIL_F_COMMAND, filePath));
                logger.info("监听文件-" + filePath);
                //监听进程的输出，阻塞至异常抛出或者被关闭输出
                new BufferedReader(new InputStreamReader(process.getInputStream()))
                        .lines()
                        .forEach(line -> ClientManager.INSTANCE.writeLine(line, setChannelMatcher));
            } catch (Exception e) {
                logger.warning("监听文件-" + filePath + "-发生异常");
                e.printStackTrace();
            } finally {
                //设置isWorking为false
                isWorking = false;
                //停止进程
                if (process != null && process.isAlive()) {
                    process.destroy();
                }
            }
        }
    }

    String getFilePath() {
        return filePath;
    }
}

final class SetChannelMatcher implements ChannelMatcher {

    private final ConcurrentSet<Channel> set = new ConcurrentSet<>();

    boolean add(final Channel channel) {
        return set.add(channel);
    }

    boolean remove(final Channel channel) {
        return set.remove(channel);
    }

    boolean isEmpty() {
        return set.isEmpty();
    }

    @Override
    public boolean matches(Channel channel) {
        return set.contains(channel);
    }
}
