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

import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Mojo(name = "nrepl", requiresDependencyResolution = ResolutionScope.TEST)
public class ClojureNReplMojo extends AbstractClojureCompilerMojo {

  /**
   * The clojure script to preceding the switch to the repl
   */
  @Parameter private String replScript;

  @Parameter(defaultValue = "0", property = "clojure.nrepl.port")
  protected int port;

  @Parameter(defaultValue = "localhost", property = "clojure.nrepl.host")
  protected String nreplHost;

  @Parameter(property = "clojure.nrepl.unix.socket")
  protected String nreplUnixSocket;

  @Parameter(property = "clojure.nrepl.handler")
  private String nreplHandler;

  @Parameter(property = "clojure.nrepl.keepRunning")
  private boolean keepRunning;

  @Parameter protected String[] nreplMiddlewares;

  @Override
  public void execute() throws MojoExecutionException {
    StringBuilder sb = new StringBuilder();
    sb.append("(do ");
    sb.append("(nrepl.server/start-server");
    if (unixSocketConfigured()) {
      sb.append(" :socket \"").append(nreplUnixSocket).append("\"");
      } else {
      sb.append(" :bind \"").append(nreplHost).append("\"");
      sb.append(" :port ");
      sb.append(port);
    }
    appendNreplHandler(sb);
    if (middlewareConfigured() && noNreplHandlerAvailable()) {
      sb.append(" :handler (nrepl.server/default-handler ");
      for (String mw : nreplMiddlewares) {
        sb.append(" (resolve (quote ").append(mw).append(")) ");
      }
      sb.append(")");
    }
    sb.append("))");

    if (keepRunning) {
      sb.append("\n\n(future @(promise))\n\n");
    }

    String nreplLoader = sb.toString();

    if (SystemUtils.IS_OS_WINDOWS) {
      nreplLoader = windowsEscapeCommandLineArg(nreplLoader);
    }

    List<String> args = new ArrayList<String>();
    if (replScript != null && new File(replScript).exists()) {
      args.add("-i");
      args.add(replScript);
    }

    args.add("-e");
    args.add("(require (quote nrepl.server))");
    requireNreplHandlerNs(args);
    if (middlewareConfigured() && noNreplHandlerAvailable()) {
      for (String mw : nreplMiddlewares) {
        // there has to be a better way of doing this
        // using Clojure or EDN reader perhaps
        String [] ns_sym = mw.split("/");
        if (ns_sym.length == 2) {
          String ns = ns_sym[0];
          args.add("-e");
          args.add("(require (quote " + ns + "))");
        }
      }
    }
    args.add("-e");
    args.add(nreplLoader);

    callClojureWith(
        getSourceDirectories(SourceDirectory.TEST, SourceDirectory.COMPILE),
        outputDirectory,
        getRunWithClasspathElements(),
        "clojure.main",
        args.toArray(new String[args.size()]));
  }

  private void requireNreplHandlerNs(List<String> args) {
    if (noNreplHandlerAvailable()) {
      return;
    }
    args.add("-e");
    String nreplHandlerNs = nreplHandler.split("/")[0];
    args.add("(require (quote " + nreplHandlerNs + "))");
  }

  private boolean noNreplHandlerAvailable() {
    return nreplHandler == null || nreplHandler.trim().isEmpty();
  }

  private void appendNreplHandler(StringBuilder sb) {
    if (noNreplHandlerAvailable()) {
      return;
    }
    sb.append(" :handler ").append(nreplHandler);
  }

  private boolean middlewareConfigured() {
    return nreplMiddlewares != null && nreplMiddlewares.length > 0;
  }

  private boolean unixSocketConfigured() {
      return nreplUnixSocket != null && nreplUnixSocket.length() > 0;
  }

  private String windowsEscapeCommandLineArg(String arg) {
    return "\"" + arg.replace("\"", "\\\"") + "\"";
  }
}
