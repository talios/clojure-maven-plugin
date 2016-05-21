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
import java.util.ArrayList;
import java.util.Properties;

import static org.apache.commons.io.IOUtils.copy;

@Mojo(name = "test-with-junit", defaultPhase = LifecyclePhase.TEST, requiresDependencyResolution = ResolutionScope.TEST)
public class ClojureRunTestWithJUnitMojo extends AbstractClojureCompilerMojo {

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
   *
   * @noinspection UnusedDeclaration
   */
  @Parameter private String testScript;

  /**
   * Output directory for test results
   *
   * @noinspection UnusedDeclaration
   */
  @Parameter(defaultValue = "${project.build.directory}/test-reports", property = "clojure.testOutputDirectory")
  private String testOutputDirectory;

  /**
   * Whether to XML escape non-report output sent to *out*
   *
   * @noinspection UnusedDeclaration
   */
  @Parameter(defaultValue = "true", property = "clojure.xmlEscapeOutput")
  private boolean xmlEscapeOutput;

  /**
   * Provide a comma separated list of tests to run, instead of trying to run all of the test.
   */
  @Parameter(property = "test")
  private String test;

  public void execute() throws MojoExecutionException {
    if (skip || skipTests) {
      getLog().info("Test execution is skipped");
    } else {
      try {
        final File[] testSourceDirectories = getSourceDirectories(SourceDirectory.TEST);
        final File[] allSourceDirectories = getSourceDirectories(SourceDirectory.TEST, SourceDirectory.COMPILE);
        final File outputFile = new File(testOutputDirectory);
        NamespaceInFile[] ns =
            new NamespaceDiscovery(getLog(), outputFile, charset, testDeclaredNamespaceOnly).discoverNamespacesIn(testNamespaces, testSourceDirectories);
        if (test != null) {
          ArrayList<NamespaceInFile> filteredNS = new ArrayList<NamespaceInFile>();
          String[] patterns = test.split("\\s*,\\s*");
          for (NamespaceInFile nsinf : ns) {
            for (String pattern : patterns) {
              if (nsinf.getName().contains(pattern)) {
                filteredNS.add(nsinf);
                break;
              }
            }
          }
          ns = filteredNS.toArray(new NamespaceInFile[filteredNS.size()]);
        }
        File confFile = File.createTempFile("run-test", ".txt");
        confFile.deleteOnExit();
        final PrintWriter confWriter = new PrintWriter(new FileWriter(confFile));
        generateConfig(confWriter, ns);
        confWriter.close();
        String testConf = confFile.getPath();

        // if the test script is supposed to be found on the classpath, skip all file checking
        if (!isClasspathResource(testScript)) {
          if (!isExistingTestScriptFile(testScript)) {
            // Generate test script
            outputFile.mkdir();

            File testFile = File.createTempFile("run-test", ".clj");
            testFile.deleteOnExit();
            final PrintWriter writer = new PrintWriter(new FileWriter(testFile));

            generateTestScript(writer);

            writer.close();

            testScript = testFile.getPath();

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

        getLog().debug("Running clojure:test-with-junit against " + testScript);
        callClojureWith(allSourceDirectories, outputDirectory, testClasspathElements, "clojure.main", new String[] {testScript, testConf});
      } catch (IOException e) {
        throw new MojoExecutionException(e.getMessage(), e);
      }
    }
  }

  protected void generateConfig(PrintWriter writer, NamespaceInFile[] ns) throws IOException {
    Properties props = getProps(ns);
    props.store(writer, "Test Run Properties");
  }

  protected Properties getProps(NamespaceInFile[] ns) {
    Properties props = new Properties();
    for (int i = 0; i < ns.length; i++) {
      props.put("ns." + i, ns[i].getName());
    }
    props.put("junit", "True");
    props.put("outputDir", testOutputDirectory);
    props.put("xmlEscape", String.valueOf(xmlEscapeOutput));
    return props;
  }

  private void generateTestScript(PrintWriter writer) throws IOException {
    copy(ClojureRunTestWithJUnitMojo.class.getResourceAsStream("/default_test_script.clj"), writer);
  }
}
