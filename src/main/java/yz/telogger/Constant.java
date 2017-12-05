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

    static final String LOG_FILE = System.getProperty("logFile");

    static String TAIL_F_COMMAND = "tail --retry -f %s";

    static final int WRITER_IDLE_TIME_SECONDS = Integer.parseInt(System.getProperty("writerIdleTimeSeconds", "600"));

    static final String HELLO = "------------欢迎访问日志服务-----------\n";

    static final String BYE = "----------再见----------\n";

    static final String COMMAND_CSV_PATH = System.getProperty("commandCsvPath", "." + File.separatorChar + "command.csv");

    static {
        //Windows系统下的tail命令
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            TAIL_F_COMMAND = "powershell Get-Content -Wait -Tail 10 %s";
        }
    }
}
