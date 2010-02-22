/*
 * Copyright (c) Mark Derricutt 2010.
 *
 * The use and distribution terms for this software are covered by the Eclipse Public License 1.0
 * (http://opensource.org/licenses/eclipse-1.0.php) which can be found in the file epl-v10.html
 * at the root of this distribution.
 *
 * By using this software in any fashion, you are agreeing to be bound by the terms of this license.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.theoryinpractise.clojure;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * Mojo to start a clojure REPL running vimclojure's nailgun.
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
