package swiftsolutions.output;

/**
 * An enumeration of the various types of output messages that one might output to the OutputManager.
 * See OutputManager.
 */
public enum OutputType {
    ERROR(TextColor.RED),
    STATUS(TextColor.BLUE),
    DEBUG(TextColor.PURPLE),
    SUCCESS(TextColor.GREEN),
    HELP(TextColor.YELLOW);

    // Color of the message to be displayed.
    private TextColor _color;

    OutputType(TextColor color) {
        _color = color;
    }

    /**
     * Makes a string that will have a color if output to the user.
     * @param msg message that will be displayed to the user.
     * @return the message with the color prefix and suffix added.
     */
    public String makeMsg(String msg) {
        return this._color.getPrefix() + "[" + this + "] " + msg + TextColor.RESET.getPrefix();
    }
}
