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

    public void addObserver(final Observer<? super T> observer) {
        synchronized (observers) {
            if (!observers.contains(observer)) {
                observers.add(observer);
            }
        }
    }

    public void removeObserver(final Observer<? super T> observer) {
        synchronized (observers) {
            observers.remove(observer);
        }
    }

    public void clearObservers() {
        synchronized (observers) {
            this.observers.clear();
        }
    }

    public void setChanged() {
        synchronized (observers) {
            this.changed = true;
        }
    }

    public void clearChanged() {
        synchronized (observers) {
            this.changed = false;
        }
    }

    public boolean hasChanged() {
        synchronized (observers) {
            return this.changed;
        }
    }

    public int countObservers() {
        synchronized (observers) {
            return observers.size();
        }
    }

    public void notifyObservers() {
        notifyObservers(null);
    }

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