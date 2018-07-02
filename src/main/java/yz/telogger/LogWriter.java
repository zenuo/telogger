package yz.telogger;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 日志书写器，每一个被订阅的日志文件对应一个本类实例
 *
 * @author zenuo
 * 2017/11/21 23:52
 */
@Slf4j
final class LogWriter {

    private final String filePath;
    private final Path path;
    /**
     * Channel matcher
     */
    private final SetChannelMatcher setChannelMatcher = new SetChannelMatcher();
    private volatile Thread thread = null;
    private volatile Process process = null;
    private volatile boolean isWorking = false;

    /**
     * Constructor
     */
    LogWriter(String filePath) {
        this.path = Paths.get(filePath);
        this.filePath = filePath;
    }

    /**
     * Handle the subscribtion to this log writer by the specific channel
     *
     * @param channel the specific channel
     */
    void subscribe(Channel channel) {
        setChannelMatcher.add(channel);
        check();
    }

    /**
     * Handle the ubsubscribtion to this log writer by the specific channel
     *
     * @param channel the specific channel
     */
    void unsubscribe(Channel channel) {
        setChannelMatcher.remove(channel);
        check();
    }

    /**
     * Check the state of this log writer
     */
    private void check() {
        if (isNeedBoot()) {
            log.info("Boot-" + filePath);
            boot();
        } else if (isNeedShutDown()) {
            log.info("Shutdown-" + filePath);
            shutdown();
        }
    }

    /**
     * Need to boot
     *
     * @return true if and only if this log writer is not working and subscribed, otherwise false
     */
    private boolean isNeedBoot() {
        return !isWorking && !setChannelMatcher.isEmpty();
    }

    /**
     * Need to shutdown
     *
     * @return true if and only if this log writer is working and not subscribed, otherwise false
     */
    private boolean isNeedShutDown() {
        return isWorking && setChannelMatcher.isEmpty();
    }

    /**
     * Boot this log writer
     * <p>
     * start a thread to execute <code>work</code> method
     */
    private void boot() {
        //create a thread
        thread = new Thread(this::work);
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
     * Work
     * <p>
     * Start a subprocess to execute <code>tail</code> command provided by the OS.
     */
    private void work() {
        //change flag isWorking to true
        isWorking = true;
        //waiting until log file exists
        while (!Files.exists(path)) {
            //if flag isWorking changed to false, return
            if (!isWorking) {
                return;
            }
            try {
                Thread.sleep(2000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            //create a subprocess
            process = Runtime.getRuntime()
                    .exec(String.format(Constant.TAIL_F_COMMAND, filePath));
            log.info("Listen {}", filePath);
            //get the output of the subprocess, and writes each line to channels subscribed this log writer
            new BufferedReader(new InputStreamReader(process.getInputStream()))
                    .lines()
                    .forEach(line -> ClientManager.INSTANCE.writeLine(line, setChannelMatcher));
        } catch (Exception e) {
            log.warn("Listen {}", filePath, e);
        } finally {
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