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

        String[] args = new String[]{Integer.toString(port)};
        callClojureWith(getSourceDirectories(SourceDirectory.COMPILE, SourceDirectory.TEST),
                outputDirectory,
                classpathElements,
                "com.martiansoftware.nailgun.NGServer", args);
    }

}
