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
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
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
        File tempFile;
        try {
            tempFile  = File.createTempFile("runswank", ".clj");
            IOUtils.copy(this.getClass().getClassLoader().getResourceAsStream("runswank.clj"),new FileOutputStream(tempFile));
        } catch (IOException e) {
            throw new MojoExecutionException("unable to load runswank.clj into temporary file",e);
        }

        List<String> args = new ArrayList<String>();
        if (replScript != null && new File(replScript).exists()) {
            args.add("-i");
            args.add(replScript);
        }

        args.add(tempFile.getAbsolutePath());

        callClojureWith(
                getSourceDirectories(SourceDirectory.TEST, SourceDirectory.COMPILE),
                outputDirectory, getRunWithClasspathElements(), "clojure.main",
                args.toArray(new String[args.size()]));

    }

}
