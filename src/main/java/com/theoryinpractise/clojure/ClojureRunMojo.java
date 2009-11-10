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
     * The main clojure script to run
     *
     * @parameter
     * @required
     */
    private String script;
    
    /**
     * args specified on the command line.
     *
     * @parameter expression="${clojure.args}"
     */
    private String args;

    public void execute() throws MojoExecutionException {
        if (script == null || "".equals(script) || !(new File(script).exists())) {
            throw new MojoExecutionException("script is empty or does not exist!");
        } else {
            List<File> dirs = new ArrayList<File>();
            if (sourceDirectories != null) {
                dirs.addAll(Arrays.asList(sourceDirectories));
            }
            dirs.add(generatedSourceDirectory);
            
            List<String> clojureArguments = new ArrayList<String>();
            clojureArguments.add(script);
            
            if(args != null) {
              clojureArguments.addAll(Arrays.asList(args.split(" ")));
            }
            
            callClojureWith(dirs.toArray(new File[]{}), outputDirectory, classpathElements, "clojure.main", clojureArguments.toArray(new String[clojureArguments.size()]));
        }
    }

}
