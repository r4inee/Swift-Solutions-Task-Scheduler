package swiftsolutions.unit;

import org.junit.Test;
import swiftsolutions.exceptions.ArgumentFormatException;
import swiftsolutions.exceptions.InputException;
import swiftsolutions.exceptions.OutputException;

import static org.junit.Assert.assertEquals;

public class ExceptionsTest {

    /**
     * This test case tests that given a particular
     * string passed into the constructor for ArgumentFormatException
     * the call to message() will return the string
     */
    @Test
    public void testArgumentFormatException(){
        String string = "foo";
        ArgumentFormatException argumentFormatException = new ArgumentFormatException(string);
        assertEquals(string,argumentFormatException.getMessage());
    }

    /**
     * This test case tests that given a particular
     * string passed into the constructor for InputException
     * the call to message() will return the string
     */
    @Test
    public void testInputException(){
        String string = "foo";
        InputException inputException = new InputException(string);
        assertEquals(string,inputException.getMessage());
    }

    /**
     * This test cases tests that given a particular
     * string passed into the constructor for OutputException
     * the call to message() will return the string
     */
    @Test
    public void testOutputException(){
        String string = "foo";
        OutputException outputException = new OutputException(string);
        assertEquals(string,outputException.getMessage());
    }
}
