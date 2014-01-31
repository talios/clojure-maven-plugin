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
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;

@Mojo(name = "compile", defaultPhase = LifecyclePhase.COMPILE, requiresDependencyResolution = ResolutionScope.COMPILE)
public class ClojureCompilerMojo extends AbstractClojureCompilerMojo {

    /**
     * Should the compile phase create a temporary output directory for .class files?
     */
    @Parameter(required = true, defaultValue = "false")
    protected Boolean temporaryOutputDirectory;

    public void execute() throws MojoExecutionException {

        File outputPath = (temporaryOutputDirectory)
                          ? createTemporaryDirectory("classes")
                          : outputDirectory;

    	callClojureWith(
                getSourceDirectories(SourceDirectory.COMPILE),
                outputPath, classpathElements, "clojure.lang.Compile",
                discoverNamespaces());

        copyNamespaceSourceFilesToOutput(outputDirectory, discoverNamespacesToCopy());
    }

}
