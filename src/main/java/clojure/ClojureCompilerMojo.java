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
 *
 * (C) Copyright Tim Dysinger   (tim -on- dysinger.net)
 *               Mark Derricutt (mark -on- talios.com)
 *               Dimitry Gashinsky (dimitry -on- gashinsky.com)
 *
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * @goal compile
 * @phase compile
 * @requiresDependencyResolution compile
 */
public class ClojureCompilerMojo extends AbstractClojureCompilerMojo {
    /**
     * Location of the file.
     *
     * @parameter default-value="${project.build.outputDirectory}"
     * @required
     */
    private File outputDirectory;

    /**
     * Location of the source files.
     *
     * @parameter default-value="${project.build.sourceDirectory}"
     * @required
     */
    private File sourceDirectory;

    /**
     * Project classpath.
     *
     * @parameter default-value="${project.compileClasspathElements}"
     * @required
     * @readonly
     */
    private List<String> classpathElements;

    /**
     * A list of namespaces to compile
     *
     * @parameter
     * @required
     */
    private String[] namespaces;

    public void execute() throws MojoExecutionException {
        compileClojure(sourceDirectory, outputDirectory, classpathElements, namespaces);
    }

}
