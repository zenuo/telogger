package yz.telogger;

import java.io.File;

/**
 * 常量类
 *
 * @author 袁臻
 * 2017/11/21 23:55
 */
final class Constant {

    /**
     * 可配置常量
     */
    static final boolean SSL = System.getProperty("ssl") != null;

    static final int PORT = Integer.parseInt(System.getProperty("port", "8007"));

    static final int WRITER_IDLE_TIME_SECONDS = Integer.parseInt(System.getProperty("writerIdleTimeSeconds", "600"));

    static final String COMMAND_CONF_PATH = System.getProperty("commandCsvPath", "." + File.separatorChar + "command.conf");

    static final String LOG_FILE_CONF_PATH = System.getProperty("logFileCsvPath", "." + File.separatorChar + "logfile.conf");

    /**
     * 不可配置常量
     */
    static final String HELLO = "------------欢迎访问日志服务------------\r\n";

    static final String BYE = "---------------再见---------------\r\n";

    static final String NEW_LINE = "\r\n";

    static String TAIL_F_COMMAND = System.getProperty("os.name").toLowerCase().contains("windows") ?
            "\"C:\\Program Files\\Git\\usr\\bin\\tail.exe\" -n 10 --retry -f %s" :
            "tail -n 10 --retry -f %s";

    /**
     * 内部命令
     */
    static final String COMMAND_INTERNAL_SUBSCRIBE = "sub,,,订阅某一个日志文件 参数:想要订阅的文件名";

    static final String COMMAND_INTERNAL_UNSUBSCRIBE = "unsub,,,取消订阅 无参数";

    /**
     * 错误
     */
    static final String ERROR_COMMAND_NOT_EXISTS = "错误-命令'%s'不存在\r\n\r\n%s";

    static final String ERROR_FILE_NOT_EXISTS = "错误-不存在文件-%s";

    static final String ERROR_NOT_SUBSCRIBED = "错误-未订阅";

    static final String ERROR_INVALID_ARGUMENTS = "错误-错误参数列表-%s";

    static final String ERROR_MULTI_SUBSCRIBE = "错误-多个订阅-请执行unsub以取消订阅后再试";

    /**
     * 成功
     */
    static final String SUCCESS_SUBSCRIBED = "订阅-成功-%s-%s";

    static final String SUCCESS_ALREADY_SUBCSRIBED = "订阅-已经订阅-%s-%s";

    static final String SUCCESS_UNSUBSCRIBED = "取消订阅-完成-%s";

}
