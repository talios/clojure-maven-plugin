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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mojo to start a clojure repl
 */
@Mojo(name = "repl", defaultPhase = LifecyclePhase.TEST_COMPILE, requiresDependencyResolution = ResolutionScope.TEST)
public class ClojureReplMojo extends AbstractClojureCompilerMojo {

  private static final String REPLY_REPLY_MAIN = "reply.ReplyMain";
  /**
   * The clojure script to preceding the switch to the repl
   */
  @Parameter private String replScript;

  private static final Pattern JLINE = Pattern.compile("^.*/jline-[^/]+.jar$");
  private static final Pattern ICLOJURE = Pattern.compile("^.*/iclojure(-[^/]+)?.jar$");
  private static final Pattern REPLY = Pattern.compile("^.*/reply(-[^/]+)?.jar$");

  boolean isJLineAvailable(List<String> elements) {
    return isPatternFoundInClasspath(elements, JLINE);
  }

  boolean isIClojureAvailable(List<String> elements) {
    return isPatternFoundInClasspath(elements, ICLOJURE);
  }

  boolean isReplyAvailable(List<String> elements) {
    return isPatternFoundInClasspath(elements, REPLY);
  }

  private boolean isPatternFoundInClasspath(List<String> elements, Pattern pattern) {
    if (elements != null) {
      for (String e : elements) {
        Matcher m = pattern.matcher(new File(e).toURI().toString());
        if (m.matches()) return true;
      }
    }
    return false;
  }

  public void execute() throws MojoExecutionException {

    List<String> args = new ArrayList<String>();
    String mainClass = "clojure.main";

    if (isIClojureAvailable(classpathElements)) {
      mainClass = "com.offbytwo.iclojure.Main";
    } else if (isReplyAvailable(classpathElements)) {
      mainClass = REPLY_REPLY_MAIN;
    } else if (isJLineAvailable(classpathElements)) {
      getLog().info("Enabling JLine support");
      args.add("clojure.main");
      mainClass = "jline.ConsoleRunner";
    }

    if (replScript != null && new File(replScript).exists()) {
      args.add("-i");
      args.add(replScript);
      if (!mainClass.equals(REPLY_REPLY_MAIN)) {
        args.add("-r");
      }
    }

    callClojureWith(
        ExecutionMode.INTERACTIVE,
        getSourceDirectories(SourceDirectory.TEST, SourceDirectory.COMPILE),
        outputDirectory,
        getRunWithClasspathElements(),
        mainClass,
        args.toArray(new String[args.size()]));
  }
}
