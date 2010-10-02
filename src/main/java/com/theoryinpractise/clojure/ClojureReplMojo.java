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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mojo to start a clojure repl
 *
 * @goal repl
 * @execute phase="test-compile"
 * @requiresDependencyResolution test
 */
public class ClojureReplMojo extends AbstractClojureCompilerMojo {

    /**
     * The clojure script to preceding the switch to the repl
     *
     * @parameter
     */
    private String replScript;

    private static final Pattern JLINE = Pattern.compile("^.*/jline-[^/]+.jar$");

    boolean isJLineAvailable(List<String> elements) {
        if (elements != null) {
            for (String e : elements) {
                Matcher m = JLINE.matcher(e);
                if (m.matches())
                    return true;
            }
        }
        return false;
    }

    public void execute() throws MojoExecutionException {

        List<String> args = new ArrayList<String>();
        String mainClass = "clojure.main";

        if (isJLineAvailable(classpathElements)) {
            getLog().info("Enabling JLine support");
            args.add("clojure.main");
            mainClass = "jline.ConsoleRunner";
        }

        if (replScript != null && new File(replScript).exists()) {
            args.add("-i");
            args.add(replScript);
            args.add("-r");
        }

        callClojureWith(
                ExecutionMode.INTERACTIVE,
                getSourceDirectories(SourceDirectory.TEST, SourceDirectory.COMPILE),
                outputDirectory, getRunWithClasspathElements(), mainClass,
                args.toArray(new String[args.size()]));
    }

}
