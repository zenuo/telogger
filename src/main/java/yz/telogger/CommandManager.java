package yz.telogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
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

    INSTANCE;

    private final Logger logger = Logger.getLogger(CommandManager.class.getName());

    private final Map<String, Command> map = new ConcurrentHashMap<>();

    private String help = null;

    CommandManager() {
        final Path path = Paths.get(Constant.COMMAND_CSV_PATH);
        //若文件存在
        if (Files.exists(path)) {
            try {
                Files.lines(path).filter(line -> !line.startsWith("#"))
                        .map(Command::new)
                        .forEach(command -> map.put(command.getName(), command));
            } catch (IOException e) {
                logger.warning("读取命令时发生异常");
                e.printStackTrace();
            }
        }
    }

    public String help() {
        if (help == null) {
            final StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("可用命令:\n");
            map.forEach((key, command) ->
                    stringBuilder.append(
                            String.format("%-10s %-10s\n", command.getName(), command.getHelp())
                    )
            );
            help = stringBuilder.toString();
        }
        return help;
    }

    public String exec(final String name) {
        final StringBuilder stringBuilder = new StringBuilder();
        final Command command = map.get(name);
        if (command != null) {
            try {
                logger.info("执行命令" + name);
                //新建进程执行命令
                final Process process = Runtime.getRuntime().exec(
                        command.getCommand(),
                        null,
                        new File(command.getWorkingDirectory())
                );
                //读取输出
                new BufferedReader(new InputStreamReader(process.getInputStream()))
                        .lines()
                        .forEach(line -> stringBuilder.append(line).append('\n'));
                //等待进程结束
                process.waitFor(10000L, TimeUnit.NANOSECONDS);
            } catch (Exception e) {
                logger.warning("执行命令-异常-" + command.toString());
                e.printStackTrace();
            }
        } else {
            stringBuilder.append("错误-命令")
                    .append(name)
                    .append("不存在.\n\n")
                    .append(help())
                    .append('\n');
        }
        return stringBuilder.toString();
    }
}
