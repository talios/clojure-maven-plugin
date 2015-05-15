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

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "compile", defaultPhase = LifecyclePhase.COMPILE, requiresDependencyResolution = ResolutionScope.COMPILE)
public class ClojureCompilerMojo extends AbstractClojureCompilerMojo {

    /**
     * Should the compile phase create a temporary output directory for .class files?
     */
    @Parameter(required = true, defaultValue = "false")
    protected Boolean temporaryOutputDirectory;

    /**
     * Should the compile phase clean unwanted aot classes?
     */
    @Parameter(required = false, defaultValue = "false")
    protected Boolean cleanAOTNamespaces;

    public void execute() throws MojoExecutionException {

        File outputPath = (temporaryOutputDirectory)
                          ? createTemporaryDirectory("classes")
                          : outputDirectory;

        getLog().debug("Compiling clojure sources to " + outputPath.getPath());

    	  callClojureWith(
                getSourceDirectories(SourceDirectory.COMPILE),
                outputPath, classpathElements, "clojure.lang.Compile",
                discoverNamespaces());

    	if (!temporaryOutputDirectory && cleanAOTNamespaces) {
    		cleanAOTNamespaces(outputDirectory, namespaces);
    	}

        copyNamespaceSourceFilesToOutput(outputDirectory, discoverNamespacesToCopy());
    }

    private void cleanAOTNamespaces(final File outputDirectory, String[] namespaces) {

    	getLog().debug("Cleaning output directory " + outputDirectory.getPath() + " of undesired AOT classes");

        final String[] namespaceFilterRegexs =
        		(namespaces == null || namespaces.length == 0)
        		    ? new String[]{".*"}
                    : namespaces;


        IFileProcessor fileCleaner = new IFileProcessor() {
			public boolean doFile(File file) {
				//getLog().debug("considering cleaning " + file.getPath());
				String classFilePath = file.getPath().substring(outputDirectory.getPath().length() + 1); // + 1 to remove the leading '/'
				//getLog().debug("classname " + classname);
				// only interested in class files
				if (!classFilePath.endsWith(".class")) return false;
				classFilePath = classFilePath.substring(0, classFilePath.lastIndexOf(".class"));
				//getLog().debug("classname " + classname);

				// get the part of the filename related to the namespace, or return
				if (classFilePath.endsWith("__init")) {
					classFilePath = classFilePath.substring(0, classFilePath.lastIndexOf("__init"));
				} else if (classFilePath.contains("$")) {
					classFilePath = classFilePath.substring(0, classFilePath.indexOf("$"));
				} else {
					return false;
				}
				//getLog().debug("munged namespace " + classname);

				final String namespace =  demunge(classFilePath);
				//getLog().debug("demunged namespace " + namespace);

	            boolean toRemove = compileDeclaredNamespaceOnly;
	            for (String regex : namespaceFilterRegexs) {

	                if (regex.startsWith("!")) {
	                    // exclude regex
	                    if (Pattern.compile("^" + regex.substring(1)).matcher(namespace).matches()) {
	                        toRemove = true;
	                        break;
	                    }

	                } else {
	                    // include regex
	                    if (Pattern.compile("^" + regex).matcher(namespace).matches()) {
	                        toRemove = true;
	                    }
	                }
	            }

	            if (toRemove) {
	            	file.delete();
		            if (getLog().isDebugEnabled()) {
		            	getLog().debug("Namespace " + namespace + " must not be compiled according to pom regexes. Removing file " + file.getPath());
		            }
		            return true;
	            } else {
	            	return false;
	            }

			}

        };

        long processedFiles = recurseDirectoryFiles(outputDirectory, fileCleaner, true);
        getLog().info(processedFiles + " AOT classes have been cleaned up after compilation");
    }

    private static interface IFileProcessor {
    	boolean doFile(File file);
    }

