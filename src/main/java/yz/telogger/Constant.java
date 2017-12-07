package yz.telogger;

import java.io.File;

/**
 * 常量类
 *
 * @author 袁臻
 * 2017/11/21 23:55
 */
final class Constant {

    static final boolean SSL = System.getProperty("ssl") != null;

    static final int PORT = Integer.parseInt(System.getProperty("port", "8007"));

    static String TAIL_F_COMMAND = System.getProperty("os.name").toLowerCase().contains("windows") ?
            "powershell Get-Content -Wait -Tail 10 %s" :
            "tail --retry -f %s";

    static final int WRITER_IDLE_TIME_SECONDS = Integer.parseInt(System.getProperty("writerIdleTimeSeconds", "600"));

    static final String HELLO = "------------欢迎访问日志服务------------\r\n";

    static final String BYE = "---------------再见---------------\r\n";

    static final String COMMAND_CONF_PATH = System.getProperty("commandCsvPath", "." + File.separatorChar + "command.conf");

    static final String LOG_FILE_CONF_PATH = System.getProperty("logFileCsvPath", "." + File.separatorChar + "logfile.conf");

    static final String NEW_LINE = "\r\n";

    static final String COMMAND_NOT_EXISTS = "错误-命令'%s'不存在\r\n\r\n%s";

    static final String FILE_NOT_EXISTS = "错误-不存在文件-%s";

    static final String INTERNAL_COMMAND_SUBSCRIBE = "sub,,,订阅某一个日志文件 参数:想要订阅的文件名";

    static final String INTERNAL_COMMAND_UNSUBSCRIBE = "unsub,,,取消订阅 无参数";

    static final String NOT_SUBSCRIBED = "错误-未订阅";

}
