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
import org.apache.maven.plugin.logging.Log;
import org.junit.Test;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(Theories.class)
public class NamespaceDiscoveryTest {

    @Test
    public void testNamespaceDiscovery() throws MojoExecutionException {

        final NamespaceDiscovery namespaceDiscovery = new NamespaceDiscovery(mock(Log.class), true);

        List<String> namespaces = new ArrayList<String>() {{
            for (NamespaceInFile s : namespaceDiscovery.discoverNamespacesInPath(new File("src/test/resources"))) {
                System.out.println(s.getName());
                add(s.getName());
            }
        }};

        assertThat(namespaces)
                .isNotNull()
                .isNotEmpty()
                .hasSize(4)
                .contains("test1")
                .contains("test2")
                .contains("test.test3")
                .contains("nsmeta")
                .excludes("test.test4");
    }

    public static class NamespaceData {
        public String[] namespaces;
        public File[] sourceDirectories;
        public boolean compileDeclaredNamespaceOnly;

        public int expectedSize;

        public NamespaceData(String[] namespaces, File[] sourceDirectories, boolean compileDeclaredNamespaceOnly, int expectedSize) {
            this.namespaces = namespaces;
            this.sourceDirectories = sourceDirectories;
            this.compileDeclaredNamespaceOnly = compileDeclaredNamespaceOnly;
            this.expectedSize = expectedSize;
        }
    }

    @DataPoint
    public static NamespaceData ns1 = new NamespaceData(new String[]{"test.*"}, new File[]{new File("src/test/resources")}, true, 3);

    @DataPoint
    public static NamespaceData ns2 = new NamespaceData(new String[]{"!test\\..*"}, new File[]{new File("src/test/resources")}, false, 3);

    @DataPoint
    public static NamespaceData ns3 = new NamespaceData(new String[]{"test1"}, new File[]{new File("src/test/resources")}, true, 1);

    @DataPoint
    public static NamespaceData ns4 = new NamespaceData(new String[]{"test\\..*"}, new File[]{new File("src/test/resources")}, true, 1);

    @DataPoint
    public static NamespaceData ns5 = new NamespaceData(new String[]{"!test\\..*", "test.*"}, new File[]{new File("src/test/resources")}, true, 2);

    @DataPoint
    public static NamespaceData ns6 = new NamespaceData(new String[]{"!test\\..*", "test.*"}, new File[]{new File("src/test/resources"), new File("src/test/resources")}, true, 2);

    @Theory
    public void testNamespaceFiltering(NamespaceData ns) throws MojoExecutionException {

        NamespaceDiscovery namespaceDiscovery = new NamespaceDiscovery(mock(Log.class), ns.compileDeclaredNamespaceOnly);

        assertThat(namespaceDiscovery.discoverNamespacesIn(ns.namespaces, ns.sourceDirectories))
                .describedAs("Discovered Namespaces")
                .isNotNull()
                .isNotEmpty()
                .hasSize(ns.expectedSize);
    }

}
