package clojure;

import org.apache.maven.plugin.MojoExecutionException;

import java.io.*;
import java.util.*;

/**
 * Plugin for Clojure source compiling.
 *
 * (C) Copyright Tim Dysinger   (tim -on- dysinger.net)
 *               Mark Derricutt (mark -on- talios.com)
 *               Dimitry Gashinsky (dimitry -on- gashinsky.com)
 *
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
     *
     * @noinspection UnusedDeclaration
     */
    private boolean skip;

    /**
     * Location of the source files.
     *
     * @parameter expression="src/test/clojure"
     * @required
     */
    private File sourceDirectory;

    /**
     * Project classpath.
     *
     * @parameter expression="${project.testClasspathElements}"
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
        if (skip) {
            getLog().info("Test compiliation is skipped");
        } else {
           callClojureWith(sourceDirectory, outputDirectory, classpathElements, "clojure.lang.Compile", namespaces);
        }
    }

}
