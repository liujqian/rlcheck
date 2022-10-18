package edu.berkeley.cs.jqf.examples.str;

import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.runner.RunWith;

import java.io.InputStream;

@RunWith(JQF.class)
public class SimpleStringTest {
    @Fuzz
    public void testSimpleStr(InputStream s) {
        SimpleString simpleString = new SimpleString(s.toString());
        // Test out an arbitrary precondition: the generated string should not be empty and should have "a" as the first character.
        Assume.assumeTrue(simpleString.toString().length() > 0 && simpleString.toString().charAt(0) == 'a');
        Assert.assertNotNull(simpleString);
    }
}
