package swiftsolutions.unit;

import org.junit.Test;
import swiftsolutions.cli.options.*;
import swiftsolutions.exceptions.ArgumentFormatException;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * This class tests that the functionality for the different options are implemented correctly
 */
public class CLIOptionTest {
    /**
     * Test that a valid argument passed to verify does
     * not throw an exception for CoreOption
     */
    @Test
    public void testCoresValidVerify(){
        CoresOption coresOption = new CoresOption();
        ArrayList<String> input = new ArrayList<>();
        input.add("1");
        try {
            coresOption.verifyArgs(input);
        } catch (ArgumentFormatException e) {
            fail();
        }

    }

    /**
     * Test that a valid argument passed to verify()
     * for OutputOption does not throw an
     * ArgumentFormatException
     */
    @Test
    public void testOutputValidVerify(){
        OutputOption outputOption = new OutputOption();
        ArrayList<String> input = new ArrayList<>();
        input.add("foo");
        try {
            outputOption.verifyArgs(input);
        } catch (ArgumentFormatException e) {
            fail();
        }
    }

    /**
     * Test that an invalid argument passed to verify()
     * for OutputOption throws an ArgumentFormatException
     * with the correct message
     */
    @Test
    public void testOutputInvalidVerify(){
        OutputOption outputOption = new OutputOption();
        ArrayList<String> input = new ArrayList<>();
        input.add("foo");
        try {
            outputOption.verifyArgs(input);
        } catch (ArgumentFormatException e) {
            assertEquals("Number of arguments", e.getMessage());
        }
    }

    /**
     * Test that for a valid argument passed to verify()
     * for VerboseOption does not throw an ArgumentFormatException
     */
    @Test
    public void testVerboseValidVerify(){
        HelpOption helpOption = new HelpOption();
        ArrayList<String> input = new ArrayList<>();
        try {
            helpOption.verifyArgs(input);
        } catch (ArgumentFormatException e) {
            fail();
        }
    }

    /**
     * Test that for an invalid argument passed to verify()
     * for VerboseOption throws an ArgumentFormatException
     * and the correct message
     */
    @Test
    public void testVerboseInvalidVerify(){
        HelpOption helpOption = new HelpOption();
        ArrayList<String> input = new ArrayList<>();
        input.add("foo");
        try {
            helpOption.verifyArgs(input);
        } catch (ArgumentFormatException e) {
            assertEquals("Number of arguments", e.getMessage());
        }
    }

    /**
     * Test that for a valid argument passed to verify()
     * for VisualizeOption does not throw an ArgumentFormatException
     */
    @Test
    public void testVisualizeValidVerify(){
        VisualizeOption visualizeOption = new VisualizeOption();
        ArrayList<String> input = new ArrayList<>();
        try {
            visualizeOption.verifyArgs(input);
        } catch (ArgumentFormatException e) {
            fail();
        }
    }

    /**
     * Test that for a invalid argument passed to verify()
     * for the Visualize throws an ArgumentFormatException
     * with the correct message
     */
    @Test
    public void testVisualizeInvalidVerify(){
        VisualizeOption visualizeOption = new VisualizeOption();
        ArrayList<String> input = new ArrayList<>();
        input.add("foo");
        try {
            visualizeOption.verifyArgs(input);
        } catch (ArgumentFormatException e) {
            assertEquals("Number of arguments", e.getMessage());
        }
    }
}
