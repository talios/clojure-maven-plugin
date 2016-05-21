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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * Mojo for running Marginalia. Allows configuration of the source list
 * and target directory.
 */
@Mojo(name = "marginalia", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.TEST)
public class ClojureMarginaliaMojo extends AbstractClojureCompilerMojo {

  /**
   * The Maven Project.
   */
  @Parameter(readonly = true, required = true, property = "project")
  private MavenProject project = null;

  @Parameter(property = "project.name")
  private String projectName;

  @Parameter(property = "project.version")
  private String projectVersion;

  @Parameter(property = "project.description")
  private String projectDescription;

  @Parameter(property = "project.build.directory")
  private String projectBuildDir;

  /**
   * Location of the source files.
   */
  @Parameter private String[] marginaliaSourceDirectories;

  /**
   * Location of the output files.
   */
  @Parameter(defaultValue = "${project.build.directory}/marginalia")
  private String marginaliaTargetDirectory;

  @Parameter private Map<String, String> marginalia;

  private Set<Artifact> filterScope(Set<Artifact> artifacts, String scope) {
    Vector to_remove = new Vector();
    for (Artifact artifact : artifacts) {
      if (artifact.getScope() != scope) to_remove.add(artifact);
    }
    artifacts.removeAll(to_remove);
    return artifacts;
  }

  private String quote(String s) {
    StringBuilder sb = new StringBuilder();
    sb.append("\"");
    sb.append(s);
    sb.append("\"");
    return sb.toString();
  }

  private String formatDependencies(Set<Artifact> artifacts) {
    StringBuilder sb = new StringBuilder();
    sb.append("[\n");

    for (Artifact artifact : artifacts) {
      sb.append("[\"");
      sb.append(artifact.getGroupId());
      sb.append("/");
      sb.append(artifact.getArtifactId());
      sb.append("\" \"");
      sb.append(artifact.getVersion());
      sb.append("\"]\n");
    }
    sb.append("]");
    return sb.toString();
  }

  private String formatMap(Map<String, String> map) {
    StringBuilder sb = new StringBuilder();
    sb.append("{\n");
    for (Map.Entry<String, String> entry : map.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();
      sb.append(" :" + key + " ");
      if (value != null) {
        sb.append(value);
      } else {
        sb.append("nil");
      }
      sb.append("\n");
    }
    sb.append("}\n");

    return sb.toString();
  }

  public void execute() throws MojoExecutionException {
    // Build a project info map
    Map<String, String> effectiveProps = new HashMap<String, String>();
    effectiveProps.put("name", quote(projectName));
    effectiveProps.put("version", quote(projectVersion));
    effectiveProps.put("description", quote(projectDescription));
    effectiveProps.put("dependencies", formatDependencies(filterScope(project.getDependencyArtifacts(), Artifact.SCOPE_COMPILE)));
    effectiveProps.put("dev-dependencies", formatDependencies(filterScope(project.getDependencyArtifacts(), Artifact.SCOPE_TEST)));

    if (marginalia != null) {
      effectiveProps.put("marginalia", formatMap(marginalia));
    }

    boolean multiDoc = (marginalia == null ? false : ("true".equals(marginalia.get("multi"))));

    // Build the script to run marginalia
    StringBuilder sb = new StringBuilder();

    sb.append("(use `marginalia.core '[marginalia.html :only (*resources*)])\n");
    // fix per https://github.com/fogus/marginalia/issues/43
    sb.append("(binding [*resources* \"\"]\n");

    sb.append("(ensure-directory! \"");
    sb.append(marginaliaTargetDirectory);
    sb.append("\")\n");

    sb.append(multiDoc ? "(multidoc!\n" : "(uberdoc!\n");

    // Create the output destination
    sb.append("  \"");
    sb.append(marginaliaTargetDirectory);
    if (!multiDoc) sb.append("/uberdoc.html");
    sb.append("\"\n");

    // Create the list of sources to process
    sb.append("  (format-sources [");

    // Append the explicit marginalia source paths, or project source paths
    for (String entry : (marginaliaSourceDirectories != null && marginaliaSourceDirectories.length > 0 ? marginaliaSourceDirectories : sourceDirectories)) {
      sb.append("\"");
      sb.append(baseDirectory);
      sb.append("/");
      sb.append(entry);
      sb.append("\" ");
    }
    sb.append("])\n");

    // and the project map
    sb.append(formatMap(effectiveProps));
    sb.append(")\n");
    sb.append(")\n");

    // Run it
    try {
      File marginaliaClj = File.createTempFile("marginalia", ".clj");
      final PrintWriter pw = new PrintWriter(marginaliaClj);
      pw.print(sb.toString());
      pw.close();

      getLog().info("Generating marginalia docs");
      getLog().debug(sb.toString());

      callClojureWith(
          getSourceDirectories(SourceDirectory.COMPILE, SourceDirectory.TEST),
          outputDirectory,
          testClasspathElements,
          "clojure.main",
          new String[] {marginaliaClj.getPath()});

    } catch (IOException e) {
      throw new MojoExecutionException(e.getMessage());
    }
  }
}
