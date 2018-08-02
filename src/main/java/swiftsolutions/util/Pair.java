package swiftsolutions.util;

import java.io.Serializable;

/**
 *  This is a generic class used to represent a pair object in java.
 *  Created by Winston on 8/2/2018.
 *  @param <A>
 *  @param <B>
 */
public class Pair<A,B> implements Serializable{
        private A _a;
        private B _b;

        public Pair(A a, B b) {
            _a = a;
            _b = b;
        }

        public A getA(){
            return _a;
        }
        public B getB(){
            return _b;
        }
}
