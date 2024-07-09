package cn.edu.ecnu.stu.gateway;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.asynchttpclient.*;
import cn.edu.ecnu.stu.gateway.filter.flowCtl.GuavaCountLimiter;

import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp() {
        String regexp = "/checkcode/?(?<segment>.*)";
        String replacement = "/xxxx/${segment}";
        Pattern pattern = Pattern.compile(regexp);
        String origin = "/checkcode/asd";
        String newPath = pattern.matcher(origin).replaceAll(replacement);
        System.out.println(newPath);

    }

    public void testRateLimiter() throws InterruptedException {
        String key = "a";
        long permit = 10;
        GuavaCountLimiter instance = GuavaCountLimiter.getInstance(key);
        Thread.sleep(1000);
        for(int i = 0; i < 100; i++)
            System.out.println(instance.acquire());
    }
}
