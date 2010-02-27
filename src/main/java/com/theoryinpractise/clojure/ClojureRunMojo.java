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
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @goal run
 * @requiresDependencyResolution compile
 */
public class ClojureRunMojo extends AbstractClojureCompilerMojo {

    /**
     * The main clojure script to run
     *
     * @parameter
     * @required
     */
    private String script;

    /**
     * Additional scripts to run
     *
     * @parameter
     */
    private String[] scripts;

    /**
     * args specified on the command line.
     *
     * @parameter expression="${clojure.args}"
     */
    private String args;

    public void execute() throws MojoExecutionException {

        if (script == null) {
            throw new MojoExecutionException("<script> is undefined");
        }
        if (scripts != null && scripts.length == 0) {
            throw new MojoExecutionException("<scripts> is defined but has no <script> entries");
        }

        List<String> scriptFiles = new ArrayList<String>();
        scriptFiles.add(script);
        if (scripts != null) {
            scriptFiles.addAll(Arrays.asList(scripts));
        }

        for (String scriptFile : scriptFiles) {
            if (scriptFile == null || "".equals(scriptFile)) {
                throw new MojoExecutionException("<script> entry cannot be empty");
            }
            if (!(new File(scriptFile).exists())) {
                throw new MojoExecutionException(scriptFile + " cannot be found");
            }
        }

        try {
            File testFile = File.createTempFile("run", ".clj");
            final PrintWriter writer = new PrintWriter(new FileWriter(testFile));

            for (String scriptFile : scriptFiles) {
                writer.println("(load-file \"" + scriptFile + "\")");
            }
            writer.close();


            List<String> clojureArguments = new ArrayList<String>();
            clojureArguments.add(testFile.getPath());

            if (args != null) {
                clojureArguments.addAll(Arrays.asList(args.split(" ")));
            }

            getLog().debug("Running clojure:run against " + testFile.getPath());

            callClojureWith(
                    getSourceDirectories(SourceDirectory.COMPILE),
                    outputDirectory, getRunWithClasspathElements(), "clojure.main",
                    clojureArguments.toArray(new String[clojureArguments.size()]));

        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }


}
