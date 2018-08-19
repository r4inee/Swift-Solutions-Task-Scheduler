package swiftsolutions.unit.benchmark;

/**
 * Thrown if schedule parsed is not valid. See BenchMarkAppRunner
 */
public class InvalidScheduleException extends Exception{

    public InvalidScheduleException(String message){
        super(message);
    }

}