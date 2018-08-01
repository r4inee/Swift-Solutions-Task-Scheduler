package swiftsolutions.util;

import java.io.Serializable;

/**
 * Created by Winston on 8/2/2018.
 */

    public class Pair<A,B> implements Serializable{
        public final A a;
        public final B b;

        public Pair(A a, B b) {
            this.a = a;
            this.b = b;
        }
}
