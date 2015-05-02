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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

import static org.apache.commons.io.IOUtils.copy;

@Mojo(name = "test", defaultPhase = LifecyclePhase.TEST, requiresDependencyResolution = ResolutionScope.TEST)
public class ClojureRunTestMojo extends AbstractClojureCompilerMojo {

  /**
   * Flag to allow test compiliation to be skipped.
   *
   * @noinspection UnusedDeclaration
   */
  @Parameter(required = true, property = "maven.test.skip", defaultValue = "false")
  private boolean skip;

  /**
   * Flag to allow test execution to be skipped.
   *
   * @noinspection UnusedDeclaration
   */
  @Parameter(required = true, property = "skipTests", defaultValue = "false")
  private boolean skipTests;

  /**
   * The main clojure script to run
   */
  @Parameter
  private String testScript;

  public void execute() throws MojoExecutionException {
    if (skip || skipTests) {
      getLog().info("Test execution is skipped");
    } else {
      try {
        final File[] testSourceDirectories = getSourceDirectories(SourceDirectory.TEST);
        final File[] allSourceDirectories = getSourceDirectories(SourceDirectory.TEST, SourceDirectory.COMPILE);
        final NamespaceInFile[] ns = new NamespaceDiscovery(getLog(), testOutputDirectory, charset, testDeclaredNamespaceOnly).discoverNamespacesIn(testNamespaces, testSourceDirectories);
        File confFile = File.createTempFile("run-test", ".txt");
        final PrintWriter confWriter = new PrintWriter(new FileWriter(confFile));
        generateConfig(confWriter, ns);
        confWriter.close();
        String testConf = confFile.getPath();

        if (!isClasspathResource(testScript)) {
          if (!isExistingTestScriptFile(testScript)) {
            // Generate test script
            File testFile = File.createTempFile("run-test", ".clj");
            final PrintWriter writer = new PrintWriter(new FileWriter(testFile));

            generateTestScript(writer, ns);

            writer.close();

            testScript = testFile.getPath();

          // throw new MojoExecutionException("testScript is empty or does not exist!");
          } else {
            File testFile = new File(testScript);

            if (!testFile.exists()) {
              testFile = new File(getWorkingDirectory(), testScript);
            }

            if (!(testFile.exists())) {
              throw new MojoExecutionException("testScript " + testFile.getPath() + " does not exist.");
            }
          }
        }

        getLog().debug("Running clojure:test against " + testScript);
        callClojureWith(allSourceDirectories, outputDirectory, testClasspathElements, "clojure.main", new String[] {testScript, testConf});
      } catch (IOException e) {
        throw new MojoExecutionException(e.getMessage(), e);
      }
    }
  }

  protected void generateConfig(PrintWriter writer, NamespaceInFile[] ns) throws IOException {
    Properties props = new Properties();
    for(int i = 0; i < ns.length; i++) {
      props.put("ns."+i, ns[i].getName());
    }
    props.put("junit", "false");
    props.put("outputDir", "/tmp/");//TODO fix this
    props.put("xmlEscape", "false");//TODO fix this
    props.store(writer,"Test Run Properties");
  }

  protected void generateTestScript(PrintWriter writer, NamespaceInFile[] ns) throws IOException {
    copy(ClojureRunTestMojo.class.getResourceAsStream("/default_test_script.clj"), writer);
  }

}
