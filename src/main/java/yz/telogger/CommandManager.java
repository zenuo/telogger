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
import java.util.regex.Matcher;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * The manager of command, singleton.
 *
 * @author yziyz
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
    private final ConcurrentHashMap<String, Command> commandNameToInstanceMap = new ConcurrentHashMap<>();

    /**
     * The help message.
     */
    private String help = null;

    /**
     * Initialize, load command to commandNameToInstanceMap.
     */
    void init() {
        //load commands
        loadInternalCommand();
        loadExternalCommand();
        //log command names list
        final List<String> commandNameList = commandNameToInstanceMap.values()
                .stream()
                .map(Command::getName)
                .collect(Collectors.toList());
        log.info("Commands: " + commandNameList);
    }

    /**
     * Load internal commands
     */
    private void loadInternalCommand() {
        //load internal command
        //command 'sub'
        final Command subscribe = new Command(Constant.COMMAND_INTERNAL_SUBSCRIBE, true);
        commandNameToInstanceMap.put(subscribe.getName(), subscribe);
        //command 'unsub'
        final Command unsubscribe = new Command(Constant.COMMAND_INTERNAL_UNSUBSCRIBE, true);
        commandNameToInstanceMap.put(unsubscribe.getName(), unsubscribe);
        //command 'help'
        final Command help = new Command(Constant.COMMAND_INTERNAL_HELP, true);
        commandNameToInstanceMap.put(help.getName(), help);
        //command 'quit'
        final Command quit = new Command(Constant.COMMAND_INTERNAL_QUIT, true);
        commandNameToInstanceMap.put(quit.getName(), quit);
    }

    /**
     * Load external commands
     */
    private void loadExternalCommand() {
        //load external command
        //if command configuration file exists, load it
        final Path path = Paths.get(Constant.COMMAND_CONF_PATH);
        if (Files.exists(path)) {
            try {
                Files.lines(path)
                        .map(String::trim)
                        .filter(this::isValidExternalCommand)
                        .map(line -> new Command(line, false))
                        .forEach(command -> commandNameToInstanceMap.put(command.getName(), command));
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
     * @return help message string
     */
    String help() {
        //lazy initialize
        if (help == null) {
            //new StringBuilder
            final StringBuilder stringBuilder = new StringBuilder("Commands:")
                    .append(Constant.NEW_LINE);
            //append commands info
            stringBuilder.append(commandNameToInstanceMap.values()
                    .stream()
                    .collect(Collector.of(
                            StringBuilder::new,
                            (StringBuilder sb, Command c) -> sb
                                    .append(String.format("%-20s %s\r\n", c.getName(), c.getHelp())),
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
     * Execute the command that client requested
     *
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
        final Command command = commandNameToInstanceMap.get(commandString);
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
     * @param channel       the channel that
     * @param commandString command string
     * @param arguments     argument string list
     * @return output
     */
    private String execInternalCommand(final Channel channel, final String commandString, final List<String> arguments) {
        switch (commandString) {
            case "sub":
                return LogWriterManager.INSTANCE.subscribe(channel, arguments);
            case "unsub":
                return LogWriterManager.INSTANCE.unsunscribe(channel);
            case "help":
                return help();
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
