package yz.telogger;

/**
 * Command class
 *
 * @author zenuo
 * 2017/12/05 18:04
 */
final class Command {

    private String name;

    private String command;

    private String workingDirectory;

    private String help;

    private boolean isInternal;

    Command(final String line, final boolean isInternal) {
        final String[] split = line.split(Constant.EXTERNAL_COMMAND_DELIMITER);
        this.name = split[0];
        this.command = split[1];
        this.workingDirectory = split[2];
        this.help = split[3];
        this.isInternal = isInternal;
    }

    String getName() {
        return name;
    }

    String getCommand() {
        return command;
    }

    String getWorkingDirectory() {
        return workingDirectory;
    }

    String getHelp() {
        return help;
    }

    boolean isInternal() {
        return isInternal;
    }

    @Override
    public String toString() {
        return "{" +
                "name='" + name + '\'' +
                ", command='" + command + '\'' +
                ", workingDirectory='" + workingDirectory + '\'' +
                ", help='" + help + '\'' +
                '}';
    }
}
