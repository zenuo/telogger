package yz.telogger;

import java.io.File;
import java.util.regex.Pattern;

/**
 * 常量类
 *
 * @author zenuo
 * 2017/11/21 23:55
 */
final class Constants {

    static final int DEFAULT_PORT = 8007;

    static final boolean DEFAULT_SSL_ENABLED = true;

    /**
     * 闲置时间，秒
     */
    static final int WRITER_IDLE_TIME_SECONDS = 600;

    /**
     * 配置文件路径字符串
     */
    static final String CONF_PATH_COMMAND = "." + File.separatorChar + "command.conf";

    static final String CONF_PATH_LOG_FILE = "." + File.separatorChar + "logfile.conf";

    static final String CONF_PATH_TELOGGER = "." + File.separatorChar + "telogger.conf";

    /**
     * 显示字符串
     */
    static final String MESSAGE_HELLO = "-------Welcome to Telogger-------\r\n";

    static final String MESSAGE_BYE = "---------------Bye---------------\r\n";

    static final String NEW_LINE = "\r\n";

    /**
     * 外置命令分隔符
     */
    static final String EXTERNAL_COMMAND_DELIMITER = "#";

    /**
     * 注释符号
     */
    static final String COMMENT_SYMBOL = "//";

    /**
     * 内置命令
     */
    static final String COMMAND_INTERNAL_SUBSCRIBE = "sub###subscribe a log file, if you already subscribed a log file," +
            " please unsubscribe it first. e.g. sub {file name}";
    static final String COMMAND_INTERNAL_UNSUBSCRIBE = "unsub###unsubscribe the file you subscribed.";
    static final String COMMAND_INTERNAL_HELP = "help###get the help message.";
    static final String COMMAND_INTERNAL_QUIT = "quit###exit the session.";
    static final String COMMAND_INTERNAL_RELOAD = "reload###reload log files configuration file.";

    /**
     * 错误消息
     */
    static final String ERROR_COMMAND_NOT_FOUND = "Error-command not found: %s\r\n";
    static final String ERROR_FILE_NOT_FOUND = "Error-file not found: %s\r\n";
    static final String ERROR_NOT_SUBSCRIBED = "Error-Sorry, you didn't subscribed any log files.\r\n";
    static final String ERROR_INVALID_ARGUMENTS = "Error-Invalid arguments: %s, please check the command by " +
            "getting help message, and retry.\r\n";
    static final String ERROR_MULTIPLE_SUBSCRIBTION = "Error-Sorry, you already subscribed a log file, please unsubscribe " +
            "it and retry.\r\n";

    /**
     * 成功消息
     */
    static final String SUCCESS_SUBSCRIBED = "Success-Subscribed log file: %s\r\n";
    static final String SUCCESS_UNSUBSCRIBED = "Success-Unsubscribed log file(s).\r\n";

    /**
     * 正则模式
     */
    static final Pattern PATTERN_EXTERNAL_COMMAND = Pattern.compile("^(?<name>.+?)#(?<commandString>.+?)#" +
            "(?<workingDirectory>.+?)#(?<help>.+?)$");

    /**
     * Tail命令模板
     */
    static String TAIL_F_COMMAND_TEMPLATE = "tail -n 0 --retry -f %s";

}
