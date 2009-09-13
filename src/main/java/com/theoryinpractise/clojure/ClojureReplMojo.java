package com.theoryinpractise.clojure;

import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Mojo to start a clojure repl
 * <p/>
 * (C) Copyright Tim Dysinger   (tim -on- dysinger.net)
 * Mark Derricutt (mark -on- talios.com)
 * Dimitry Gashinsky (dimitry -on- gashinsky.com)
 * Scott Fleckenstein (nullstyle -on- gmail.com)
 * <p/>
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * @goal repl
 * @execute phase="compile"
 * @requiresDependencyResolution compile
 */
public class ClojureReplMojo extends AbstractClojureCompilerMojo {
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
     * @parameter
     */
    private File[] sourceDirectories = new File[] {new File("src/main/clojure")};

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

    public void execute() throws MojoExecutionException {

        List<File> dirs = new ArrayList<File>();
        if (sourceDirectories != null) {
            dirs.addAll(Arrays.asList(sourceDirectories));
        }
        dirs.add(generatedSourceDirectory);
        
        String[] args = new String[0];
        
        if (replScript != null && new File(replScript).exists()) {
          args = new String[] { replScript };   
        }
        
        callClojureWith(dirs.toArray(new File[]{}), outputDirectory, classpathElements, "clojure.main", args);
        
    }

}
