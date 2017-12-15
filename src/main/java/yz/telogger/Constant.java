package yz.telogger;

import java.io.File;

/**
 * Constant class
 *
 * @author yziyz
 * 2017/11/21 23:55
 */
final class Constant {

    /**
     * Configuration
     */
    static final boolean SSL = System.getProperty("ssl") != null;

    static final int PORT = Integer.parseInt(System.getProperty("port", "8007"));

    static final int WRITER_IDLE_TIME_SECONDS = Integer.parseInt(System.getProperty("writerIdleTimeSeconds", "600"));

    static final String COMMAND_CONF_PATH = System.getProperty("commandConfPath", "." + File.separatorChar + "command.conf");

    static final String LOG_FILE_CONF_PATH = System.getProperty("logFileConfPath", "." + File.separatorChar + "logfile.conf");

    static final String MESSAGE_HELLO = "-------Welcome to telogger-------\r\n";

    static final String MESSAGE_BYE = "---------------Bye---------------\r\n";

    static final String NEW_LINE = "\r\n";

    static String TAIL_F_COMMAND = System.getProperty("os.name").toLowerCase().contains("windows") ?
            "\"C:\\Program Files\\Git\\usr\\bin\\tail.exe\" -n 0 --retry -f %s" :
            "tail -n 0 --retry -f %s";

    /**
     * Internal command
     */
    static final String COMMAND_INTERNAL_SUBSCRIBE = "sub,,,subscribe a log file, if you already subscribed a log file," +
            " please unsubscribe it first. e.g. sub {FileName}";

    static final String COMMAND_INTERNAL_UNSUBSCRIBE = "unsub,,,unsubscribe the file you subscribed.";

    static final String COMMAND_INTERNAL_HELP = "help,,,get the help message.";

    static final String COMMAND_INTERNAL_QUIT = "quit,,,exit the session.";

    /**
     * Error message
     */
    static final String ERROR_COMMAND_NOT_FOUND = "Error-command not found: %s";

    static final String ERROR_FILE_NOT_FOUND = "Error-file not found: %s";

    static final String ERROR_NOT_SUBSCRIBED = "Error-Sorry, you didn't subscribed any log files.";

    static final String ERROR_INVALID_ARGUMENTS = "Error-Invalid arguments: %s, please check the command by " +
            "getting help message, and retry.";

    static final String ERROR_MULTIPLE_SUBSCRIBTION = "Error-Sorry, you already subscribed a log file, please unsubscribe " +
            "it and retry";

    /**
     * Success message
     */
    static final String SUCCESS_SUBSCRIBED = "Success-Subscribed log file: %s";

    static final String SUCCESS_UNSUBSCRIBED = "Success-Unsubscribed log file: %s";

}
