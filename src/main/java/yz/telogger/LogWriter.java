package yz.telogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

/**
 * 日志书写器
 *
 * @author 袁臻
 * 2017/11/21 23:52
 */
public enum LogWriter {

    INSTANCE;

    private BufferedReader bufferedReader;

    private final Logger logger = Logger.getLogger(LogWriter.class.getName());

    LogWriter() {
        try {
            final Process process = Runtime.getRuntime()
                    .exec(String.format(Constant.TAIL_F_COMMAND, Constant.LOG_FILE));
            this.bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            logger.info("监听文件" + Constant.LOG_FILE);
        } catch (IOException e) {
            logger.warning("监听文件" + Constant.LOG_FILE + "发生异常:");
            e.printStackTrace();
            System.exit(2);
        }
    }

    /**
     * 向客户端写入新增的行
     *
     * @return null
     */
    Void work() {
        logger.info("写出文件的新增行");
        this.bufferedReader.lines().
                forEach(line -> ClientManager.INSTANCE.write(line.concat(Constant.NEW_LINE)));
        return null;
    }
}
