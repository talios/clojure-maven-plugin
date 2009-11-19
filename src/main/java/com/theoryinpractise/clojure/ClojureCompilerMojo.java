package com.theoryinpractise.clojure;

import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Plugin for Clojure source compiling.
 * <p/>
 * (C) Copyright Tim Dysinger   (tim -on- dysinger.net)
 * Mark Derricutt (mark -on- talios.com)
 * Dimitry Gashinsky (dimitry -on- gashinsky.com)
 * <p/>
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * @goal compile
 * @phase compile
 * @requiresDependencyResolution compile
 */
public class ClojureCompilerMojo extends AbstractClojureCompilerMojo {

    public void execute() throws MojoExecutionException {
        callClojureWith(
                getSourceDirectories(SourceDirectory.COMPILE),
                outputDirectory, classpathElements, "clojure.lang.Compile",
                discoverNamespaces());
    }

}
