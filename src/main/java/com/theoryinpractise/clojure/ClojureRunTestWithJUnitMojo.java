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

import java.io.*;

import static org.apache.commons.io.IOUtils.copy;

/**
 * @goal test-with-junit
 * @phase test
 * @requiresDependencyResolution test
 */
public class ClojureRunTestWithJUnitMojo extends AbstractClojureCompilerMojo {

    /**
     * Flag to allow test compiliation to be skipped.
     *
     * @parameter expression="${maven.test.skip}" default-value="false"
     * @noinspection UnusedDeclaration
     */
    private boolean skip;

    /**
     * The main clojure script to run
     *
     * @parameter
     */
    private String testScript;

    /**
     * Output directory for test results
     *
     * @parameter default-value="${project.build.directory}/test-reports"
     */
    private String testOutputDirectory;

    /**
     * Whether to XML escape non-report output sent to *out*
     *
     * @parameter default-value="true"
     */
    private boolean xmlEscapeOutput;

    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("Test execution is skipped");
        } else {
            final File[] testSourceDirectories = getSourceDirectories(SourceDirectory.TEST);
            final File[] allSourceDirectories = getSourceDirectories(SourceDirectory.TEST, SourceDirectory.COMPILE);

            if (testScript == null || "".equals(testScript) || !(new File(testScript).exists())) {
                // Generate test script
                try {
                    new File(testOutputDirectory).mkdir();

                    NamespaceInFile[] ns = new NamespaceDiscovery(getLog(), testDeclaredNamespaceOnly).discoverNamespacesIn(testNamespaces, testSourceDirectories);

                    File testFile = File.createTempFile("run-test", ".clj");
                    final PrintWriter writer = new PrintWriter(new FileWriter(testFile));

                    for (NamespaceInFile namespace : ns) {
                        writer.println("(require '" + namespace.getName() + ")");
                    }

                    StringWriter testCljWriter = new StringWriter();
                    copy(ClojureRunTestWithJUnitMojo.class.getResourceAsStream("/default_test_script.clj"), testCljWriter);

                    StringBuilder runTestLine = new StringBuilder();
                    for (NamespaceInFile namespace : ns) {
                        if (xmlEscapeOutput) {
                            // Assumes with-junit-output uses with-test-out internally when necessary.  xml escape anything sent to *out*.
                            runTestLine.append("(with-open [writer (clojure.java.io/writer \"" + escapeFilePath(testOutputDirectory, namespace.getName() + ".xml") + "\") ");
                            runTestLine.append("            escaped (xml-escaping-writer writer)] ");
                            runTestLine.append("(binding [*test-out* writer *out* escaped] (with-junit-output ");
                            runTestLine.append("(run-tests");
                            runTestLine.append(" '" + namespace.getName());
                            runTestLine.append("))))");
                        } else {
                            // Use with-test-out to fix with-junit-output until clojure #431 is fixed
                            runTestLine.append("(with-open [writer (clojure.java.io/writer \"" + escapeFilePath(testOutputDirectory, namespace.getName() + ".xml") + "\")] ");
                            runTestLine.append("(binding [*test-out* writer] (with-test-out (with-junit-output ");
                            runTestLine.append("(run-tests");
                            runTestLine.append(" '" + namespace.getName());
                            runTestLine.append(")))))");
                        }
                    }

                    String testClj = testCljWriter.toString().replace("(run-tests)", runTestLine.toString());

                    writer.println(testClj);

                    writer.close();

                    testScript = testFile.getPath();

                } catch (IOException e) {
                    throw new MojoExecutionException(e.getMessage(), e);
                }
            } else {
                File testFile = new File(testScript);

                if (!testFile.exists()) {
                    testFile = new File(getWorkingDirectory(), testScript);
                }

                if (!(testFile.exists())) {
                    throw new MojoExecutionException("testScript " + testFile.getPath() + " does not exist.");
                }
            }

            getLog().debug("Running clojure:test-with-junit against " + testScript);

            callClojureWith(allSourceDirectories, outputDirectory, testClasspathElements, "clojure.main", new String[]{testScript});
        }
    }

}
