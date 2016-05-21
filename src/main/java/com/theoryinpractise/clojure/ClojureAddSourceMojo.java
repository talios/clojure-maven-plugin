package com.theoryinpractise.clojure;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.File;

/**
 * Add Clojure source directories to the POM
 */
@Mojo(name = "add-source", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class ClojureAddSourceMojo extends AbstractClojureCompilerMojo {
  public void execute() throws MojoExecutionException, MojoFailureException {
    for (File file : this.getSourceDirectories(SourceDirectory.COMPILE)) {
      this.project.addCompileSourceRoot(file.getAbsolutePath());
    }
  }
}
