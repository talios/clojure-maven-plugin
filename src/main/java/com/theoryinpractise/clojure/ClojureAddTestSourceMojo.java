package com.theoryinpractise.clojure;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.File;

/**
 * Add Clojure test source directories to the POM
 */
@Mojo(name = "add-test-source", defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES)
public class ClojureAddTestSourceMojo extends AbstractClojureCompilerMojo {
  public void execute() throws MojoExecutionException, MojoFailureException {
    for (File file : this.getSourceDirectories(SourceDirectory.TEST)) {
      this.project.addTestCompileSourceRoot(file.getAbsolutePath());
    }
  }
}
