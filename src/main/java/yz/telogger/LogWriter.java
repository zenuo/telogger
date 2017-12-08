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

    private Thread thread = null;

    private Process process = null;

    private boolean isWorking = false;

    private final String filePath;

    private final Path path;

    private final Logger logger = Logger.getLogger(LogWriter.class.getName());

    /**
     * 匹配器
     */
    private final SetChannelMatcher setChannelMatcher = new SetChannelMatcher();

    /**
     * 构造方法
     */
    LogWriter(String filePath) {
        this.path = Paths.get(filePath);
        this.filePath = filePath;
    }

    /**
     * 将指定的channel加入匹配器
     *
     * @param channel 指定的channel实例
     */
    void subscribe(Channel channel) {
        setChannelMatcher.add(channel);
        check();
    }

    /**
     * 将指定的channel从匹配器中移除
     *
     * @param channel 指定的channel实例
     */
    void unsubscribe(Channel channel) {
        setChannelMatcher.remove(channel);
        check();
    }

    /**
     * 检查状态
     */
    private void check() {
        if (isNeedBoot()) {
            logger.info("启动" + filePath);
            boot();
        }
        if (isNeedShutDown()) {
            logger.info("关闭" + filePath);
            shutdown();
        }
    }

    /**
     * 是否需要启动
     *
     * @return 当且仅当文件不为空且文件存在且未在工作中且存在订阅客户端时返回true，否则返回false
     */
    private boolean isNeedBoot() {
        return path != null && Files.exists(path) && !isWorking && !setChannelMatcher.isEmpty();
    }

    /**
     * 是否需要关闭
     *
     * @return 当且仅当在工作中且不存在订阅客户端时返回true，否则返回false
     */
    private boolean isNeedShutDown() {
        return isWorking && setChannelMatcher.isEmpty();
    }

    private void boot() {
        thread = new Thread(this::work);
        thread.setName("Thread-" + filePath);
        thread.setDaemon(true);
        thread.start();
    }

    private void shutdown() {
        isWorking = false;
        if (process != null && process.isAlive()) {
            process.destroyForcibly();
            process = null;
        }
        if (thread != null && !thread.isInterrupted()) {
            thread.interrupt();
            thread = null;
        }
    }

    boolean contains(Object o) {
        return setChannelMatcher.contains(o);
    }

    /**
     * 监听文件直至异常发生
     */
    private void work() {
        if (Files.exists(path)) {
            //设置isWorking为true
            isWorking = true;
            //进程引用

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

    void add(final Channel channel) {
        set.add(channel);
    }

    void remove(final Channel channel) {
        set.remove(channel);
    }

    boolean isEmpty() {
        return set.isEmpty();
    }

    boolean contains(Object o) {
        return set.contains(o);
    }

    @Override
    public boolean matches(Channel channel) {
        return set.contains(channel);
    }
}
