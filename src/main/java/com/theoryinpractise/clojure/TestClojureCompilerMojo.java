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
 * @goal testCompile
 * @phase test-compile
 * @requiresDependencyResolution test
 */
public class TestClojureCompilerMojo extends AbstractClojureCompilerMojo {

    /**
     * Flag to allow test compiliation to be skipped.
     *
     * @parameter expression="${maven.test.skip}" default-value="false"
     * @noinspection UnusedDeclaration
     */
    private boolean skip;

    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("Test compiliation is skipped");
        } else {
            final File[] allSourceDirectories = getSourceDirectories(SourceDirectory.COMPILE, SourceDirectory.TEST);
            callClojureWith(allSourceDirectories, outputDirectory, classpathElements, "clojure.lang.Compile",
                    new NamespaceDiscovery(getLog(), compileDeclaredNamespaceOnly).discoverNamespacesIn(namespaces, allSourceDirectories));
        }
    }

}
