package swiftsolutions.unit;

import junit.framework.Assert;
import org.junit.Test;
import swiftsolutions.Main;

public class MainTest {

    @Test
    public void testMain() {
        Main main = new Main();
        Assert.assertTrue(main.testFunc().equals("memes"));
    }
}
