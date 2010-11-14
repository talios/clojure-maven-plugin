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
 * @goal test
 * @phase test
 * @requiresDependencyResolution test
 */
public class ClojureRunTestMojo extends AbstractClojureCompilerMojo {

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

    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("Test execution is skipped");
        } else {

            final File[] testSourceDirectories = getSourceDirectories(SourceDirectory.TEST);
            final File[] allSourceDirectories = getSourceDirectories(SourceDirectory.TEST, SourceDirectory.COMPILE);

            if (testScript == null || "".equals(testScript)) {

                // Generate test script

                try {
                    NamespaceInFile[] ns = new NamespaceDiscovery(getLog(), testDeclaredNamespaceOnly).discoverNamespacesIn(testNamespaces, testSourceDirectories);


                    File testFile = File.createTempFile("run-test", ".clj");
                    final PrintWriter writer = new PrintWriter(new FileWriter(testFile));

                    for (NamespaceInFile namespace : ns) {
                        writer.println("(require '" + namespace.getName() + ")");
                    }

                    String testClj = generateTestScript(ns);

                    writer.println(testClj);

                    writer.close();

                    testScript = testFile.getPath();

                } catch (IOException e) {
                    throw new MojoExecutionException(e.getMessage(), e);
                }


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

            getLog().debug("Running clojure:test against " + testScript);

            callClojureWith(allSourceDirectories, outputDirectory, testClasspathElements, "clojure.main", new String[]{testScript});
        }
    }

    protected String generateTestScript(NamespaceInFile[] ns) throws IOException {
        StringWriter testCljWriter = new StringWriter();
        copy(ClojureRunTestMojo.class.getResourceAsStream("/default_test_script.clj"), testCljWriter);

        StringBuilder runTestLine = new StringBuilder();
        runTestLine.append("(run-tests");
        for (NamespaceInFile namespace : ns) {
            runTestLine.append(" '" + namespace.getName());
        }
        runTestLine.append(")");

        return testCljWriter.toString().replace("(run-tests)", runTestLine.toString());
    }

}
