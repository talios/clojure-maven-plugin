/*
 *  Copyright 2009 mkleint.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
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