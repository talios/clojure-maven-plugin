/*
 * Created by IntelliJ IDEA.
 * User: amrk
 * Date: Apr 18, 2009
 * Time: 1:08:16 PM
 */
package clojure;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public abstract class AbstractClojureCompilerMojo extends AbstractMojo {

    protected void callClojureWith(
            File sourceDirectory,
            File outputDirectory,
            List<String> compileClasspathElements,
            String mainClass,
            String[] clojureArgs) throws MojoExecutionException {

        outputDirectory.mkdirs();

        String cp = sourceDirectory.getPath() + File.pathSeparator + outputDirectory.getPath();

        for (Object classpathElement : compileClasspathElements) {
            cp = cp + File.pathSeparator + classpathElement;
        }

        getLog().debug("Clojure classpath: " + cp);
        List<String> args = new ArrayList<String>();
        args.add("java");
        args.add("-cp");
        args.add(cp);
        args.add("-Dclojure.compile.path=" + outputDirectory.getPath() + "");
        args.add(mainClass);
        for (String arg : clojureArgs) {
            args.add(arg);
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
                throw new MojoExecutionException("Clojure failed.");
            }

        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage());
        }

    }
}
