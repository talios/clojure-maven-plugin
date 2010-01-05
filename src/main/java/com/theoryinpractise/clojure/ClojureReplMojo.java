package com.theoryinpractise.clojure;

import java.io.File;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.List;
import java.util.regex.*;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * Mojo to start a clojure repl
 * <p/>
 * (C) Copyright Tim Dysinger   (tim -on- dysinger.net)
 * Mark Derricutt (mark -on- talios.com)
 * Dimitry Gashinsky (dimitry -on- gashinsky.com)
 * Scott Fleckenstein (nullstyle -on- gmail.com)
 * <p/>
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * @goal repl
 * @execute phase="compile"
 * @requiresDependencyResolution compile
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
        String mainClass = "clojure.lang.Repl";

        if (isJLineAvailable(classpathElements)) {
            getLog().info("Enabling JLine support");
            args.add("clojure.lang.Repl");
            mainClass = "jline.ConsoleRunner";
        }

        if (replScript != null && new File(replScript).exists()) {
            args.add(replScript);
        }

        callClojureWith(
                getSourceDirectories(SourceDirectory.COMPILE, SourceDirectory.TEST),
                outputDirectory, classpathElements, mainClass,
                args.toArray(new String[args.size()]));
    }

}
