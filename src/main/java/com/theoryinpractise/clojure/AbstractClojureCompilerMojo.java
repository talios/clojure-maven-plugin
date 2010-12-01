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

import org.apache.commons.exec.*;
import org.apache.commons.lang.SystemUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.toolchain.Toolchain;
import org.apache.maven.toolchain.ToolchainManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
     * Base directory of the project.
     *
     * @parameter expression="${basedir}"
     * @required
     * @readonly
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
    protected String[] sourceDirectories = new String[]{"src/main/clojure"};

    /**
     * Location of the source files.
     *
     * @parameter
     */
    protected String[] testSourceDirectories = new String[]{"src/test/clojure"};

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
     * Working directory for forked java clojure process.
     *
     * @parameter
     */
    protected File workingDirectory;

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
     * @parameter expression="${clojure.options}"
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

    /**
     * Specify additional vmargs to use when running clojure or swank.
     *
     * @parameter expression="${clojure.vmargs}"
     */
    private String vmargs;

    /**
     * Escapes the given file path so that it's safe for inclusion in a
     * Clojure string literal.
     *
     * @param directory directory path
     * @param file      file name
     * @return escaped file path, ready for inclusion in a string literal
     */
    protected String escapeFilePath(String directory, String file) {
        return escapeFilePath(new File(directory, file));
    }

    /**
     * Escapes the given file path so that it's safe for inclusion in a
     * Clojure string literal.
     *
     * @param file
     * @return escaped file path, ready for inclusion in a string literal
     */
    protected String escapeFilePath(final File file) {
        // TODO: Should handle also possible newlines, etc.
        return file.getPath().replace("\\", "\\\\");
    }

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

    protected File getWorkingDirectory() throws MojoExecutionException {
        if (workingDirectory != null) {
            if (workingDirectory.exists()) {
                return workingDirectory;
            } else {
                throw new MojoExecutionException("Directory specified in <workingDirectory/> does not exists: " + workingDirectory.getPath());
            }
        } else {
            return session.getCurrentProject().getBasedir();
        }
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

    public String getSourcePath(SourceDirectory... sourceDirectoryTypes) {
        return getPath(getSourceDirectories(sourceDirectoryTypes));
    }

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

    private String getPath(File[] sourceDirectory) {
        String cp = "";
        for (File directory : sourceDirectory) {
            cp = cp + directory.getPath() + File.pathSeparator;
        }
        return cp.substring(0, cp.length() - 1);
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
        callClojureWith(ExecutionMode.BATCH, sourceDirectory, outputDirectory, compileClasspathElements, mainClass, namespaceArgs);
    }

    protected void callClojureWith(
            File[] sourceDirectory,
            File outputDirectory,
            List<String> compileClasspathElements,
            String mainClass,
            String[] clojureArgs) throws MojoExecutionException {
        callClojureWith(ExecutionMode.BATCH, sourceDirectory, outputDirectory, compileClasspathElements, mainClass, clojureArgs);
    }

    protected void callClojureWith(
            ExecutionMode executionMode,
            File[] sourceDirectory,
            File outputDirectory,
            List<String> compileClasspathElements,
            String mainClass,
            NamespaceInFile[] namespaceArgs) throws MojoExecutionException {
        String[] stringArgs = new String[namespaceArgs.length];
        for (int i = 0; i < namespaceArgs.length; i++) {
            stringArgs[i] = namespaceArgs[i].getName();
        }
        callClojureWith(executionMode, sourceDirectory, outputDirectory, compileClasspathElements, mainClass, stringArgs);
    }

    protected void callClojureWith(
            ExecutionMode executionMode,
            File[] sourceDirectory,
            File outputDirectory,
            List<String> compileClasspathElements,
            String mainClass,
            String[] clojureArgs) throws MojoExecutionException {

        outputDirectory.mkdirs();

        String cp = getPath(sourceDirectory);

        cp = cp + File.pathSeparator + outputDirectory.getPath() + File.pathSeparator;

        for (Object classpathElement : compileClasspathElements) {
            cp = cp + File.pathSeparator + classpathElement;
        }

        cp = cp.replaceAll("\\s", "\\ ");

        final String javaExecutable = getJavaExecutable();
        getLog().debug("Java exectuable used:  " + javaExecutable);
        getLog().debug("Clojure classpath: " + cp);
        CommandLine cl = null;

        if (ExecutionMode.INTERACTIVE == executionMode && SystemUtils.IS_OS_WINDOWS) {
            cl = new CommandLine("cmd");
            cl.addArgument("/c");
            cl.addArgument("start");
            cl.addArgument(javaExecutable);
        } else {
            cl = new CommandLine(javaExecutable);
        }

        if (vmargs != null) {
            cl.addArguments(vmargs, false);
        }

        cl.addArgument("-cp");
        cl.addArgument(cp, false);
        cl.addArgument("-Dclojure.compile.path=" + escapeFilePath(outputDirectory), false);

        if (warnOnReflection) cl.addArgument("-Dclojure.compile.warn-on-reflection=true");

        cl.addArguments(clojureOptions, false);

        if (prependClasses != null) {
            cl.addArguments(prependClasses.toArray(new String[prependClasses.size()]));
        }

        cl.addArgument(mainClass);

        if (clojureArgs != null) {
            cl.addArguments(clojureArgs, false);
        }

        getLog().debug("Command line: " + cl.toString());

        Executor exec = new DefaultExecutor();
        Map<String, String> env = new HashMap<String, String>(System.getenv());
//        env.put("path", ";");
//        env.put("path", System.getProperty("java.home"));

        ExecuteStreamHandler handler = new PumpStreamHandler(System.out, System.err, System.in);
        exec.setStreamHandler(handler);
        exec.setWorkingDirectory(getWorkingDirectory());

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
