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
 * @goal run
 * @requiresDependencyResolution compile
 */
public class ClojureRunMojo extends AbstractClojureCompilerMojo {
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
    private File baseSourceDirectory;

    /**
     * Location of the source files.
     *
     * @parameter
     */
    private File[] sourceDirectories;

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
     * The main clojure script to run
     *
     * @parameter
     * @required
     */
    private String script;

    public void execute() throws MojoExecutionException {
        if (script == null || "".equals(script) || !(new File(script).exists())) {
            throw new MojoExecutionException("script is empty or does not exist!");
        } else {
            List<File> dirs = new ArrayList<File>();
            dirs.add(baseSourceDirectory);
            if (sourceDirectories != null) {
                dirs.addAll(Arrays.asList(sourceDirectories));
            }
            dirs.add(generatedSourceDirectory);

            callClojureWith(dirs.toArray(new File[]{}), outputDirectory, classpathElements, "clojure.main", new String[]{script});
        }
    }

}
