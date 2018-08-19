package swiftsolutions.util;

/**
 * Observer for the parameterized Observable type.
 * @param <U> the type of message that will be sent.
 */
public interface Observer<U> {
    /**
     * When the observer received a new message this will be called.
     * @param observable the observable that sent the message.
     * @param arg the message that was sent by the observable.
     */
    void update(Observable<? extends U> observable, U arg);
}
