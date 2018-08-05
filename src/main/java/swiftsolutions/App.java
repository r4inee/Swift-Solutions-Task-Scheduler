package swiftsolutions;

public class App {
    public static void main(String[] args) {
        Scheduler scheduler = Scheduler.getContext();
        scheduler.start(args);
    }
}
