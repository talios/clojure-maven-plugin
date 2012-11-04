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

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang.SystemUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.toolchain.Toolchain;
import org.apache.maven.toolchain.ToolchainManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

public abstract class AbstractClojureCompilerMojo extends AbstractMojo {

    @Parameter(required = true, readonly = true, property = "project")
    protected MavenProject project;

    @Component
    private ToolchainManager toolchainManager;

    @Parameter(required = true, readonly = true, property = "session")
    private MavenSession session;

    @Parameter(required = true, readonly = true, property = "basedir")
    protected File baseDirectory;

    @Parameter(required = true, readonly = true, property = "project.compileClasspathElements")
    protected List<String> classpathElements;

    @Parameter(required = true, readonly = true, property = "project.testClasspathElements")
    protected List<String> testClasspathElements;

    @Parameter(required = true, property = "plugin.artifacts")
    private java.util.List<Artifact> pluginArtifacts;

    @Parameter(required = true, defaultValue = "${project.build.outputDirectory}")
    protected File outputDirectory;

    @Parameter(required = true, defaultValue = "${project.build.testOutputDirectory}")
    protected File testOutputDirectory;

    /**
     * Location of the source files.
     */
    @Parameter
    protected String[] sourceDirectories = new String[]{"src/main/clojure"};

    /**
     * Location of the source files.
     */
    @Parameter
    protected String[] testSourceDirectories = new String[]{"src/test/clojure"};

    /**
     * Location of the source files.
     */
    @Parameter(required = true, defaultValue = "${project.build.testSourceDirectory}")
    protected File baseTestSourceDirectory;

    /**
     * Location of the generated source files.
     */
    @Parameter(required = true, defaultValue = "${project.build.outputDirectory}/../generated-sources")
    protected File generatedSourceDirectory;

    /**
     * Working directory for forked java clojure process.
     */
    @Parameter
    protected File workingDirectory;

    /**
     * Should we compile all namespaces or only those defined?
     */
    @Parameter(defaultValue = "false")
    protected boolean compileDeclaredNamespaceOnly;

    /**
     * A list of namespaces to compile
     */
    @Parameter
    protected String[] namespaces;

    /**
     * Should we test all namespaces or only those defined?
     */
    @Parameter(defaultValue = "false")
    protected boolean testDeclaredNamespaceOnly;

    /**
     * A list of test namespaces to compile
     */
    @Parameter
    protected String[] testNamespaces;

    /**
     * Classes to put onto the command line before the main class
     */
    @Parameter
    private List<String> prependClasses;

    /**
     * Clojure/Java command-line options
     */
    @Parameter(property = "clojure.options")
    private String clojureOptions = "";

    /**
     * Run with test-classpath or compile-classpath?
     */
    @Parameter(property = "clojure.runwith.test", defaultValue = "true")
    private boolean runWithTests;

    /**
     * Include plugin dependencies in classpath?
     */
    @Parameter(defaultValue = "false")
    private boolean includePluginDependencies;

    /**
     * A list of namespaces whose source files will be copied to the output.
     */
    @Parameter
    protected String[] copiedNamespaces;

    /**
     * Should we copy the source of all namespaces or only those defined?
     */
    @Parameter(defaultValue = "false")
    protected boolean copyDeclaredNamespaceOnly;

    /**
     * Should the source files of all compiled namespaces be copied to the output?
     * This overrides copiedNamespaces and copyDeclaredNamespaceOnly.
     */
    @Parameter(defaultValue = "false")
    private boolean copyAllCompiledNamespaces;

    /**
     * Should reflective invocations in Clojure source emit warnings?  Corresponds with
     * the *warn-on-reflection* var and the clojure.compile.warn-on-reflection system property.
     */
    @Parameter(defaultValue = "false")
    private boolean warnOnReflection;

    /**
     * Specify additional vmargs to use when running clojure or swank.
     */
    @Parameter(property = "clojure.vmargs")
    private String vmargs;


    /**
     * Spawn a new console window for interactive clojure sessions on Windows
     */
    @Parameter(defaultValue = "true")
    private boolean spawnInteractiveConsoleOnWindows;

