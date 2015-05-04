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

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.util.Properties;

@Mojo(name = "test", defaultPhase = LifecyclePhase.TEST, requiresDependencyResolution = ResolutionScope.TEST)
public class ClojureRunTestMojo extends ClojureRunTestWithJUnitMojo {
  /**
   * Whether to produce junit output or not
   *
   * @noinspection UnusedDeclaration
   */
  @Parameter(defaultValue = "false", property = "clojure.junitOutput")
  private boolean junitOutput;

  protected Properties getProps(NamespaceInFile[] ns) {
    Properties props = super.getProps(ns);
    props.put("junit", String.valueOf(junitOutput));
    return props;
  }
}
