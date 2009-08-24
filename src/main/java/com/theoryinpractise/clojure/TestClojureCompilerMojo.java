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
     * Location of the file.
     *
     * @parameter expression="${project.build.testOutputDirectory}"
     * @required
     */
    private File outputDirectory;

    /**
     * Flag to allow test compiliation to be skipped.
     *
     * @parameter expression="${maven.test.skip}" default-value="false"
     * @noinspection UnusedDeclaration
     */
    private boolean skip;

    /**
     * Location of the source files.
     *
     * @parameter
     */
    private File[] testSourceDirectories = new File[] {new File("src/test/clojure")};

    /**
     * Project classpath.
     *
     * @parameter expression="${project.testClasspathElements}"
     * @required
     * @readonly
     */
    private List classpathElements;

    /**
     * Should we compile all namespaces or only those defined?
     * @parameter defaut-value="false"
     */
    private boolean compileDeclaredNamespaceOnly;

    /**
     * A list of namespaces to compile
     *
     * @parameter
     */
    private String[] namespaces;

    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("Test compiliation is skipped");
        } else {
            List<File> dirs = new ArrayList<File>();
            dirs.addAll(Arrays.asList(testSourceDirectories));

            final File[] allSourceDirectories = dirs.toArray(new File[]{});
            callClojureWith(allSourceDirectories, outputDirectory, classpathElements, "clojure.lang.Compile",
                    new NamespaceDiscovery(getLog(), compileDeclaredNamespaceOnly).discoverNamespacesIn(namespaces, allSourceDirectories));
        }
    }

}
