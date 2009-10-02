package com.theoryinpractise.clojure;

import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Mojo to start a clojure REPL running SWANK
 * <p/>
 * (C) Copyright Tim Dysinger   (tim -on- dysinger.net)
 * Mark Derricutt (mark -on- talios.com)
 * Dimitry Gashinsky (dimitry -on- gashinsky.com)
 * Scott Fleckenstein (nullstyle -on- gmail.com)
 * <p/>
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * @goal swank
 * @execute phase="compile"
 * @requiresDependencyResolution compile
 */
public class ClojureSwankMojo extends AbstractClojureCompilerMojo {
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
     * Location of the test source files.
     *
     * @parameter
     */
    private File[] testSourceDirectories = new File[] {new File("src/test/clojure")};

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
     * @parameter expression="${clojure.swank.port}" default-value="4005"
     */
    protected int port;

    /**
     * @parameter expression="${clojure.swank.protocolVersion}"
     *            default-value="2009-09-14"
     */
    protected String protocolVersion;


    public void execute() throws MojoExecutionException {

        List<File> dirs = new ArrayList<File>();
        if (sourceDirectories != null) {
            dirs.addAll(Arrays.asList(sourceDirectories));
        }
        if (testSourceDirectories != null) {
            dirs.addAll(Arrays.asList(testSourceDirectories));
        }
        dirs.add(generatedSourceDirectory);
        
        File swankTempFile;
        try {
             swankTempFile = File.createTempFile("swank", ".port");
        } catch (java.io.IOException e) {
            throw new MojoExecutionException("could not create SWANK port file", e);
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("(do ");
        sb.append("(swank.swank/ignore-protocol-version \"");
        sb.append(protocolVersion);
        sb.append("\") ");
        sb.append("(swank.swank/start-server \"");
        sb.append(swankTempFile.getAbsolutePath());
        sb.append("\" :port ");
        sb.append(Integer.toString(port));
		sb.append(" :dont-close true");
        sb.append("))");
        String swankLoader = sb.toString();

        String[] args = new String[] { "-e", "(require (quote swank.swank))",
                                       "-e", swankLoader };
        
        callClojureWith(dirs.toArray(new File[]{}), outputDirectory, classpathElements, "clojure.main", args);
        
    }

}
