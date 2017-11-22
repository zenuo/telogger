package yz.telogger;

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

    static final String COMMAND = "tail -f %s";

    static final int WRITER_IDLE_TIME_SECONDS = Integer.parseInt(System.getProperty("writerIdleTimeSeconds", "600"));

    static final String HELLO = "------------欢迎访问日志服务-----------\n";

    static final String BYE = "----------长时间无内容，关闭连接----------\n";

}
