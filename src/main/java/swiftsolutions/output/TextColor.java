package swiftsolutions.output;

/**
 * An enumeration of the colors of texts that be displayed to the commmand line.
 */
public enum TextColor {
    GREEN("\u001B[32m"),
    RED("\u001B[31m"),
    BLACK("\u001B[30m"),
    RESET("\u001B[0m"),
    YELLOW("\u001B[33m"),
    BLUE("\u001B[34m"),
    PURPLE("\u001B[35m"),
    CYAN("\u001B[36m"),
    WHITE("\u001B[37m");

    private String _prefix;

    TextColor(String prefix) {
        this._prefix = prefix;
    }

    /**
     * @return the prefix that is prepended to a string to make it colorful in the CLI.
     */
    public String getPrefix() {
        return this._prefix;
    }

}
