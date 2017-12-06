package yz.telogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * 命令管理器
 *
 * @author 袁臻
 * 2017/12/05 18:01
 */
public enum CommandManager {

    /**
     * 单例
     */
    INSTANCE;

    private final Logger logger = Logger.getLogger(CommandManager.class.getName());

    private final ConcurrentHashMap<String, Command> map = new ConcurrentHashMap<>();

    private String help = null;

    /**
     * 构造方法，实现读取文件到私有映射中
     */
    CommandManager() {
        final Path path = Paths.get(Constant.COMMAND_CONF_PATH);
        //若文件存在
        if (Files.exists(path)) {
            try {
                Files.lines(path)
                        .map(String::trim)
                        .filter(line -> !line.startsWith("#") && line.length() != 0)
                        .map(Command::new)
                        .forEach(command -> map.put(command.getName(), command));
            } catch (IOException e) {
                logger.warning("读取命令时发生异常");
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取帮助字符串
     *
     * @return 根据映射构建帮助字符串
     */
    private String help() {
        if (help == null) {
            final StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("可用命令:").append(Constant.NEW_LINE);
            map.forEach((key, command) ->
                    stringBuilder.append(
                            String.format("%-20s %-30s\r\n", command.getName(), command.getHelp())
                    )
            );
            help = stringBuilder.toString();
        }
        return help;
    }

    /**
     * 执行传入的命令
     *
     * @param commandName 命令名称字符串
     * @return 若存在命令名称对应的字符串，则执行它并返回其输出字符串；否则返回帮助字符串
     */
    public String exec(final String commandName) {
        final StringBuilder stringBuilder = new StringBuilder();
        final Command command = map.get(commandName);
        if (command != null) {
            Process process = null;
            try {
                logger.info("执行命令" + commandName);
                //新建进程执行命令
                process = Runtime.getRuntime().exec(
                        command.getCommand(),
                        null,
                        new File(command.getWorkingDirectory())
                );
                //读取输出，阻塞
                new BufferedReader(new InputStreamReader(process.getInputStream()))
                        .lines()
                        .forEach(line -> stringBuilder.append(line).append(Constant.NEW_LINE));
                //等待进程结束
                process.waitFor(1000L, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                logger.warning("执行命令-异常-" + command.toString());
                e.printStackTrace();
            } finally {
                if (process != null && process.isAlive()) {
                    process.destroy();
                }
            }
        } else {
            stringBuilder.append("错误-命令'")
                    .append(commandName)
                    .append("'不存在")
                    .append(Constant.NEW_LINE)
                    .append(Constant.NEW_LINE)
                    .append(help());
        }
        return stringBuilder.toString();
    }
}
