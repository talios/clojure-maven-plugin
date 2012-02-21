package com.theoryinpractise.clojure;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;

/**
 * Add Clojure source directories to the POM
 *
 * @goal add-source
 * @phase generate-sources
 */
public class ClojureAddSourceMojo extends AbstractClojureCompilerMojo {
    public void execute() throws MojoExecutionException, MojoFailureException {
        for (File file : this.getSourceDirectories(SourceDirectory.COMPILE)) {
            this.project.addCompileSourceRoot(file.getAbsolutePath());
        }
    }
}
