package com.theoryinpractice.clojure;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Goal which runs the clojure repl.
 *
 * @goal repl
 */
public class ClojureCompilerMojo extends AbstractMojo {

    /**
     * Location of the file.
     *
     * @parameter expression="${project.build.directory}/classes"
     * @required
     */
    private File outputDirectory;

    /**
     * Location of the source files.
     *
     * @parameter expression="src/main/clojure"
     * @required
     */
    private File srcDirectory;

    /**
     * A list of namespaces to compile
     *
     * @parameter
     */
    private String[] namespaces;

    public void execute() throws MojoExecutionException {

        outputDirectory.mkdirs();

        Enumeration<URL> path = null;
        try {
            path = Thread.currentThread().getContextClassLoader().getResources("clojure");
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage());
        }
        String cp = srcDirectory.getPath() + File.pathSeparator + outputDirectory.getPath();

        while (path.hasMoreElements()) {
            URL url = path.nextElement();

            getLog().debug(url.getPath());
            if ("jar".equals(url.getProtocol())) {
                cp = cp + File.pathSeparator + url.getPath().replaceFirst("file:", "").replaceFirst("jar.*", "jar");
            }
        }

        List<String> args = new ArrayList<String>();
        args.add("java");
        args.add("-cp");
        args.add(cp);
        args.add("clojure.lang.Repl");

        ProcessBuilder pb = new ProcessBuilder(args);
        pb.environment().put("path", ";");
        pb.environment().put("path", System.getProperty("java.home"));

        pb.redirectErrorStream(true);
        try {
            writeProcessOutput(pb.start());
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage());
        }

    }

    public void writeProcessOutput(Process process) throws IOException {
        InputStreamReader tempReader = new InputStreamReader(new BufferedInputStream(process.getInputStream()));
        BufferedReader reader = new BufferedReader(tempReader);
        while (true) {
            String line = reader.readLine();
            if (line == null)
                break;
            getLog().info(line);
        }
    }

}
