package com.theoryinpractise.clojure;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;

/**
 * Add Clojure test source directories to the POM
 *
 * @goal add-test-source
 * @phase generate-test-sources
 */
public class ClojureAddTestSourceMojo extends AbstractClojureCompilerMojo {
    public void execute() throws MojoExecutionException, MojoFailureException {
        for (File file : this.getSourceDirectories(SourceDirectory.TEST)) {
            this.project.addTestCompileSourceRoot(file.getAbsolutePath());
        }
    }
}
