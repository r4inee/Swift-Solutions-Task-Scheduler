package swiftsolutions.util;

/**
 * Created by Winston on 7/31/2018.
 */

public interface Observer<U> {
    public void update(Observable<? extends U> observer, U arg);
}
