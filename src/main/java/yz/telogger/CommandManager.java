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
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * The manager of command, singleton.
 *
 * @author zenuo
 * 2017/12/05 18:01
 */
public enum CommandManager {

    /**
     * Instance of command manager.
     */
    INSTANCE;

    /**
     * Log of this instance.
     */
    private final Logger log = Logger.getLogger(CommandManager.class.getName());

    /**
     * The mapping from command name to its instance
     */
    private final ConcurrentHashMap<String, Command> internalCommandNameToInstanceMap = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, Command> externalCommandNameToInstanceMap = new ConcurrentHashMap<>();

    /**
     * The help message.
     */
    private String help = null;

    /**
     * Initialize, load command to commandNameToInstanceMap.
     */
    void init() {
        //load commands
        if (internalCommandNameToInstanceMap.isEmpty()) {
            loadInternalCommand();
        }
        loadExternalCommand();
        //log command names list
        final List<String> commandNameList = internalCommandNameToInstanceMap.values()
                .stream()
                .map(Command::getName)
                .collect(Collectors.toList());
        commandNameList.addAll(externalCommandNameToInstanceMap.values()
                .stream()
                .map(Command::getName)
                .collect(Collectors.toList())
        );
        log.info("Commands: " + commandNameList);
    }

    /**
     * Load internal commands
     */
    private void loadInternalCommand() {
        //load internal command
        //command 'sub'
        final Command subscribe = new Command(Constant.COMMAND_INTERNAL_SUBSCRIBE, true);
        //command 'unsub'
        final Command unsubscribe = new Command(Constant.COMMAND_INTERNAL_UNSUBSCRIBE, true);
        //command 'help'
        final Command help = new Command(Constant.COMMAND_INTERNAL_HELP, true);
        //command 'quit'
        final Command quit = new Command(Constant.COMMAND_INTERNAL_QUIT, true);
        //command 'reload'
        final Command reload = new Command(Constant.COMMAND_INTERNAL_RELOAD, true);
        internalCommandNameToInstanceMap.put(subscribe.getName(), subscribe);
        internalCommandNameToInstanceMap.put(unsubscribe.getName(), unsubscribe);
        internalCommandNameToInstanceMap.put(help.getName(), help);
        internalCommandNameToInstanceMap.put(quit.getName(), quit);
        internalCommandNameToInstanceMap.put(reload.getName(), reload);
    }

    /**
     * Load external commands
     */
    void loadExternalCommand() {
        //load external command
        //if command configuration file exists, load it
        final Path path = Paths.get(Constant.COMMAND_CONF_PATH);
        if (Files.exists(path)) {
            try {
                final ConcurrentMap<String, Command> collect = Files.lines(path)
                        .map(String::trim)
                        .filter(this::isValidExternalCommand)
                        .map(line -> new Command(line, false))
                        .collect(Collectors.toConcurrentMap(Command::getName, Function.identity()));
                //if collect is not empty
                if (!collect.isEmpty()) {
                    //clear old content
                    externalCommandNameToInstanceMap.clear();
                    //put all new content
                    externalCommandNameToInstanceMap.putAll(collect);
                }
            } catch (IOException e) {
                log.warning("Exception occurred while loading external command:");
                e.printStackTrace();
            }
        } else {
            log.info("External command not configured.");
        }
    }

    /**
     * Get help message
     *
     * @param reload is reloading help message
     * @return help message string
     */
    String help(boolean reload) {
        //lazy initialize
        if (help == null || reload) {
            //new StringBuilder
            final StringBuilder stringBuilder = new StringBuilder("Commands:")
                    .append(Constant.NEW_LINE);
            //append commands info
            stringBuilder.append(internalCommandNameToInstanceMap.values()
                    .stream()
                    .collect(Collector.of(
                            StringBuilder::new,
                            (StringBuilder sb, Command c) -> sb
                                    .append(String.format("%-30s %s\r\n", c.getName(), c.getHelp())),
                            StringBuilder::append,
                            Collector.Characteristics.IDENTITY_FINISH
                    )));
            stringBuilder.append(externalCommandNameToInstanceMap.values()
                    .stream()
                    .collect(Collector.of(
                            StringBuilder::new,
                            (StringBuilder sb, Command c) -> sb
                                    .append(String.format("%-30s %s\r\n", c.getName(), c.getHelp())),
                            StringBuilder::append,
                            Collector.Characteristics.IDENTITY_FINISH
                    )));
            //assign stringBuilder to help
            help = stringBuilder
                    .append(Constant.NEW_LINE)
                    .append(LogWriterManager.INSTANCE.filePaths())
                    .toString();
        }
        return help;
    }

