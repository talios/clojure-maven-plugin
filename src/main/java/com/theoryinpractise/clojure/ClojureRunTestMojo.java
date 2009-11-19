package com.theoryinpractise.clojure;

import org.apache.maven.plugin.MojoExecutionException;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.commons.io.IOUtils.copy;

/**
 * Plugin for Clojure source compiling.
 * <p/>
 * (C) Copyright Tim Dysinger   (tim -on- dysinger.net)
 * Mark Derricutt (mark -on- talios.com)
 * Dimitry Gashinsky (dimitry -on- gashinsky.com)
 * <p/>
 * http://www.eclipse.org/legal/epl-v10.html
 *
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

            List<File> dirs = new ArrayList<File>();
            if (baseTestSourceDirectory != null) {
                dirs.add(baseTestSourceDirectory);
            }
            if (testSourceDirectories != null) {
                dirs.addAll(Arrays.asList(testSourceDirectories));
            }
            if (sourceDirectories != null) {
                dirs.addAll(Arrays.asList(sourceDirectories));
            }
            final File[] allSourceDirectories = dirs.toArray(new File[]{});

            if (testScript == null || "".equals(testScript) || !(new File(testScript).exists())) {

                // Generate test script

                try {
                    String[] ns = new NamespaceDiscovery(getLog(), compileDeclaredNamespaceOnly).discoverNamespacesIn(namespaces, allSourceDirectories);


                    File testFile = File.createTempFile("run-test", ".clj");
                    final PrintWriter writer = new PrintWriter(new FileWriter(testFile));

                    for (String namespace : ns) {
                        writer.println("(require '" + namespace + ")");
                    }

                    copy(ClojureRunTestMojo.class.getResourceAsStream("/default_test_script.clj"), writer);

                    writer.close();

                    testScript = testFile.getPath();

                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }


                // throw new MojoExecutionException("testScript is empty or does not exist!");
            }


            getLog().debug("Running clojure:test against " + testScript);

            callClojureWith(allSourceDirectories, outputDirectory, classpathElements, "clojure.main", new String[]{testScript});
        }
    }

}
