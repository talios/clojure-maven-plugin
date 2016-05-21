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

@Mojo(name = "testCompile", defaultPhase = LifecyclePhase.TEST_COMPILE, requiresDependencyResolution = ResolutionScope.TEST)
public class TestClojureCompilerMojo extends AbstractClojureCompilerMojo {

  /**
   * Flag to allow test compiliation to be skipped.
   */
  @Parameter(required = true, property = "maven.test.skip", defaultValue = "false")
  private boolean skip;

  /**
   * Should the test-compile phase create a temporary output directory for .class files?
   */
  @Parameter(required = true, defaultValue = "false")
  protected Boolean temporaryTestOutputDirectory;

  public void execute() throws MojoExecutionException {
    if (skip) {
      getLog().info("Test compilation is skipped");
    } else {
      File outputPath = (temporaryTestOutputDirectory) ? createTemporaryDirectory("test-classes") : testOutputDirectory;

      getLog().debug("Compiling clojure sources to " + outputPath.getPath());

      final File[] testSourceDirectories = getSourceDirectories(SourceDirectory.TEST);
      callClojureWith(
          testSourceDirectories,
          outputPath,
          testClasspathElements,
          "clojure.lang.Compile",
          new NamespaceDiscovery(getLog(), outputPath, charset, testDeclaredNamespaceOnly, true).discoverNamespacesIn(testNamespaces, testSourceDirectories));
    }
  }
}
