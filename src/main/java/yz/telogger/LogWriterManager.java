package yz.telogger;

import io.netty.channel.Channel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
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

    private String filePaths = null;

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
        }
    }

    /**
     * 获取文件列表字符串
     *
     * @return 文件列表字符串
     */
    String filePaths() {
        if (filePaths == null) {
            //懒加载
            final StringBuilder stringBuilder = new StringBuilder("可订阅的文件列表:\r\n");
            map.values().forEach(logWriter -> stringBuilder
                    .append(logWriter.getFilePath())
                    .append(Constant.NEW_LINE));
            filePaths = stringBuilder.append(Constant.NEW_LINE).toString();
        }
        return filePaths;
    }

    /**
     * 订阅
     *
     * @param channel   需要订阅的channel实例
     * @param arguments 参数列表
     * @return 返回给客户端的信息
     */
    String subscribe(final Channel channel, final List<String> arguments) {
        //判断参数列表长度
        if (arguments.size() == 1) {
            //若为1
            final String filePath = arguments.get(0);
            //判断是否已订阅
            final LogWriter subscribed = subscribed(channel);
            if (subscribed != null) {
                //存在订阅
                //判断已订阅的日志是否为此次请求订阅的日志
                if (Objects.equals(subscribed.getFilePath(), filePath)) {
                    //若是
                    return String.format(Constant.SUCCESS_ALREADY_SUBCSRIBED, channel.remoteAddress().toString(), filePath);
                } else {
                    //若不是
                    return Constant.ERROR_MULTI_SUBSCRIBE;
                }
            } else {
                //不存在订阅
                //判断请求订阅的日志文件名称是否存在
                final LogWriter logWriter = map.get(filePath);
                if (logWriter != null) {
                    //若存在
                    logWriter.subscribe(channel);
                    return String.format(Constant.SUCCESS_SUBSCRIBED, channel.remoteAddress().toString(), filePath);
                } else {
                    //若不存在
                    return String.format(Constant.ERROR_FILE_NOT_EXISTS, filePath);
                }
            }
        } else {
            //若不为1
            return String.format(Constant.ERROR_INVALID_ARGUMENTS, arguments.toString());
        }
    }

    /**
     * 取消订阅
     *
     * @param channel 需要取消订阅的channel
     * @return 返回给客户端的信息
     */
    String unsunscribe(final Channel channel) {
        logger.info("取消订阅-" + channel.remoteAddress());
        //获取已订阅的日志
        final LogWriter subscribed = subscribed(channel);
        if (subscribed != null) {
            subscribed.unsubscribe(channel);
            //若不为空，则代表已经订阅
            return "取消订阅-成功-" + channel.remoteAddress();
        } else {
            //若为空，代表不存在订阅
            return Constant.ERROR_NOT_SUBSCRIBED;
        }
    }

    /**
     * 获取某个channel已经订阅的日志
     *
     * @param channel 需要获取已订阅日志的channel
     * @return 若已订阅，则返回已订阅的日志书写器实例；否则返回false
     */
    private LogWriter subscribed(final Channel channel) {
        for (final LogWriter logWriter : map.values()) {
            if (logWriter.contains(channel)) {
                return logWriter;
            }
        }
        return null;
    }
}
