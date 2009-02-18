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
import java.util.*;

/**
 * Goal which touches a timestamp file.
 *
 * @goal compile
 * @phase compile
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
     * Project classpath.
     *
     * @parameter expression="${project.compileClasspathElements}"
     * @required
     * @readonly
     */
    private List classpathElements;

    /**
     * A list of namespaces to compile
     *
     * @parameter
     */
    private String[] namespaces;

    public void execute() throws MojoExecutionException {

        outputDirectory.mkdirs();

        String cp = srcDirectory.getPath() + File.pathSeparator + outputDirectory.getPath();
        for (String jarResource : findJarsForPackages("clojure")) {
            cp = cp + File.pathSeparator + jarResource;
        }

        for (Object classpathElement : classpathElements) {
            cp = cp + File.pathSeparator + classpathElement;
        }

        getLog().info("Compiling clojure sources with classpath: " + cp.toString());

        List<String> args = new ArrayList<String>();
        args.add("java");
        args.add("-cp");
        args.add(cp);
        args.add("-Dclojure.compile.path=" + outputDirectory.getPath() + "");
        args.add("clojure.lang.Compile");
        for (String namespace : namespaces) {
            args.add(namespace);
        }

        ProcessBuilder pb = new ProcessBuilder(args);
        pb.environment().put("path", ";");
        pb.environment().put("path", System.getProperty("java.home"));

        pb.redirectErrorStream(true);
        try {
            Process process = pb.start();
            new OutputHandlder(process, getLog()).start();

            int status;
            try {
                status = process.waitFor();
            } catch (InterruptedException e) {
                status = process.exitValue();
            }

            if (status != 0) {
                throw new MojoExecutionException("Clojure compilation failed.");
            }


        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage());
        }

    }

    private Set<String> findJarsForPackages(String packageName) throws MojoExecutionException {
        Set<String> jarResources = new HashSet<String>();
        Enumeration<URL> path = null;
        try {

            path = Thread.currentThread().getContextClassLoader().getResources(packageName.replaceAll("\\.","/"));
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage());
        }

        while (path.hasMoreElements()) {
            URL url = path.nextElement();

            getLog().debug(url.getPath());
            if ("jar".equals(url.getProtocol())) {
                jarResources.add(url.getPath().replaceFirst("file:", "").replaceFirst("jar.*", "jar"));
            }
        }
        return jarResources;
    }


}
