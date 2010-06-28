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

import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.exec.*;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.toolchain.Toolchain;
import org.apache.maven.toolchain.ToolchainManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public abstract class AbstractClojureCompilerMojo extends AbstractMojo {


    /**
     * The current toolchain maanager instance
     *
     * @component
     */
    private ToolchainManager toolchainManager;

    /**
     * The current build session instance. This is used for
     * toolchain manager API calls.
     *
     * @parameter expression="${session}"
     * @required
     * @readonly
     */
    private MavenSession session;

    /**
     * True to run Clojure by forking a new Java process, false to run
     * in-process with Maven.
     *
     * @parameter default-value="false"
     */
    protected boolean fork;

    /**
     * Base directory of the project.
     *
     * @parameter expression="${basedir}"
     */
    protected File baseDirectory;

    /**
     * Project classpath.
     *
     * @parameter default-value="${project.compileClasspathElements}"
     * @required
     * @readonly
     */
    protected List<String> classpathElements;

    /**
     * Project test classpath.
     *
     * @parameter default-value="${project.testClasspathElements}"
     * @required
     * @readonly
     */
    protected List<String> testClasspathElements;

    /**
     * Location of the file.
     *
     * @parameter default-value="${project.build.outputDirectory}"
     * @required
     */
    protected File outputDirectory;

    /**
     * Location of the file.
     *
     * @parameter default-value="${project.build.testOutputDirectory}"
     * @required
     */
    protected File testOutputDirectory;

    /**
     * Location of the source files.
     *
     * @parameter
     */
    private String[] sourceDirectories = new String[]{"src/main/clojure"};

    /**
     * Location of the source files.
     *
     * @parameter
     */
    private String[] testSourceDirectories = new String[]{"src/test/clojure"};

    /**
     * Location of the source files.
     *
     * @parameter default-value="${project.build.testSourceDirectory}"
     * @required
     */
    protected File baseTestSourceDirectory;

    /**
     * Location of the generated source files.
     *
     * @parameter default-value="${project.build.outputDirectory}/../generated-sources"
     * @required
     */
    protected File generatedSourceDirectory;

    /**
     * Should we compile all namespaces or only those defined?
     *
     * @parameter default-value="false"
     */
    protected boolean compileDeclaredNamespaceOnly;

    /**
     * A list of namespaces to compile
     *
     * @parameter
     */
    protected String[] namespaces;

    /**
     * Should we test all namespaces or only those defined?
     *
     * @parameter default-value="false"
     */
    protected boolean testDeclaredNamespaceOnly;

    /**
     * A list of test namespaces to compile
     *
     * @parameter
     */
    protected String[] testNamespaces;

    /**
     * Classes to put onto the command line before the main class
     *
     * @parameter
     */
    private List<String> prependClasses;

    /**
     * Clojure/Java command-line options
     *
     * @parameter
     */
    private String clojureOptions = "";

    /**
     * Run with test-classpath or compile-classpath?
     *
     * @parameter expression="${clojure.runwith.test}" default-value="true"
     */
    private boolean runWithTests;

    /**
     * A list of namespaces whose source files will be copied to the output.
     *
     * @parameter
     */
    protected String[] copiedNamespaces;

    /**
     * Should we copy the source of all namespaces or only those defined?
     *
     * @parameter default-value="false"
     */
    protected boolean copyDeclaredNamespaceOnly;

    /**
     * Should the source files of all compiled namespaces be copied to the output?
     * This overrides copiedNamespaces and copyDeclaredNamespaceOnly.
     *
     * @parameter default-value="false"
     */
    private boolean copyAllCompiledNamespaces;

    /**
     * Should reflective invocations in Clojure source emit warnings?  Corresponds with
     * the *warn-on-reflection* var and the clojure.compile.warn-on-reflection system property.
     *
     * @parameter default-value="false"
     */
    private boolean warnOnReflection;

    private String getJavaExecutable() throws MojoExecutionException {

        Toolchain tc = toolchainManager.getToolchainFromBuildContext("jdk", //NOI18N
                session);
        if (tc != null) {
            getLog().info("Toolchain in clojure-maven-plugin: " + tc);
            String foundExecutable = tc.findTool("java");
            if (foundExecutable != null) {
                return foundExecutable;
            } else {
                throw new MojoExecutionException("Unable to find 'java' executable for toolchain: " + tc);
            }
        }

        return "java";
    }

    private File[] translatePaths(String[] paths) {
        File[] files = new File[paths.length];
        for (int i = 0; i < paths.length; i++) {
            files[i] = new File(baseDirectory, paths[i]);
        }
        return files;
    }

    protected NamespaceInFile[] discoverNamespaces() throws MojoExecutionException {
        return new NamespaceDiscovery(getLog(), compileDeclaredNamespaceOnly).discoverNamespacesIn(namespaces, translatePaths(sourceDirectories));
    }

    protected NamespaceInFile[] discoverNamespacesToCopy() throws MojoExecutionException {
        if (copyAllCompiledNamespaces)
            return discoverNamespaces();
        else
            return new NamespaceDiscovery(getLog(), copyDeclaredNamespaceOnly).discoverNamespacesIn(copiedNamespaces, translatePaths(sourceDirectories));
    }

    public enum SourceDirectory {
        COMPILE, TEST
    }

    ;

    public File[] getSourceDirectories(SourceDirectory... sourceDirectoryTypes) {
        List<File> dirs = new ArrayList<File>();

        if (Arrays.asList(sourceDirectoryTypes).contains(SourceDirectory.COMPILE)) {
            dirs.add(generatedSourceDirectory);
            dirs.addAll(Arrays.asList(translatePaths(sourceDirectories)));
        }
        if (Arrays.asList(sourceDirectoryTypes).contains(SourceDirectory.TEST)) {
            dirs.add(baseTestSourceDirectory);
            dirs.addAll(Arrays.asList(translatePaths(testSourceDirectories)));
        }

        return dirs.toArray(new File[]{});

    }

    public List<String> getRunWithClasspathElements() {
        return runWithTests ? testClasspathElements : classpathElements;
    }

    protected void copyNamespaceSourceFilesToOutput(File outputDirectory, NamespaceInFile[] discoveredNamespaces) throws MojoExecutionException {
        for (NamespaceInFile ns : discoveredNamespaces) {
            File outputFile = new File(outputDirectory, ns.getFilename());
            outputFile.getParentFile().mkdirs();
            try {
                FileInputStream is = new FileInputStream(ns.getSourceFile());
                try {
                    FileOutputStream os = new FileOutputStream(outputFile);
                    try {
                        int amountRead;
                        byte[] buffer = new byte[4096];
                        while ((amountRead = is.read(buffer)) >= 0) {
                            os.write(buffer, 0, amountRead);
                        }
                        is.close();
                    } finally {
                        is.close();
                    }
                } finally {
                    is.close();
                }
            } catch (IOException ex) {
                throw new MojoExecutionException("Couldn't copy the clojure source files to the output", ex);
            }
        }
    }

    protected void callClojureWith(
            File[] sourceDirectory,
            File outputDirectory,
            List<String> compileClasspathElements,
            String mainClass,
            NamespaceInFile[] namespaceArgs) throws MojoExecutionException {
        String[] stringArgs = new String[namespaceArgs.length];
        for (int i = 0; i < namespaceArgs.length; i++) {
            stringArgs[i] = namespaceArgs[i].getName();
        }
        callClojureWith(sourceDirectory, outputDirectory, compileClasspathElements, mainClass, stringArgs);
    }
    
    protected void callClojureWith(File[] sourceDirectory,
				   File outputDirectory,
				   List<String> compileClasspathElements,
				   String mainClass,
				   String[] clojureArgs) throws MojoExecutionException {
	if (fork) {
	    forkClojureWith(sourceDirectory, outputDirectory, compileClasspathElements,
			    mainClass, clojureArgs);
	} else {
	    embedClojureWith(sourceDirectory, outputDirectory, compileClasspathElements,
			     mainClass, clojureArgs);
	}
    }

    protected ClassLoader getClassLoader(List<String> paths)
        throws MalformedURLException {

        URL[] urls = new URL[paths.size()];
        for (int i = 0; i < paths.size(); i++) {
            File file = new File(paths.get(i));
            urls[i] = file.toURL();
        }
        return new URLClassLoader(urls);
    }	

    private Collection<Thread> getActiveThreads(ThreadGroup threadGroup)
    {
        Thread[] threads = new Thread[threadGroup.activeCount()];
        int numThreads = threadGroup.enumerate(threads);
        Collection<Thread> result = new ArrayList<Thread>(numThreads);
        for (int i = 0; i < threads.length && threads[i] != null; i++)
        {
            result.add(threads[i]);
        }
        return result;
    }

    private void joinThreads(ThreadGroup threadGroup) {
        boolean found;
        do {
            found = false;
            Collection<Thread> threads = getActiveThreads(threadGroup);
            for (Iterator<Thread> iter = threads.iterator(); iter.hasNext(); ) {
                Thread thread = iter.next();
                try {
                    getLog().debug("Joining thread " + thread.toString());
                    thread.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    getLog().warn("Interrupted while waiting for " + thread.toString(), e);
                }

                found = true; // new threads may have been created
            }
        } while (found);
    }

    protected void embedClojureWith(File[] sourceDirectory,
				    File outputDirectory,
				    List<String> compileClasspathElements,
				    final String mainClassName,
				    final String[] clojureArgs) throws MojoExecutionException {
	try {
	    outputDirectory.mkdirs();

	    List<String> classpath = new ArrayList<String>();
	    classpath.addAll(compileClasspathElements);

	    for (File directory : sourceDirectory) {
		classpath.add(directory.getPath());
	    }

	    classpath.add(outputDirectory.getPath());

	    getLog().debug("Clojure classpath: " + classpath.toString());

	    System.setProperty("clojure.compile.path", outputDirectory.getPath());	

	    ClassLoader classloader = getClassLoader(classpath);

	    ThreadGroup threadGroup = new ThreadGroup("clojure");
	    Thread thread = new Thread(threadGroup, new Runnable() {
		    public void run() {
			try {
			    Class mainClass =
				Thread.currentThread()
				.getContextClassLoader()
				.loadClass(mainClassName);
			    Method mainMethod = mainClass.getMethod("main", new Class[]{String[].class});
			    if (!mainMethod.isAccessible()) {
				getLog().debug( "Setting accessibility true to invoke main()." );
				mainMethod.setAccessible( true );
			    }
			    getLog().debug("Invoking main");
			    mainMethod.invoke(null, new Object[]{clojureArgs});
			} catch (NoSuchMethodException e) {
			    Thread.currentThread()
				.getThreadGroup()
				.uncaughtException(Thread.currentThread(),
						   new Exception("Missing main method with appropriate signature.", e));
			
			} catch (Exception e) {
			    Thread.currentThread()
				.getThreadGroup()
				.uncaughtException(Thread.currentThread(), e);
			}
		    }
		});
	    thread.setContextClassLoader(classloader);
	    thread.start();
	    joinThreads(threadGroup);
	} catch (Exception e) {
            throw new MojoExecutionException("Running embedded Clojure failed", e);
	}	    
    }
    
    protected void forkClojureWith(
            File[] sourceDirectory,
            File outputDirectory,
            List<String> compileClasspathElements,
            String mainClass,
            String[] clojureArgs) throws MojoExecutionException {

        outputDirectory.mkdirs();

        String cp = "";
        for (File directory : sourceDirectory) {
            cp = cp + directory.getPath() + File.pathSeparator;
        }

        cp = cp + outputDirectory.getPath() + File.pathSeparator;

        for (Object classpathElement : compileClasspathElements) {
            cp = cp + File.pathSeparator + classpathElement;
        }

        final String javaExecutable = getJavaExecutable();
        getLog().debug("Java exectuable used:  " + javaExecutable);
        getLog().debug("Clojure classpath: " + cp);
        CommandLine cl = new CommandLine(javaExecutable);

        cl.addArgument("-cp");
        cl.addArgument(cp);
        cl.addArgument("-Dclojure.compile.path=" + outputDirectory.getPath() + "");

        if (warnOnReflection) cl.addArgument("-Dclojure.compile.warn-on-reflection=true");

        cl.addArguments(clojureOptions, false);

        if (prependClasses != null) {
            cl.addArguments(prependClasses.toArray(new String[prependClasses.size()]));
        }

        cl.addArgument(mainClass);

        if (clojureArgs != null) {
            cl.addArguments(clojureArgs, false);
        }

        Executor exec = new DefaultExecutor();
        Map<String, String> env = new HashMap<String, String>(System.getenv());
//        env.put("path", ";");
//        env.put("path", System.getProperty("java.home"));

        ExecuteStreamHandler handler = new PumpStreamHandler(System.out, System.err, System.in);
        exec.setStreamHandler(handler);

        int status;
        try {
            status = exec.execute(cl, env);
        } catch (ExecuteException e) {
            status = e.getExitValue();
        } catch (IOException e) {
            status = 1;
        }

        if (status != 0) {
            throw new MojoExecutionException("Clojure failed.");
        }

    }

}
