package yz.telogger;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Log writer manager
 *
 * @author zenuo
 * 2017/12/06 16:32
 */
@Slf4j
enum LogWriterManager {

    /**
     * Instance
     */
    INSTANCE;

    /**
     * The mapping from file path to its instance
     */
    private final ConcurrentHashMap<String, LogWriter> filePathToInstanceMap = new ConcurrentHashMap<>();

    /**
     * Message of file path
     */
    private volatile String filePaths = null;

    /**
     * Initialize
     */
    void init() {
        //path of log files configuration file
        final Path path = Paths.get(Constants.CONF_PATH_LOG_FILE);
        //if file exists
        if (Files.exists(path)) {
            try {
                //read lines and construct log writer instances
                final ConcurrentMap<String, LogWriter> collect = Files.lines(path)
                        .map(String::trim)
                        .filter(this::isValidLogFile)
                        .map(LogWriter::new)
                        .collect(Collectors.toConcurrentMap(LogWriter::getFilePath, Function.identity()));
                //if collect is not empty
                if (!collect.isEmpty()) {
                    //clear old content
                    filePathToInstanceMap.clear();
                    //put all new content
                    filePathToInstanceMap.putAll(collect);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //judge whether the filePathToInstanceMap is empty or not
        if (filePathToInstanceMap.isEmpty()) {
            //if empty, exit
            log.error("No log file configured");
            System.exit(1);
        } else {
            //print log file list
            final List<String> filePathList = filePathToInstanceMap.values()
                    .stream()
                    .map(LogWriter::getFilePath)
                    .collect(Collectors.toList());
            log.info("Log files :" + filePathList.toString());
        }
    }

    /**
     * Get log files list string
     *
     * @return log files list string
     */
    String filePaths() {
        //lazy initialize
        if (filePaths == null) {
            final StringBuilder stringBuilder = new StringBuilder("Log files:")
                    .append(Constants.NEW_LINE);
            stringBuilder.append(filePathToInstanceMap.values().stream().collect(Collector.of(
                    StringBuilder::new,
                    (StringBuilder sb, LogWriter l) -> sb.append(l.getFilePath()).append(Constants.NEW_LINE),
                    StringBuilder::append,
                    Collector.Characteristics.IDENTITY_FINISH
            )));
            filePaths = stringBuilder.append(Constants.NEW_LINE).toString();
        }
        //return
        return filePaths;
    }

    /**
     * Subscribe a log file
     *
     * @param channel   the channel instance of the client requested
     * @param arguments arguments string list
     * @return message return to client
     */
    String subscribe(final Channel channel, final List<String> arguments) {
        log.info("Subscribe '{}' from {}", arguments.toString(), channel.remoteAddress());
        //judge arguments length
        if (arguments.size() == 1) {
            //if length is 1
            final String filePath = arguments.get(0);
            //judge whether client already subscribed or not
            final LogWriter subscribed = subscribed(channel);
            if (subscribed != null) {
                //subscribed
                //judge whether request log file is the subscribed log file or not
                if (Objects.equals(subscribed.getFilePath(), filePath)) {
                    //yes
                    return String.format(Constants.SUCCESS_SUBSCRIBED, filePath);
                } else {
                    //not
                    return Constants.ERROR_MULTIPLE_SUBSCRIBTION;
                }
            } else {
                //not subscribed
                //judge request log file exists or not
                final LogWriter logWriter = filePathToInstanceMap.get(filePath);
                if (logWriter != null) {
                    //exists
                    logWriter.subscribe(channel);
                    //subscribtion success
                    return String.format(Constants.SUCCESS_SUBSCRIBED, filePath);
                } else {
                    //not exists
                    //subscribtion error
                    return String.format(Constants.ERROR_FILE_NOT_FOUND, filePath);
                }
            }
        } else {
            //if length is not 1
            return String.format(Constants.ERROR_INVALID_ARGUMENTS, arguments.toString());
        }
    }

    /**
     * Unsubscribe
     *
     * @param channel the channel instance of the client requested
     * @return the message to return to the client.
     */
    String unsubscribe(final Channel channel) {
        log.info("Unsubscribe " + channel.remoteAddress());
        //get the subscribed log writer.
        final LogWriter subscribed = subscribed(channel);
        if (subscribed != null) {
            subscribed.unsubscribe(channel);
            //if not null, the client already subscribed a log file.
            return Constants.SUCCESS_UNSUBSCRIBED;
        } else {
            //if null, the client didn't subscribe any log file.
            return Constants.ERROR_NOT_SUBSCRIBED;
        }
    }

    /**
     * Get the subscribed log file of the specified channel
     *
     * @param channel the channel instance of the client requested
     * @return if the specified channel subscribed, return the subscribed log file , otherwise return null
     */
    private LogWriter subscribed(final Channel channel) {
        for (final LogWriter logWriter : filePathToInstanceMap.values()) {
            if (logWriter.contains(channel)) {
                return logWriter;
            }
        }
        return null;
    }

    /**
     * Judge a line of log file configuration file is valid or not
     *
     * @param line the line of log file configuration
     * @return if valid return true, otherwise return null
     */
    private boolean isValidLogFile(final String line) {
        return !line.isEmpty() &&
                !line.startsWith(Constants.COMMENT_SYMBOL) &&
                Files.exists(Paths.get(line));
    }
}
