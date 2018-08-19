package swiftsolutions.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

/**
 * like java.util.Observable, But uses generics to avoid need for a cast.
 *
 * For any un-documented variable, parameter or method, see java.util.Observable
 */
public class Observable<T> {

    private boolean changed = false;
    private final Collection<Observer<? super T>> observers;

    public Observable() {
        this(ArrayList::new);
    }

    public Observable(Supplier<Collection<Observer<? super T>>> supplier) {
        observers = supplier.get();
    }

    /**
     * @param observer an observer that will be notified of messages to the observable.
     */
    public void addObserver(final Observer<? super T> observer) {
        synchronized (observers) {
            if (!observers.contains(observer)) {
                observers.add(observer);
            }
        }
    }

    /**
     * @param observer an observer that would've been be notified of messages to the observable.
     */
    public void removeObserver(final Observer<? super T> observer) {
        synchronized (observers) {
            observers.remove(observer);
        }
    }

    /**
     * Remove all observers.
     */
    public void clearObservers() {
        synchronized (observers) {
            this.observers.clear();
        }
    }

    /**
     * Next time a message is pushed this observable will notify all observers of new messages.
     */
    public void setChanged() {
        synchronized (observers) {
            this.changed = true;
        }
    }

    /**
     * Next time a message is pushed observers will not be notified.
     */
    public void clearChanged() {
        synchronized (observers) {
            this.changed = false;
        }
    }

    /**
     * @return whether observers will be notified next time a message is pushed.
     */
    public boolean hasChanged() {
        synchronized (observers) {
            return this.changed;
        }
    }

    /**
     * @return the number of observers that are observer this observable.
     */
    public int countObservers() {
        synchronized (observers) {
            return observers.size();
        }
    }

    /**
     * Notify observers of a new message
     * @param value message for the observers.
     */
    public void notifyObservers(final T value) {
        ArrayList<Observer<? super T>> toNotify = null;
        synchronized(observers) {
            if (!changed) {
                return;
            }
            toNotify = new ArrayList<>(observers);
            changed = false;
        }
        for (Observer<? super T> observer : toNotify) {
            observer.update(this, value);
        }
    }
}