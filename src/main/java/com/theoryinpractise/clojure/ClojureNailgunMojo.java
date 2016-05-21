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

/**
 * Mojo to start a clojure REPL running vimclojure's nailgun.
 */
@Mojo(name = "nailgun", defaultPhase = LifecyclePhase.TEST_COMPILE, requiresDependencyResolution = ResolutionScope.TEST)
public class ClojureNailgunMojo extends AbstractClojureCompilerMojo {

  /**
   * The clojure script to preceding the switch to the repl
   */
  @Parameter private String replScript;

  @Parameter(defaultValue = "2113", property = "clojure.nailgun.port")
  protected int port;

  /**
   * pre vimclojure 2.2.0: com.martiansoftware.nailgun.NGServer
   */
  @Parameter(defaultValue = "vimclojure.nailgun.NGServer", property = "clojure.nailgun.server")
  protected String server;

  public void execute() throws MojoExecutionException {

    String[] args = new String[] {Integer.toString(port)};
    callClojureWith(getSourceDirectories(SourceDirectory.TEST, SourceDirectory.COMPILE), outputDirectory, getRunWithClasspathElements(), server, args);
  }
}
