package yz.telogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author 袁臻
 * 2017/12/06 16:32
 */
enum LogWriterManager {

    /**
     * 单例
     */
    INSTANCE;

    private final Logger logger = Logger.getLogger(LogWriterManager.class.getName());

    private final List<LogWriter> list = new ArrayList<>();

    /**
     * 启动
     */
    void boot() {
        final Path path = Paths.get(Constant.LOG_FILE_CONF_PATH);
        //若文件存在
        if (Files.exists(path)) {
            try {
                //读取配置文件的所有行，加入列表中
                list.addAll(Files.lines(path)
                        .map(String::trim)
                        .filter(line -> !line.startsWith("#") && line.length() != 0)
                        .map(LogWriter::new)
                        .collect(Collectors.toList()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            logger.info("********监听文件列表********");
            list.forEach(System.out::println);
            logger.info("**************************");
        }
        //判断映射是否为空
        if (list.isEmpty()) {
            throw new IllegalStateException("No file to listen, exit now.");
        } else {
            //创建轮寻文件列表线程
            final Thread thread = new Thread(this::manage);
            //设置为守护线程
            thread.setDaemon(true);
            //设置名称
            thread.setName("logWriterManagerThread");
            //启动线程
            thread.start();
            logger.info("轮寻文件列表线程已创建");
        }
    }

    private void manage() {
        while (true) {
            list.stream().filter(LogWriter::isNeedBoot)
                    .forEach(LogWriter::boot);
            try {
                Thread.sleep(5000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
