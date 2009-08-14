/*
 * Created by IntelliJ IDEA.
 * User: amrk
 * Date: Apr 18, 2009
 * Time: 12:47:31 PM
 */
package clojure;

import org.apache.maven.plugin.logging.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class OutputHandler extends Thread {

    private Process process;

    public OutputHandler(Process process, Log log) {
        this.process = process;
    }

    @Override
    public void run() {
        try {
            InputStreamReader tempReader = new InputStreamReader
                    (new BufferedInputStream(process.getInputStream()));
            while (true) {
                int line = tempReader.read();
                if (line == -1)
                    break;
                System.out.write(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}