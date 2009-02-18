package com.theoryinpractice.clojure;

import org.apache.maven.plugin.logging.Log;

import java.io.InputStreamReader;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
* User: amrk
* Date: Feb 6, 2009
* Time: 5:30:55 PM
* To change this template use File | Settings | File Templates.
*/
public class OutputHandlder extends Thread {

    private Process process;
    private Log log;

    public OutputHandlder(Process process, Log log) {
        this.process = process;
        this.log = log;
    }

    @Override
    public void run() {
        try {
            InputStreamReader tempReader = new InputStreamReader(new BufferedInputStream(process.getInputStream()));
            while (true) {
                int line = tempReader.read();
                if (line == -1)
                    break;
                System.out.write(line);
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
