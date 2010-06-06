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

import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @goal swank
 * @execute phase="compile"
 * @requiresDependencyResolution test
 */
public class ClojureSwankMojo extends AbstractClojureCompilerMojo {

    /**
     * The clojure script to preceding the switch to the repl
     *
     * @parameter
     */
    private String replScript;

    /**
     * @parameter expression="${clojure.swank.port}" default-value="4005"
     */
    protected int port;

    /**
     * @parameter expression="${clojure.swank.protocolVersion}"
     * default-value="2009-09-14"
     */
    protected String protocolVersion;


    public void execute() throws MojoExecutionException {
        File swankTempFile;
        try {
            swankTempFile = File.createTempFile("swank", ".port");
        } catch (java.io.IOException e) {
            throw new MojoExecutionException("could not create SWANK port file", e);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("(do ");
        sb.append("(swank.swank/ignore-protocol-version \"");
        sb.append(protocolVersion);
        sb.append("\") ");
        sb.append("(swank.swank/start-server \"");
        sb.append(swankTempFile.getAbsolutePath());
        sb.append("\" :port ");
        sb.append(Integer.toString(port));
        sb.append(" :dont-close true");
        sb.append("))");
        String swankLoader = sb.toString();

        List<String> args = new ArrayList<String>();
        if (replScript != null && new File(replScript).exists()) {
            args.add("-i");
            args.add(replScript);
        }

        args.add("-e");
        args.add("(require (quote swank.swank))");
        args.add("-e");
        args.add(swankLoader);

        callClojureWith(
                getSourceDirectories(SourceDirectory.TEST, SourceDirectory.COMPILE),
                outputDirectory, getRunWithClasspathElements(), "clojure.main",
                args.toArray(new String[args.size()]));

    }

}
