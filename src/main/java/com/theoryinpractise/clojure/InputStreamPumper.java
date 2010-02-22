/*
 * Copyright (c) Mark Derricutt 2010.
 *
 * The use and distribution terms for this software are covered by the Eclipse Public License 1.0
 * (http://opensource.org/licenses/eclipse-1.0.php) which can be found in the file epl-v10.html
 * at the root of this distribution.
 *
 * By using this software in any fashion, you are agreeing to be bound by the terms of this license.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.theoryinpractise.clojure;

import org.apache.commons.exec.util.DebugUtils;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * once https://issues.apache.org/jira/browse/EXEC-33 is fixed and used, this class can go.*
 * Copies all data from an input stream to an output stream.
 */
public class InputStreamPumper implements Runnable {
    public static final int SLEEPING_TIME = 100;

    /**
     * the input stream to pump from
     */
    private final InputStream is;

    /**
     * the output stream to pmp into
     */
    private final OutputStream os;

    private volatile boolean stop;


    /**
     * Create a new stream pumper.
     *
     * @param is input stream to read data from
     * @param os output stream to write data to.
     */
    public InputStreamPumper(final InputStream is, final OutputStream os) {
        this.is = is;
        this.os = os;
    }


    /**
     * Copies data from the input stream to the output stream. Terminates as
     * soon as the input stream is closed or an error occurs.
     */
    public void run() {
        stop = false;

        try {
            while (!stop) {
                while (is.available() > 0) {
                    os.write(is.read());
                }
                os.flush();
                Thread.sleep(SLEEPING_TIME);
            }
        } catch (Exception e) {
            String msg = "Got exception while reading/writing the stream";
            DebugUtils.handleException(msg, e);
        } finally {
        }
    }


    public void stopProcessing() {
        stop = true;
    }

}
