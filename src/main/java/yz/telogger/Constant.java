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

}
