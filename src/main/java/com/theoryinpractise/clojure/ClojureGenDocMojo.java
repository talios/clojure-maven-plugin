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
import java.io.IOException;
import java.io.PrintWriter;

@Mojo(name = "gendoc", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.TEST)
public class ClojureGenDocMojo extends AbstractClojureCompilerMojo {

  /**
   * Should we compile all namespaces or only those defined?
   */
  @Parameter(defaultValue = "false")
  private boolean generateTestDocumentation;

  public void execute() throws MojoExecutionException {
    File genDocClj;
    File docsDir;
    try {
      genDocClj = File.createTempFile("generate-docs", ".clj");
      if (!outputDirectory.getParentFile().exists()) {
        outputDirectory.getParentFile().mkdir();
      }
      docsDir = new File(outputDirectory.getParentFile(), "clojure");
      getLog().debug("Creating documentation directory " + docsDir.getPath());
      docsDir.mkdir();
      System.out.println(docsDir.getPath() + " exists " + docsDir.exists());
    } catch (IOException e) {
      throw new MojoExecutionException(e.getMessage());
    }

    StringBuilder sb = new StringBuilder();
    sb.append("(use 'clojure.contrib.gen-html-docs)\n");
    sb.append("(generate-documentation-to-file \n");
    int count = 0;
    sb.append("  \"").append(docsDir.getPath().replace('\\', '/')).append("/index.html\"\n");
    sb.append("  [");

    final NamespaceInFile[] allNamespaces =
        new NamespaceDiscovery(getLog(), outputDirectory, charset, compileDeclaredNamespaceOnly)
            .discoverNamespacesIn(namespaces, getSourceDirectories(SourceDirectory.COMPILE, SourceDirectory.TEST));

    for (NamespaceInFile namespace : allNamespaces) {
      sb.append("'").append(namespace.getName());
      if (count++ < allNamespaces.length - 1) {
        sb.append("\n   ");
      }
    }
    sb.append("])\n");
    try {
      final PrintWriter pw = new PrintWriter(genDocClj);
      pw.print(sb.toString());
      pw.close();
      getLog().info("Generating docs to " + docsDir.getCanonicalPath() + " with " + genDocClj.getPath());
      getLog().debug(sb.toString());
    } catch (IOException e) {
      throw new MojoExecutionException(e.getMessage());
    }

    callClojureWith(
        getSourceDirectories(SourceDirectory.COMPILE, SourceDirectory.TEST),
        outputDirectory,
        testClasspathElements,
        "clojure.main",
        new String[] {genDocClj.getPath()});
  }
}
