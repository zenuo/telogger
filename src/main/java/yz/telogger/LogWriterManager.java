package yz.telogger;

import io.netty.channel.Channel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * 日志书写者管理
 *
 * @author 袁臻
 * 2017/12/06 16:32
 */
enum LogWriterManager {

    /**
     * 单例
     */
    INSTANCE;

    private final Logger logger = Logger.getLogger(LogWriterManager.class.getName());

    private final ConcurrentHashMap<String, LogWriter> map = new ConcurrentHashMap<>();

    /**
     * 启动
     */
    void boot() {
        final Path path = Paths.get(Constant.LOG_FILE_CONF_PATH);
        //若文件存在
        if (Files.exists(path)) {
            try {
                //读取配置文件的所有行，加入列表中
                map.putAll(Files.lines(path)
                        .map(String::trim)
                        .filter(line -> !line.startsWith("#") && line.length() != 0)
                        .map(LogWriter::new)
                        .collect(Collectors.toConcurrentMap(LogWriter::getFilePath, logWriter -> logWriter)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //判断映射是否为空
        if (map.isEmpty()) {
            throw new IllegalStateException("No file to listen, exit now.");
        } else {
            //打印监听文件列表
            final List<String> filePathList = map.values()
                    .stream()
                    .map(LogWriter::getFilePath)
                    .collect(Collectors.toList());
            logger.info("监听文件列表-" + filePathList.toString());
            //创建轮寻文件列表线程
            final Thread thread = new Thread(this::manage);
            //设置为守护线程
            thread.setDaemon(true);
            //设置名称
            thread.setName("Thread-logWriterManager");
            //启动线程
            thread.start();
            logger.info("轮寻文件列表线程已创建");
        }
    }

    String filePathList() {
        final StringBuilder stringBuilder = new StringBuilder("可订阅的文件列表:\r\n");
        map.values().forEach(logWriter -> stringBuilder
                .append(logWriter.getFilePath())
                .append(Constant.NEW_LINE));
        return stringBuilder.append(Constant.NEW_LINE).toString();
    }

    private void manage() {
        //无尽循环
        while (true) {
            logger.info("循环检测");
            //开启需要开启的
            map.values().stream()
                    .filter(LogWriter::isNeedBoot)
                    .forEach(LogWriter::boot);
            //关闭需要关闭的
            map.values().stream()
                    .filter(LogWriter::isNeedShutDown)
                    .forEach(LogWriter::shutdown);
            //等待5秒，执行下一次检查
            try {
                Thread.sleep(5000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    void offline(final Channel channel) {
        map.values().forEach(logWriter -> logWriter.unsubscribe(channel));
    }

    String subscribe(final Channel channel, final String filePath) {
        final LogWriter logWriter = map.get(filePath);
        if (logWriter != null) {
            final boolean subscribe = logWriter.subscribe(channel);
            if (subscribe) {
                return "订阅-成功-" + channel.remoteAddress() + "-" + filePath;
            } else {
                return "订阅-已订阅-" + channel.remoteAddress() + "-" + filePath;
            }
        } else {
            return String.format(Constant.ERROR_FILE_NOT_EXISTS, filePath);
        }
    }

    String unsunscribe(final Channel channel) {
        for (final LogWriter logWriter : map.values()) {
            if (logWriter.contains(channel)) {
                final boolean unsubscribe = logWriter.unsubscribe(channel);
                if (unsubscribe) {
                    return "取消订阅-成功-" + channel.remoteAddress();
                } else {
                    return "取消订阅-未订阅-" + channel.remoteAddress();
                }
            }
        }
        return Constant.ERROR_NOT_SUBSCRIBED;
    }
}
