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
    /**
     * Location of the file.
     *
     * @parameter default-value="${project.build.outputDirectory}"
     * @required
     */
    private File outputDirectory;

    /**
     * Base directory of the project.
     *
     * @parameter expression="${basedir}"
     * @required
     * @readonly
     */
    private File baseDirectory;

    /**
     * Location of the source files.
     *
     * @parameter
     */
    private File[] sourceDirectories = new File[] {
        new File(baseDirectory, "src/main/clojure")
    };

    /**
     * Location of the generated source files.
     *
     * @parameter default-value="${project.build.outputDirectory}/../generated-sources"
     * @required
     */
    private File generatedSourceDirectory;

    /**
     * Project classpath.
     *
     * @parameter default-value="${project.compileClasspathElements}"
     * @required
     * @readonly
     */
    private List<String> classpathElements;

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
        List<File> dirs = new ArrayList<File>();
        if (sourceDirectories != null) {
            dirs.addAll(Arrays.asList(sourceDirectories));
        }
        dirs.add(generatedSourceDirectory);

        callClojureWith(dirs.toArray(new File[]{}), outputDirectory, classpathElements, "clojure.lang.Compile",
                new NamespaceDiscovery(getLog(), compileDeclaredNamespaceOnly).discoverNamespacesIn(namespaces, sourceDirectories));
    }

}
