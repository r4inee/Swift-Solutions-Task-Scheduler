package swiftsolutions.util;

import java.io.Serializable;

/**
 *  This is a generic class used to represent a pair object in java.
 *  @param <A> the type of the first value
 *  @param <B> the type of the second value
 */
public class Pair<A,B> implements Serializable{
        private A _a;
        private B _b;

        public Pair(A a, B b) {
            _a = a;
            _b = b;
        }

    /**
     * @return the value of A
     */
    public A getA(){
            return _a;
        }

    /**
     * @return the value of B
     */
    public B getB(){
            return _b;
        }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        Pair pair = (Pair) obj;
        return (pair.getA().equals(_a) && pair.getB().equals(_b));
    }
}
