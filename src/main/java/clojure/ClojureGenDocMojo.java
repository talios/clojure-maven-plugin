package clojure;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

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
 * @requiresDependencyResolution compile
 */
public class ClojureGenDocMojo extends AbstractClojureCompilerMojo {
    /**
     * Location of the file.
     *
     * @parameter default-value="${project.build.outputDirectory}"
     * @required
     */
    private File outputDirectory;

    /**
     * Location of the source files.
     *
     * @parameter default-value="${project.build.sourceDirectory}"
     * @required
     */
    private File baseSourceDirectory;

    /**
     * Location of the source files.
     *
     * @parameter
     */
    private File[] sourceDirectory;

    /**
     * Location of the generated source files.
     *
     * @parameter default-value="${project.build.outputDirectory}/../generated-sources"
     * @required
     */
    private File generatedSourceDirectory;

    /**
     * Project classpath.
     *
     * @parameter default-value="${project.compileClasspathElements}"
     * @required
     * @readonly
     */
    private List<String> classpathElements;

    /**
     * A list of namespaces to compile
     *
     * @parameter
     * @required
     */
    private String[] namespaces;

    public void execute() throws MojoExecutionException {
        List<File> dirs = new ArrayList<File>();
        dirs.add(baseSourceDirectory);
        if (sourceDirectory != null) {
            dirs.addAll(Arrays.asList(sourceDirectory));
        }
        dirs.add(generatedSourceDirectory);

        File genDocClj;
        File docsDir;
        try {
            genDocClj = File.createTempFile("generate-docs", ".clj");
            docsDir = new File(outputDirectory.getPath() + "/../clojure");
            docsDir.mkdir();
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage());
        }


        StringBuilder sb = new StringBuilder();
        sb.append("(use 'clojure.contrib.gen-html-docs)\n");
        sb.append("(generate-documentation-to-file \n");
        int count = 0;
        sb.append("  \"").append(docsDir.getPath()).append("/index.html\"\n");
        sb.append("  [");
        for (String namespace : namespaces) {
            sb.append("   '").append(namespace);
            if (count++ < namespaces.length) {
                sb.append("\n   ");
            }
        }
        sb.append("])\n");
        try {
            System.out.println(sb.toString());
            final PrintWriter pw = new PrintWriter(genDocClj);
            pw.print(sb.toString());
            pw.close();
            getLog().info("Generating docs to " + docsDir.getCanonicalPath() + " with " + genDocClj.getPath());
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage());
        }

        callClojureWith(dirs.toArray(new File[]{}), outputDirectory, classpathElements, "clojure.main", new String[]{genDocClj.getPath()});
    }

}