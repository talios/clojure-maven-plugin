package clojure;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

/**
 * Plugin for Clojure source compiling.
 *
 * (C) Copyright Tim Dysinger   (tim -on- dysinger.net)
 *               Mark Derricutt (mark -on- talios.com)
 *
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * @goal compile
 * @phase compile
 */
public class ClojureCompilerMojo extends AbstractMojo {
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
    private File sourceDirectory;

    /**
     * Project classpath.
     *
     * @parameter default-value="${project.compileClasspathElements}"
     * @required
     * @readonly
     */
    private List<String> compileClasspathElements;

    /**
     * A list of namespaces to compile
     *
     * @parameter
     * @required
     */
    private String[] namespaces;

    public void execute() throws MojoExecutionException {

        outputDirectory.mkdirs();

        String cp = sourceDirectory.getPath() + File.pathSeparator
            + outputDirectory.getPath();

        for (Object classpathElement : compileClasspathElements) {
            cp = cp + File.pathSeparator + classpathElement;
        }

        List<String> args = new ArrayList<String>();
        args.add("java");
        args.add("-cp");
        args.add(cp);
        args.add("-Dclojure.compile.path=" + outputDirectory.getPath() + "");
        args.add("clojure.lang.Compile");
        for (String namespace : namespaces) {
            args.add(namespace);
        }

        ProcessBuilder pb = new ProcessBuilder(args);
        pb.environment().put("path", ";");
        pb.environment().put("path", System.getProperty("java.home"));

        pb.redirectErrorStream(true);
        try {
            Process process = pb.start();
            new OutputHandler(process, getLog()).start();

            int status;
            try {
                status = process.waitFor();
            } catch (InterruptedException e) {
                status = process.exitValue();
            }

            if (status != 0) {
                throw new MojoExecutionException("Clojure compilation failed.");
            }

        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage());
        }

    }

    class OutputHandler extends Thread {

    	private Process process;

    	public OutputHandler(Process process, Log log) {
            this.process = process;
    	}

    	@Override
            public void run() {
            try {
                InputStreamReader tempReader = new InputStreamReader
                    (new BufferedInputStream(process.getInputStream()));
                while (true) {
                    int line = tempReader.read();
                    if (line == -1)
                        break;
                    System.out.write(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
    	}
    }
}
