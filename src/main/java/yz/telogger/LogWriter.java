package yz.telogger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

/**
 * 日志书写器
 *
 * @author 袁臻
 * 2017/11/21 23:52
 */
final class LogWriter {

    private final String filePath;

    private boolean isWorking = false;

    private Path path = null;

    private final Logger logger = Logger.getLogger(LogWriter.class.getName());

    /**
     * 构造方法
     */
    LogWriter(String filePath) {
        this.filePath = filePath;
        this.path = Paths.get(filePath);
    }

    /**
     * 是否需要启动
     *
     * @return 若需要被启动，返回true，否则返回false
     */
    boolean isNeedBoot() {
        //当且仅当文件不为空且文件存在且未在工作中时返回true
        return path != null && Files.exists(path) && !isWorking;
    }

    /**
     * 监听文件直至异常发生
     *
     * @return null
     */
    Void boot() {
        if (Files.exists(path)) {
            //设置isWorking为true
            isWorking = true;
            //进程引用
            Process process = null;
            try {
                //创建进程
                process = Runtime.getRuntime()
                        .exec(String.format(Constant.TAIL_F_COMMAND, filePath));
                logger.info("监听文件-" + filePath);
                //监听进程的输出，阻塞至异常抛出或者被关闭输出
                new BufferedReader(new InputStreamReader(process.getInputStream()))
                        .lines()
                        .forEach(ClientManager.INSTANCE::writeLine);
            } catch (Exception e) {
                logger.warning("监听文件-" + filePath + "-发生异常");
                e.printStackTrace();
            } finally {
                //设置isWorking为false
                isWorking = false;
                //停止进程
                if (process != null && process.isAlive()) {
                    process.destroy();
                }
            }
        }
        return null;
    }

    String getFilePath() {
        return filePath;
    }
}
