package swiftsolutions.util;

import java.util.Objects;

/**
 * This is a generic class used to represent a pair object in java.
 * Used to represent a pair of start and end time.
 * @param <T>
 * @param <V>
 */
public class Tuple<T, V> implements Comparable<Tuple<T, V>> {
    T _a;
    V _b;

    public Tuple(T a, V b){
        _a = a;
        _b = b;
    }

    public T getA(){
        return _a;
    }
    public V getB(){
        return _b;
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Tuple)) {
            return false;
        }

        Tuple t = (Tuple) o;

        return (t._a == this._a) && (t._b == this._b);
    }

    @Override
    public int hashCode(){
        return Objects.hash(_a,_b);
    }

    @Override
    public int compareTo(Tuple<T, V> o) {
        Tuple<T, V> tuple = o;
        if ((Integer) (this.getA()) < (Integer) (tuple.getA())) {
            return -1;
        } else if ((Integer) (this.getA()) > (Integer) (tuple.getA())) {
            return 1;
        }
        return 0;
    }
}
