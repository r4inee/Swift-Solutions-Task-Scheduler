package swiftsolutions;

/**
 * The class that will run the Scheduler application instance.
 */
public class App {

    /**
     * Main method that is called on application initialization
     * @param args the CLI arguments to be parsed by the application.
     */
    public static void main(String[] args) {
        Scheduler scheduler = Scheduler.getInstance();
        scheduler.start(args);
    }
}
