package swiftsolutions.output;

/**
 * Created by Winston on 7/31/2018.
 */
public enum OutputType {
    ERROR(TextColor.RED),
    STATUS(TextColor.BLUE),
    DEBUG(TextColor.PURPLE),
    SUCCESS(TextColor.GREEN),
    HELP(TextColor.YELLOW);

    private TextColor _color;

    OutputType(TextColor color) {
        _color = color;
    }

    public String makeMsg(String msg) {
        return this._color.getPrefix() + "[" + this + "] " + msg + TextColor.RESET.getPrefix();
    }
}
