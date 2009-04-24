package clojure;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

/**
 * Plugin for Clojure source compiling.
 * <p/>
 * (C) Copyright Tim Dysinger   (tim -on- dysinger.net)
 * Mark Derricutt (mark -on- talios.com)
 * Dimitry Gashinsky (dimitry -on- gashinsky.com)
 * <p/>
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * @goal test
 * @phase test
 * @requiresDependencyResolution test
 */
public class ClojureRunTestMojo extends AbstractClojureCompilerMojo {
    /**
     * Location of the file.
     *
     * @parameter default-value="${project.build.testOutputDirectory}"
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
     * @parameter default-value="${project.build.testSourceDirectory}"
     * @required
     */
    private File testSourceDirectory;

    /**
     * Project classpath.
     *
     * @parameter default-value="${project.testClasspathElements}"
     * @required
     * @readonly
     */
    private List<String> classpathElements;

    /**
     * The main clojure script to run
     *
     * @parameter
     */
    private String testScript;

    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("Test execution is skipped");
        } else {
            if (testScript == null || "".equals(testScript) || !(new File(testScript).exists())) {
                throw new MojoExecutionException("testScript is empty or does not exist!");
            } else {
                callClojureWith(testSourceDirectory, outputDirectory, classpathElements, "clojure.main", new String[]{testScript});
            }
        }
    }

}
