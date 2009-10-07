package com.theoryinpractise.clojure;

import java.io.File;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.List;
import java.util.regex.*;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Mojo to start a clojure repl
 * <p/>
 * (C) Copyright Tim Dysinger   (tim -on- dysinger.net)
 * Mark Derricutt (mark -on- talios.com)
 * Dimitry Gashinsky (dimitry -on- gashinsky.com)
 * Scott Fleckenstein (nullstyle -on- gmail.com)
 * <p/>
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * @goal repl
 * @execute phase="compile"
 * @requiresDependencyResolution compile
 */
public class ClojureReplMojo extends AbstractClojureCompilerMojo {
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
     * @parameter
     */
    private File[] sourceDirectories = new File[] {new File("src/main/clojure")};

    /**
     * Location of the test source files.
     *
     * @parameter
     */
    private File[] testSourceDirectories = new File[] {new File("src/test/clojure")};

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
     * The clojure script to preceding the switch to the repl
     *
     * @parameter
     */
    private String replScript;

	private static final Pattern JLINE = Pattern.compile("^.*/jline-[^/]+.jar$");

	boolean isJLineAvailable(List<String> elements) {
		if(elements != null) {
			for(String e: elements) {
				Matcher m = JLINE.matcher(e);
				if(m.matches()) 
					return true;
			}
		}
		return false;
	}

    public void execute() throws MojoExecutionException {

        List<File> dirs = new ArrayList<File>();
        if (sourceDirectories != null) {
            dirs.addAll(Arrays.asList(sourceDirectories));
        }
        if (testSourceDirectories != null) {
            dirs.addAll(Arrays.asList(testSourceDirectories));
        }
        dirs.add(generatedSourceDirectory);

		List<String> args = new ArrayList<String>();
		String mainClass = "clojure.main";

		if (isJLineAvailable(classpathElements)) {
			getLog().info("Enabling JLine support");
		    args.add("clojure.main");
			mainClass = "jline.ConsoleRunner";
		} 

        if (replScript != null && new File(replScript).exists()) {
			args.add(replScript);
        }

        callClojureWith(dirs.toArray(new File[]{}), outputDirectory, classpathElements, mainClass, 
				args.toArray(new String[args.size()]));
    }

}