    private long recurseDirectoryFiles(File d, IFileProcessor fp, boolean deleteEmptyDirs) {
    	long processedFiles = 0;
    	for (File f: d.listFiles()) {
    		if (f.isDirectory() && f.canRead()) {
    			processedFiles += recurseDirectoryFiles(f, fp, deleteEmptyDirs);
    			if (deleteEmptyDirs && f.listFiles().length==0) {
    				f.delete();
    			}
    		} else {
    			if (fp.doFile(f)) {
    				processedFiles++;
    			}
    		}
    	}
    	return processedFiles;
    }

    //////////////////////////////////////////////////////////////////////
    // Code adapted from clojure.lang.Compiler
    public String demunge(String mungedName){
    	StringBuilder sb = new StringBuilder();
    	Matcher m = DEMUNGE_PATTERN.matcher(mungedName);
    	int lastMatchEnd = 0;
    	while (m.find())
    		{
    		int start = m.start();
    		int end = m.end();
    		// Keep everything before the match
    		sb.append(mungedName.substring(lastMatchEnd, start));
    		lastMatchEnd = end;
    		// Replace the match with DEMUNGE_MAP result
    		Character origCh = DEMUNGE_MAP.get(m.group());
    		sb.append(origCh);
    		}
    	// Keep everything after the last match
    	sb.append(mungedName.substring(lastMatchEnd));
    	String result = sb.toString();
    	if (getLog().isDebugEnabled()) {
    		getLog().debug("demunged " + mungedName + " into " + result);
    	}
    	return result;
    }

    @SuppressWarnings("serial")
	public static final Map<String, Character> DEMUNGE_MAP = new HashMap<String, Character>() {
    	{
    		put("/", '.');
    		put("$", '/');
        	put("_COLON_", ':');
        	put("_PLUS_", '+');
        	put("_GT_", '>');
        	put("_LT_", '<');
        	put("_EQ_", '=');
        	put("_TILDE_", '~');
        	put("_BANG_", '!');
        	put("_CIRCA_", '@');
        	put("_SHARP_", '#');
        	put("_SINGLEQUOTE_", '\'');
        	put("_DOUBLEQUOTE_", '"');
        	put("_PERCENT_", '%');
        	put("_CARET_", '^');
        	put("_AMPERSAND_", '&');
        	put("_STAR_", '*');
        	put("_BAR_", '|');
        	put("_LBRACE_", '{');
        	put("_RBRACE_", '}');
        	put("_LBRACK_", '[');
        	put("_RBRACK_", ']');
        	put("_SLASH_", '/');
        	put("_BSLASH_", '\\');
        	put("_QMARK_", '?');
    	}
    };
    public static final Pattern DEMUNGE_PATTERN;
    static {
    	// DEMUNGE_PATTERN searches for the first of any occurrence of
    	// the strings that are keys of DEMUNGE_MAP.
    	// Note: Regex matching rules mean that #"_|_COLON_" "_COLON_"
           // returns "_", but #"_COLON_|_" "_COLON_" returns "_COLON_"
           // as desired.  Sorting string keys of DEMUNGE_MAP from longest to
           // shortest ensures correct matching behavior, even if some strings are
    	// prefixes of others.
    	String[] mungeStrs = DEMUNGE_MAP.keySet().toArray(new String[DEMUNGE_MAP.keySet().size()]);
    	Arrays.sort(mungeStrs, new Comparator<String>() {
                    public int compare(String s1, String s2) {
                        return s2.length() - s1.length();
                    }});
    	StringBuilder sb = new StringBuilder();
    	boolean first = true;
    	for(Object s : mungeStrs)
    		{
    		String escapeStr = (String) s;
    		if (!first)
    			sb.append("|");
    		first = false;
    		sb.append("\\Q");
    		sb.append(escapeStr);
    		sb.append("\\E");
    		}
    	DEMUNGE_PATTERN = Pattern.compile(sb.toString());
    }

}