    /**
     * Which Windows command to use when starting the REPL
     */
    @Parameter(defaultValue = "cmd /c start")
    private String windowsConsole;

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
        return new NamespaceDiscovery(getLog(), outputDirectory, compileDeclaredNamespaceOnly, false).discoverNamespacesIn(namespaces, translatePaths(sourceDirectories));
    }

    protected NamespaceInFile[] discoverNamespacesToCopy() throws MojoExecutionException {
        if (copyAllCompiledNamespaces)
            return discoverNamespaces();
        else
            return new NamespaceDiscovery(getLog(), outputDirectory, copyDeclaredNamespaceOnly, false).discoverNamespacesIn(copiedNamespaces, translatePaths(sourceDirectories));
    }

    public enum SourceDirectory {
        COMPILE, TEST
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

    public List<String> getRunWithClasspathElements() {
        Set<String> classPathElements = new HashSet<String>();
        if (includePluginDependencies) {
            for (Artifact artifact : pluginArtifacts) {
                classPathElements.add(artifact.getFile().getPath());
            }
        }
        classPathElements.addAll(runWithTests ? testClasspathElements : classPathElements);

        return new ArrayList<String>(classPathElements);
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

        String classpath = manifestClasspath(sourceDirectory, outputDirectory, compileClasspathElements);

        final String javaExecutable = getJavaExecutable();
        getLog().debug("Java exectuable used:  " + javaExecutable);
        getLog().debug("Clojure manifest classpath: " + classpath);
        CommandLine cl = null;

        if (ExecutionMode.INTERACTIVE == executionMode && SystemUtils.IS_OS_WINDOWS && spawnInteractiveConsoleOnWindows) {
            Scanner sc = new Scanner(windowsConsole);
            Pattern pattern = Pattern.compile("\"[^\"]*\"|'[^']*'|[\\w'/]+");
            cl = new CommandLine(sc.findInLine(pattern));
            String param;
            while ((param = sc.findInLine(pattern)) != null) {
                cl.addArgument(param);
            }
            cl.addArgument(javaExecutable);
        } else {
            cl = new CommandLine(javaExecutable);
        }

        if (vmargs != null) {
            cl.addArguments(vmargs, false);
        }

        cl.addArgument("-Dclojure.compile.path=" + escapeFilePath(outputDirectory), false);

        if (warnOnReflection) cl.addArgument("-Dclojure.compile.warn-on-reflection=true");

        cl.addArguments(clojureOptions, false);

        cl.addArgument("-jar");
	File jar;
        if (prependClasses != null && prependClasses.size() > 0) {
            jar = createJar(classpath, prependClasses.get(0));
            cl.addArgument(jar.getAbsolutePath(), false);
	    List<String> allButFirst = prependClasses.subList(1, prependClasses.size());
            cl.addArguments(allButFirst.toArray(new String[allButFirst.size()]));
	    cl.addArgument(mainClass);
        } else {
            jar = createJar(classpath, mainClass);
            cl.addArgument(jar.getAbsolutePath(), false);
	}


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

    private String manifestClasspath(final File[] sourceDirectory, final File outputDirectory,
                                     final List<String> compileClasspathElements) {
        String cp = getPath(sourceDirectory);

        cp = cp + outputDirectory.toURI() + " ";

        for (String classpathElement : compileClasspathElements) {
            cp = cp + new File(classpathElement).toURI() + " ";
        }

        cp = cp.replaceAll("\\s+", "\\ ");
        return cp;
    }

    private String getPath(File[] sourceDirectory) {
        String cp = "";
        for (File directory : sourceDirectory) {
            cp = cp + directory.toURI() + " ";
        }
        return cp;
    }

    private File createJar(final String cp, final String mainClass) {
        try {
            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
            manifest.getMainAttributes().put(Attributes.Name.CLASS_PATH, cp);
            manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, mainClass);
            File tempFile = File.createTempFile("clojuremavenplugin", "jar");
            tempFile.deleteOnExit();
            JarOutputStream target = new JarOutputStream(new FileOutputStream(tempFile), manifest);
            target.close();
            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