    /**
     * Re-opening log files
     *
     * @param channel the channel instance of the client requested
     * @return message that send to the client
     */
    String reload(final Channel channel) {
        log.info("Reload-" + channel.remoteAddress());
        LogWriterManager.INSTANCE.init();
        CommandManager.INSTANCE.init();
        return help(true);
    }

    /**
     * Execute the command that client requested
     *
     * @param channel           the channel instance of the client requested
     * @param commandLineString string client requested
     * @return if command found, execute it, return its output, otherwise return error message
     */
    String exec(final Channel channel, final String commandLineString) {
        //split commandLineString by space
        final List<String> segments = new ArrayList<>(Arrays.asList(commandLineString.split(" ")));
        //get command name
        final String commandString = segments.remove(0);
        //log
        log.info("Command-" + channel.remoteAddress() + "-" + commandLineString);
        //get command instance
        Command command = internalCommandNameToInstanceMap.get(commandString);
        if (command == null) {
            command = externalCommandNameToInstanceMap.get(commandString);
        }
        //judge whether it found or not
        if (command != null) {
            //found
            if (command.isInternal()) {
                //internal command
                return execInternalCommand(channel, commandString, segments);
            } else {
                //external command
                return execExternalCommand(command, segments);
            }
        } else {
            //not found
            return String.format(Constant.ERROR_COMMAND_NOT_FOUND, commandString);
        }
    }

    /**
     * Execute internal command
     *
     * @param channel       the channel instance of the client requested
     * @param commandString command string
     * @param arguments     argument string list
     * @return output
     */
    private String execInternalCommand(final Channel channel, final String commandString, final List<String> arguments) {
        switch (commandString) {
            case "sub":
                return LogWriterManager.INSTANCE.subscribe(channel, arguments);
            case "unsub":
                return LogWriterManager.INSTANCE.unsubscribe(channel);
            case "help":
                return help(false);
            case "reload":
                return reload(channel);
            default:
                return String.format(Constant.ERROR_COMMAND_NOT_FOUND, commandString);
        }
    }

    /**
     * Execute external command
     *
     * @param command   command instance
     * @param arguments argument string list
     * @return output
     */
    private String execExternalCommand(final Command command, final List<String> arguments) {
        //new StringBuilder
        final StringBuilder stringBuilder = new StringBuilder();
        //the process that executes command
        Process process = null;
        try {
            //executes the specified string command in a separate process
            process = Runtime.getRuntime().exec(
                    command.getCommand(),
                    null,
                    new File(command.getWorkingDirectory())
            );
            //get stdout lines, blocking until process returned
            new BufferedReader(new InputStreamReader(process.getInputStream()))
                    .lines()
                    .forEach(line -> stringBuilder.append(line).append(Constant.NEW_LINE));
            //blocking until process terminated
            process.waitFor(20000L, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            //exception occurred
            log.warning("Error-Command-" + command.toString() + "-arguments-" + arguments.toString());
            e.printStackTrace();
        } finally {
            //clear process
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }
        //return output
        return stringBuilder.toString();
    }

    /**
     * Judge whether external command valid or not
     *
     * @param line a line of external command configuration
     * @return true if valid, false otherwise
     */
    private boolean isValidExternalCommand(final String line) {
        if (!line.isEmpty() && !line.startsWith(Constant.COMMENT_SYMBOL)) {
            final Matcher matcher = Constant.PATTERN_EXTERNAL_COMMAND.matcher(line);
            final boolean lineMatches = matcher.find();
            final boolean workingDirectoryExists = Paths.get(matcher.group("workingDirectory")).toFile().exists();
            return lineMatches && workingDirectoryExists;
        } else {
            return false;
        }
    }
}
