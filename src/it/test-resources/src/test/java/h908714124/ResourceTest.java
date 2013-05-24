package h908714124;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ResourceTest {

    @Test
    public void okJar() throws IOException {
        String bip = "/test/build-info.properties";
        InputStream s = getClass().getResourceAsStream(bip);
        Assert.assertNotNull(s);
        InputStreamReader sr = new InputStreamReader(s);
        BufferedReader bsr = new BufferedReader(sr);
        String line;
        try {
            while ((line = bsr.readLine()) != null) {
                System.out.println(line);
            }
        } finally {
            bsr.close();
        }
    }

}
