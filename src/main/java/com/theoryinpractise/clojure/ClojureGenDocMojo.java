package com.theoryinpractise.clojure;

import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Plugin for Clojure source compiling.
 * <p/>
 * (C) Copyright Tim Dysinger   (tim -on- dysinger.net)
 * Mark Derricutt (mark -on- talios.com)
 * Dimitry Gashinsky (dimitry -on- gashinsky.com)
 * <p/>
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * @goal gendoc
 * @phase package
 * @requiresDependencyResolution test
 */
public class ClojureGenDocMojo extends AbstractClojureCompilerMojo {

    /**
     * Should we compile all namespaces or only those defined?
     *
     * @parameter defaut-value="false"
     */
    private boolean generateTestDocumentation;

    public void execute() throws MojoExecutionException {
        List<File> dirs = new ArrayList<File>();

        if (sourceDirectories != null) {
            dirs.addAll(Arrays.asList(sourceDirectories));
        }

        if (generateTestDocumentation) {
            if (testSourceDirectories != null) {
                dirs.addAll(Arrays.asList(testSourceDirectories));
            }
        }

        dirs.add(generatedSourceDirectory);

        File genDocClj;
        File docsDir;
        try {
            genDocClj = File.createTempFile("generate-docs", ".clj");
            if (!outputDirectory.getParentFile().exists()) {
                outputDirectory.getParentFile().mkdir();
            }
            docsDir = new File(outputDirectory.getParentFile(), "clojure");
            getLog().debug("Creating documentation directory " + docsDir.getPath());
            docsDir.mkdir();
            System.out.println(docsDir.getPath() + " exists " + docsDir.exists());
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("(use 'clojure.contrib.gen-html-docs)\n");
        sb.append("(generate-documentation-to-file \n");
        int count = 0;
        sb.append("  \"").append(docsDir.getPath()).append("/index.html\"\n");
        sb.append("  [");

        final String[] allNamespaces = new NamespaceDiscovery(getLog(), compileDeclaredNamespaceOnly).discoverNamespacesIn(namespaces, dirs.toArray(new File[]{}));
        for (String namespace : allNamespaces) {
            sb.append("'").append(namespace);
            if (count++ < allNamespaces.length - 1) {
                sb.append("\n   ");
            }
        }
        sb.append("])\n");
        try {
            final PrintWriter pw = new PrintWriter(genDocClj);
            pw.print(sb.toString());
            pw.close();
            getLog().info("Generating docs to " + docsDir.getCanonicalPath() + " with " + genDocClj.getPath());
            getLog().debug(sb.toString());
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage());
        }

        callClojureWith(dirs.toArray(new File[]{}), outputDirectory, classpathElements, "clojure.main", new String[]{genDocClj.getPath()});
    }

}