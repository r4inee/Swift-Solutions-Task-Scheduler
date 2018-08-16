package swiftsolutions.unit;

import org.junit.Before;
import org.junit.Test;
import swiftsolutions.cli.CLIArgumentParser;
import swiftsolutions.exceptions.ArgumentFormatException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * This class is used to test the CLIArgumentParser
 */
public class CLIArgumentParserTest {

    CLIArgumentParser _cliArgumentParser;
    /**
     * Initialise a new CLIArgumentParser before each test case
     */
    @Before
    public void init(){
        _cliArgumentParser = new CLIArgumentParser();
    }

    /**
     * This test case tests if the CLIArgumentParser correctly throws
     * a ArgumentFormatException if there are less than 2
     * arguments passed into parse() and the correct error
     * message is returned
     */
    @Test
    public void testParseNoArgs(){
        String[] input = new String[0];
        try {
            _cliArgumentParser.parse(input);
        } catch (ArgumentFormatException e) {
            assertEquals("Less arguments than expected, try using -h for help.", e.getMessage());
        }
    }

    /**
     * This test case tests that if an argument with two values
     * are passed to parse() and are valid then parse will
     * correctly parse the input
     */
    @Test
    public void testParseTwoArguments(){
        String[] input = new String[] {"foo","1"};
        try {
            _cliArgumentParser.parse(input);
        } catch (ArgumentFormatException e) {
            fail();
        }

    }

    /**
     * This test case tests if an argument with two values
     * are passed to parse() and the second value is not an
     * integer then an ArgumentFormatException with the correct
     * error message will be thrown
     */
    @Test
    public void testParseTwoArgumentsSecondInvalid(){
        String[] input = new String[] {"foo","foo"};
        try {
            _cliArgumentParser.parse(input);
        } catch (ArgumentFormatException e) {
            assertEquals("Second argument must be an integer, try using -h for help.",e.getMessage());
        }
    }

    /**
     * This test case tests if an argument with all valid flags
     * passed to parse() will parse correctly and no exception
     * is thrown
     */
    @Test
    public void testParseFlagValid(){
        String[] input = new String [] {"foo", "1", "-p 2", "-dir foo", "-o foo", "-verbose", "-v"};
        try {
            _cliArgumentParser.parse(input);
        } catch (ArgumentFormatException e) {
            fail();
        }
    }

    /**
     * This test case tests if an argument with an invalid first
     * flag, where the flag does not exist, passed to parse() will
     * throw an ArgumentFormatException with the correct
     * error message
     */
    @Test
    public void TestParseFirstFlagInvalid(){
        String[] input = new String[] {"foo","1","-c"};
        try {
            _cliArgumentParser.parse(input);
        } catch (ArgumentFormatException e) {
            assertEquals("Invalid first flag, try using -h for help.", e.getMessage());
        }
    }

    /**
     * This test case tests if an argument with an invalid
     * flag, where the flag does not exist, not in the first flag
     * passed to parse() will throw an ArgumentFormatException
     */
    @Test
    public void TestParseFlagInvalid(){
        String[] input = new String[] {"foo","1","-v","-c"};
        try {
            _cliArgumentParser.parse(input);
        } catch (ArgumentFormatException e) {
            e.printStackTrace();
        }
    }

    /**
     * This test case tests if an argument with a valid flags
     * correctly stores the flag argument to the CLIArgument
     * parser fields
     */
    @Test
    public void testCorrectFieldValues(){
        String[] input = new String[] {"foo", "1", "-p", "2", "-dir", "foo", "-o", "foo", "-h", "-v"};
        try{
            _cliArgumentParser.parse(input);
        } catch (ArgumentFormatException e) {
            fail();
        }
        assertEquals("foo",_cliArgumentParser.getFile().toString());
        int coresArg = _cliArgumentParser.getCoresOption().getArgs();
        assertEquals(2,coresArg);
        assertEquals(1,_cliArgumentParser.getProcessors());
        assertEquals("foo",_cliArgumentParser.getOutputOption().getArgs());
        assertTrue(_cliArgumentParser.getVisualizeOption().getArgs());
        assertTrue(_cliArgumentParser.getHelpOption().getArgs());
    }
}
