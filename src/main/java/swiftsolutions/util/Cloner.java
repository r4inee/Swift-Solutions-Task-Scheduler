package swiftsolutions.util;

import java.io.*;

/**
 * Cloner to create a deep copy of objects.
 */
public class Cloner {

    /**
     * Creates a clone of the input object.
     * @param orig original object
     * @param <T> the type of the original object
     * @return a clone of the original object.
     */
    public <T> T  copy(T orig) {
        T obj = null;
        try {
            // Write the object out to a byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(orig);
            out.flush();
            out.close();

            // Make an input stream from the byte array and read
            // a copy of the object back in.
            ObjectInputStream in = new ObjectInputStream(
                    new ByteArrayInputStream(bos.toByteArray()));
            obj = (T)in.readObject();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        catch(ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
        return obj;
    }

}
