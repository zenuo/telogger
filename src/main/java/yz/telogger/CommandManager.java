package yz.telogger;

import io.netty.channel.Channel;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
        //加载内部命令
        //订阅文件
        final Command subscribe = new Command(Constant.COMMAND_INTERNAL_SUBSCRIBE, true);
        map.put(subscribe.getName(), subscribe);
        //取消订阅文件
        final Command unsubscribe = new Command(Constant.COMMAND_INTERNAL_UNSUBSCRIBE, true);
        map.put(unsubscribe.getName(), unsubscribe);

        //加载外部命令
        //若文件存在，则加载外部命令
        final Path path = Paths.get(Constant.COMMAND_CONF_PATH);
        if (Files.exists(path)) {
            try {
                Files.lines(path)
                        .map(String::trim)
                        .filter(line -> !line.startsWith("#") && line.length() != 0)
                        .map(line -> new Command(line, false))
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
    String help() {
        if (help == null) {
            final StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("可用命令:").append(Constant.NEW_LINE);
            map.forEach((key, command) ->
                    stringBuilder.append(
                            String.format("%-20s %s\r\n", command.getName(), command.getHelp())
                    )
            );
            help = stringBuilder
                    .append(Constant.NEW_LINE)
                    .append(LogWriterManager.INSTANCE.filePaths())
                    .toString();
        }
        return help;
    }

    /**
     * 执行传入的命令
     *
     * @param commandLineString 命令行字符串
     * @return 若存在对应的命令，则执行它并返回其输出字符串；否则返回帮助字符串
     */
    String exec(final Channel channel, final String commandLineString) {
        //使用空格切分命令行字符串
        final List<String> segments = new ArrayList<>(Arrays.asList(commandLineString.split(" ")));
        //命令字符串
        final String commandString = segments.remove(0);
        //获取命令实例
        final Command command = map.get(commandString);
        //判断是否存在
        if (command != null) {
            //命令存在
            logger.info("执行命令-" + commandLineString + "-" + segments.toString());
            if (command.isInternal()) {
                //内部命令
                return execInternal(channel, commandString, segments);
            } else {
                //外部命令
                return execExternal(command, segments);
            }
        } else {
            //命令不存在
            return String.format(Constant.ERROR_COMMAND_NOT_EXISTS, commandString, help());
        }
    }

    /**
     * 执行内部命令
     *
     * @param channel       请求执行命令的channel
     * @param commandString 命令字符串
     * @param arguments     参数字符串列表
     * @return 返回给客户端的信息
     */
    private String execInternal(final Channel channel, final String commandString, final List<String> arguments) {
        switch (commandString) {
            case "sub":
                return LogWriterManager.INSTANCE.subscribe(channel, arguments);
            case "unsub":
                return LogWriterManager.INSTANCE.unsunscribe(channel);
            default:
                return String.format(Constant.ERROR_COMMAND_NOT_EXISTS, commandString, help());
        }
    }

    /**
     * 执行外部命令
     *
     * @param command   命令字符串
     * @param arguments 参数字符串列表
     * @return 返回给客户端的信息
     */
    private String execExternal(final Command command, final List<String> arguments) {
        final StringBuilder stringBuilder = new StringBuilder();
        Process process = null;
        try {
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
            logger.warning("执行命令-异常-" + command.toString() + "-参数列表-" + arguments.toString());
            e.printStackTrace();
        } finally {
            if (process != null && process.isAlive()) {
                process.destroy();
            }
        }
        return stringBuilder.toString();
    }
}
