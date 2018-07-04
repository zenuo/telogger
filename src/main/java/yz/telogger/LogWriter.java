package yz.telogger;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * 日志书写器，每一个被订阅的日志文件对应一个本类实例
 *
 * @author zenuo
 * 2017/11/21 23:52
 */
@Slf4j
final class LogWriter {

    private final String filePath;
    /**
     * 通道匹配器
     */
    private final SetChannelMatcher setChannelMatcher = new SetChannelMatcher();
    private volatile Thread thread = null;
    private volatile Process process = null;
    private volatile boolean isWorking = false;

    /**
     * 构造方法
     */
    LogWriter(String filePath) {
        this.filePath = filePath;
    }

    /**
     * 订阅
     *
     * @param channel 订阅此日志文件的通道实例
     */
    void subscribe(Channel channel) {
        setChannelMatcher.add(channel);
        check();
    }

    /**
     * 取消订阅
     *
     * @param channel 取消订阅此日志文件的通道实例
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
            log.info("Boot " + filePath);
            boot();
        } else if (isNeedShutDown()) {
            log.info("Shutdown " + filePath);
            shutdown();
        }
    }

    /**
     * 是否需要启动
     *
     * @return true if and only if this log writer is not working and subscribed, otherwise false
     */
    private boolean isNeedBoot() {
        return !isWorking && !setChannelMatcher.isEmpty();
    }

    /**
     * 是否需要关闭
     *
     * @return true if and only if this log writer is working and not subscribed, otherwise false
     */
    private boolean isNeedShutDown() {
        return isWorking && setChannelMatcher.isEmpty();
    }

    /**
     * Boot this log writer
     * <p>
     * start a thread to execute <code>tail</code> method
     */
    private void boot() {
        //create a thread
        thread = new Thread(this::tail);
        thread.setName("Thread-" + filePath);
        thread.setDaemon(true);
        //start the thread
        thread.start();
    }

    /**
     * Shutdown this log writer
     * <p>
     * stop the <code>process</code> and the <code>thread</code>
     */
    private void shutdown() {
        //stop the process
        if (process != null && process.isAlive()) {
            process.destroyForcibly();
            process = null;
        }
        //stop the thread
        if (thread != null && !thread.isInterrupted()) {
            thread.interrupt();
            thread = null;
        }
    }

    /**
     * Check the specific channel whether subscribed this log writer or not
     *
     * @param channel the specific channel
     * @return true if and only if <code>setChannelMatcher</code> matches the specific channel, false otherwise
     */
    boolean contains(Channel channel) {
        return setChannelMatcher.matches(channel);
    }

    /**
     * 创建一个子进程执行 <code>tail</code> 命令
     */
    private void tail() {
        //change flag isWorking to true
        isWorking = true;
        try {
            //创建子进程
            process = Runtime.getRuntime()
                    .exec(String.format(Constants.TAIL_F_COMMAND_TEMPLATE, filePath));
            log.info("Listen {}", filePath);
            //获取子进程的输出，并将每一行写至订阅本文件的通道
            new BufferedReader(new InputStreamReader(process.getInputStream()))
                    .lines()
                    .forEach(line -> ClientManager.INSTANCE.writeLine(line, setChannelMatcher));
        } catch (Exception e) {
            log.warn("Listen " + filePath, e);
        } finally {
            log.info("Exit listen " + filePath);
            //改变isWorking
            isWorking = false;
            //检查状态
            check();
        }
    }

    String getFilePath() {
        return filePath;
    }
}