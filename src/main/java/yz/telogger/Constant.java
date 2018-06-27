package yz.telogger;

import java.io.File;
import java.util.regex.Pattern;

/**
 * Constant class
 *
 * @author zenuo
 * 2017/11/21 23:55
 */
final class Constant {

    /**
     * Configurations
     */
    static final boolean SSL = System.getProperty("ssl") != null;

    static final int PORT = Integer.parseInt(System.getProperty("port", "8007"));

    static final int WRITER_IDLE_TIME_SECONDS = Integer.parseInt(System.getProperty("writerIdleTimeSeconds", "600"));

    static final String COMMAND_CONF_PATH = System.getProperty("commandConfPath", "." + File.separatorChar + "command.conf");

    static final String LOG_FILE_CONF_PATH = System.getProperty("logFileConfPath", "." + File.separatorChar + "logfile.conf");

    static final String MESSAGE_HELLO = "-------Welcome to Telogger-------\r\n";

    static final String MESSAGE_BYE = "---------------Bye---------------\r\n";

    static final String NEW_LINE = "\r\n";

    static final String EXTERNAL_COMMAND_DELIMITER = "#";

    static final String COMMENT_SYMBOL = "//";

    static String TAIL_F_COMMAND = System.getProperty("os.name").toLowerCase().contains("windows") ?
            "\"C:\\Program Files\\Git\\usr\\bin\\tail.exe\" -n 0 --retry -f %s" :
            "tail -n 0 --retry -f %s";

    /**
     * Internal commands
     */
    static final String COMMAND_INTERNAL_SUBSCRIBE = "sub###subscribe a log file, if you already subscribed a log file," +
            " please unsubscribe it first. e.g. sub {FileName}";

    static final String COMMAND_INTERNAL_UNSUBSCRIBE = "unsub###unsubscribe the file you subscribed.";

    static final String COMMAND_INTERNAL_HELP = "help###get the help message.";

    static final String COMMAND_INTERNAL_QUIT = "quit###exit the session.";

    static final String COMMAND_INTERNAL_RELOAD = "reload###re-opening log files.";

    /**
     * Error messages
     */
    static final String ERROR_COMMAND_NOT_FOUND = "Error-command not found: %s\r\n";

    static final String ERROR_FILE_NOT_FOUND = "Error-file not found: %s\r\n";

    static final String ERROR_NOT_SUBSCRIBED = "Error-Sorry, you didn't subscribed any log files.\r\n";

    static final String ERROR_INVALID_ARGUMENTS = "Error-Invalid arguments: %s, please check the command by " +
            "getting help message, and retry.\r\n";

    static final String ERROR_MULTIPLE_SUBSCRIBTION = "Error-Sorry, you already subscribed a log file, please unsubscribe " +
            "it and retry.\r\n";

    /**
     * Success messages
     */
    static final String SUCCESS_SUBSCRIBED = "Success-Subscribed log file: %s\r\n";

    static final String SUCCESS_UNSUBSCRIBED = "Success-Unsubscribed log file.\r\n";

    /**
     * Regex pattern
     */
    static final Pattern PATTERN_EXTERNAL_COMMAND = Pattern.compile("^(?<name>.+?)#(?<commandString>.+?)#" +
            "(?<workingDirectory>.+?)#(?<help>.+?)$");

}
