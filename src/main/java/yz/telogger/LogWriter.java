package yz.telogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 日志书写器
 *
 * @author 袁臻
 * 2017/11/21 23:52
 */
public enum LogWriter {

    INSTANCE;

    private BufferedReader bufferedReader;

    LogWriter() {
        try {
            final Process process = Runtime.getRuntime().exec(String.format(Constant.COMMAND, Constant.LOG_FILE));
            this.bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        } catch (IOException e) {
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
        this.bufferedReader.lines().
                forEach(line -> ClientManager.INSTANCE.write(line.concat("\n")));
        return null;
    }
}
