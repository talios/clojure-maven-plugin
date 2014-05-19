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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

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
    @Parameter
    private String testScript;

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

    public void execute() throws MojoExecutionException {
        if (skip || skipTests) {
            getLog().info("Test execution is skipped");
        } else {
            final File[] testSourceDirectories = getSourceDirectories(SourceDirectory.TEST);
            final File[] allSourceDirectories = getSourceDirectories(SourceDirectory.TEST, SourceDirectory.COMPILE);

            // if the test script is supposed to be found on the classpath, skip all file checking
            if (!isClasspathResource(testScript)) {

                if (testScript == null || "".equals(testScript) || !(new File(testScript).exists())) {
                    // Generate test script
                    try {
                        File outputFile = new File(testOutputDirectory);
                        outputFile.mkdir();

                        NamespaceInFile[] ns = new NamespaceDiscovery(getLog(), outputFile, charset, testDeclaredNamespaceOnly).discoverNamespacesIn(testNamespaces, testSourceDirectories);

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
                                runTestLine.append("\n");
                                runTestLine.append("(with-open [writer (clojure.java.io/writer \"" + escapeFilePath(testOutputDirectory, namespace.getName() + ".xml") + "\") ");
                                runTestLine.append("            escaped (xml-escaping-writer writer)] ");
                                runTestLine.append("\n");
                                runTestLine.append("(binding [*test-out* writer *out* escaped]");
                                runTestLine.append("\n");
                                runTestLine.append(" (with-junit-output ");
                                runTestLine.append("\n");
                                runTestLine.append("(run-tests");
                                runTestLine.append(" '" + namespace.getName());
                                runTestLine.append("))))");
                            } else {
                                // Use with-test-out to fix with-junit-output for Clojure 1.2 (See http://dev.clojure.org/jira/browse/CLJ-431)
                                runTestLine.append("\n");
                                runTestLine.append("(with-open [writer (clojure.java.io/writer \"" + escapeFilePath(testOutputDirectory, namespace.getName() + ".xml") + "\")] ");
                                runTestLine.append("(binding [*test-out* writer] ");
                                runTestLine.append("\n");
                                runTestLine.append(" (with-test-out ");
                                runTestLine.append("\n");
                                runTestLine.append(" (with-junit-output ");
                                runTestLine.append("\n");
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
            }

            getLog().debug("Running clojure:test-with-junit against " + testScript);

            callClojureWith(allSourceDirectories, outputDirectory, testClasspathElements, "clojure.main", new String[]{testScript});
        }
    }

    //  From: http://clojuredocs.org/clojure_core/clojure.main/main
    //  "Paths may be absolute or relative in the filesystem or relative to
    //  classpath. Classpath-relative paths have prefix of @ or @/"
    private boolean isClasspathResource(String script) {

        if (script == null) {
            return false;
        }

        if (script.length() == 0) {
            return false;
        }

        return testScript.charAt(0) == '@';
    }

}
