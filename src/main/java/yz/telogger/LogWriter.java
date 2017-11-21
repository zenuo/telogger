package yz.telogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 日志书写器
 *
 * @author 袁臻
 * 2017/11/21 23:52
 */
final class LogWriter {

    private final BufferedReader bufferedReader;

    /**
     * 构造方法
     *
     * @throws IOException 若指令为空字符串
     */
    LogWriter() throws IOException {
        final InputStream inputStream = Runtime
                .getRuntime()
                .exec(String.format(Constant.COMMAND, Constant.LOG_FILE))
                .getInputStream();
        this.bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
    }

    /**
     * 包装run方法，使其能
     *
     * @return
     */
    Void work() {
        this.bufferedReader.lines().
                forEach(line -> ClientManager.INSTANCE.write(line.concat("\n")));
        return null;
    }
}
