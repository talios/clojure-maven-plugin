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

import java.io.File;

/**
 * @goal testCompile
 * @phase test-compile
 * @requiresDependencyResolution test
 */
public class TestClojureCompilerMojo extends AbstractClojureCompilerMojo {

    /**
     * Flag to allow test compiliation to be skipped.
     *
     * @parameter expression="${maven.test.skip}" default-value="false"
     * @noinspection UnusedDeclaration
     */
    private boolean skip;

    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("Test compilation is skipped");
        } else {
            final File[] testSourceDirectories = getSourceDirectories(SourceDirectory.TEST);
            callClojureWith(testSourceDirectories, testOutputDirectory, testClasspathElements, "clojure.lang.Compile",
                    new NamespaceDiscovery(getLog(), testOutputDirectory, testDeclaredNamespaceOnly, true).discoverNamespacesIn(testNamespaces, testSourceDirectories));
        }
    }

}
