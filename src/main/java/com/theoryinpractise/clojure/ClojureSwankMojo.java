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

import org.apache.commons.lang.SystemUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Mojo(name = "swank", requiresDependencyResolution = ResolutionScope.TEST)
public class ClojureSwankMojo extends AbstractClojureCompilerMojo {

  /**
   * The clojure script to preceding the switch to the repl
   */
  @Parameter private String replScript;

  @Parameter(defaultValue = "4005", property = "clojure.swank.port")
  protected int port;

  @Parameter(defaultValue = "2009-09-14", property = "clojure.swank.protocolVersion")
  protected String protocolVersion;

  @Parameter(defaultValue = "iso-8859-1", property = "clojure.swank.encoding")
  protected String encoding;

  @Parameter(defaultValue = "localhost", property = "clojure.swank.host")
  protected String swankHost;

  public void execute() throws MojoExecutionException {
    StringBuilder sb = new StringBuilder();
    sb.append("(do ");
    sb.append("(swank.swank/start-server");
    sb.append(" :host \"").append(swankHost).append("\"");
    sb.append(" :port ");
    sb.append(Integer.toString(port));
    sb.append(" :encoding \"").append(encoding).append("\"");
    sb.append(" :dont-close true");
    sb.append("))");
    String swankLoader = sb.toString();

    if (SystemUtils.IS_OS_WINDOWS) {
      swankLoader = windowsEscapeCommandLineArg(swankLoader);
    }

    List<String> args = new ArrayList<String>();
    if (replScript != null && new File(replScript).exists()) {
      args.add("-i");
      args.add(replScript);
    }

    args.add("-e");
    args.add("(require (quote swank.swank))");
    args.add("-e");
    args.add(swankLoader);

    callClojureWith(
        getSourceDirectories(SourceDirectory.TEST, SourceDirectory.COMPILE),
        outputDirectory,
        getRunWithClasspathElements(),
        "clojure.main",
        args.toArray(new String[args.size()]));
  }

  private String windowsEscapeCommandLineArg(String arg) {
    return "\"" + arg.replace("\"", "\\\"") + "\"";
  }
}
