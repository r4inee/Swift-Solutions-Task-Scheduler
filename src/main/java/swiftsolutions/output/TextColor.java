package swiftsolutions.output;

/**
 * Created by Winston on 7/31/2018.
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

    public String getPrefix() {
        return this._prefix;
    }

}
