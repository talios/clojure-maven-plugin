package com.theoryinpractise.clojure;

import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Mojo to start a clojure REPL running vimclojure's nailgun.
 * <p/>
 * (C) Copyright Tim Dysinger   (tim -on- dysinger.net)
 * Mark Derricutt (mark -on- talios.com)
 * Dimitry Gashinsky (dimitry -on- gashinsky.com)
 * Scott Fleckenstein (nullstyle -on- gmail.com)
 * Alexandre Patry (patryale -on- iro.umontreal.ca)
 * <p/>
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * @goal nailgun
 * @execute phase="compile"
 * @requiresDependencyResolution compile
 */
public class ClojureNailgunMojo extends AbstractClojureCompilerMojo {
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
     * Location of the test source files.
     *
     * @parameter
     */
    private File[] testSourceDirectories = new File[] {
        new File(baseDirectory, "src/test/clojure")
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
     * The clojure script to preceding the switch to the repl
     *
     * @parameter
     */
    private String replScript;

    /**
     * @parameter expression="${clojure.nailgun.port}" default-value="2113"
     */
    protected int port;

    public void execute() throws MojoExecutionException {

        List<File> dirs = new ArrayList<File>();
        if (sourceDirectories != null) {
            dirs.addAll(Arrays.asList(sourceDirectories));
        }
        if (testSourceDirectories != null) {
            dirs.addAll(Arrays.asList(testSourceDirectories));
        }
        dirs.add(generatedSourceDirectory);

        String[] args = new String[] {Integer.toString(port)};
        callClojureWith(dirs.toArray(new File[]{}),
                        outputDirectory,
                        classpathElements,
                        "com.martiansoftware.nailgun.NGServer", args);
    }

}
