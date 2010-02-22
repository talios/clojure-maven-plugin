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
     * args specified on the command line.
     *
     * @parameter expression="${clojure.args}"
     */
    private String args;

    public void execute() throws MojoExecutionException {
        if (script == null || "".equals(script) || !(new File(script).exists())) {
            throw new MojoExecutionException("script is empty or does not exist!");
        } else {

            List<String> clojureArguments = new ArrayList<String>();
            clojureArguments.add(script);

            if (args != null) {
                clojureArguments.addAll(Arrays.asList(args.split(" ")));
            }

            callClojureWith(
                getSourceDirectories(SourceDirectory.COMPILE),
                outputDirectory, classpathElements, "clojure.main",
                clojureArguments.toArray(new String[clojureArguments.size()]));
        }
    }

}
