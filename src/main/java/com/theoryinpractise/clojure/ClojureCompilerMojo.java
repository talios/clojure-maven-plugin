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

import java.io.File;
import java.io.IOException;

/**
 * @goal compile
 * @phase compile
 * @requiresDependencyResolution compile
 */
public class ClojureCompilerMojo extends AbstractClojureCompilerMojo {

    /**
     * Should the compile phase create a temporary output directory for .class files?
     *
     * @parameter default-value="false"
     * @required
     */
    protected Boolean temporaryOutputDirectory;

    public void execute() throws MojoExecutionException {

        File outputPath = outputDirectory;
        if (temporaryOutputDirectory) {
            try {
                outputPath = File.createTempFile("classes", ".dir");
                getLog().debug("Compiling clojure sources to " + outputPath.getPath());
            } catch (IOException e) {
                throw new MojoExecutionException("Unable to create temporary output directory: " + e.getMessage());
            }
            outputPath.delete();
            outputPath.mkdir();
        }

        callClojureWith(
                getSourceDirectories(SourceDirectory.COMPILE),
                outputPath, classpathElements, "clojure.lang.Compile",
                discoverNamespaces());

        copyNamespaceSourceFilesToOutput(outputDirectory, discoverNamespacesToCopy());
    }

}
