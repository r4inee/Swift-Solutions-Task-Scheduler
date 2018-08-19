package swiftsolutions.unit.benchmark;

/**
 * Returns if the schedule was not optimal
 */
public class NonOptimalScheduleException extends Exception{
    public NonOptimalScheduleException(String message){
        super(message);
    }
}