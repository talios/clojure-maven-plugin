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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NamespaceDiscovery {

    private final Pattern nsPattern = Pattern.compile("^\\s*\\(ns(\\s.*|$)");
    private Log log;
    private boolean compileDeclaredNamespaceOnly;

    public NamespaceDiscovery(Log log, boolean compileDeclaredNamespaceOnly) {
        this.log = log;
        this.compileDeclaredNamespaceOnly = compileDeclaredNamespaceOnly;
    }

    /**
     * Discover namespaces in a list of source directories filtered by a list of namespace regexs
     *
     * @param namespaceFilterRegexs
     * @param paths
     * @return
     * @throws FileNotFoundException
     */
    public NamespaceInFile[] discoverNamespacesIn(String[] namespaceFilterRegexs, File... paths) throws MojoExecutionException {

        if (namespaceFilterRegexs == null || namespaceFilterRegexs.length == 0) {
            namespaceFilterRegexs = new String[]{".*"};
        }

        List<NamespaceInFile> namespaces = new ArrayList<NamespaceInFile>();

        for (NamespaceInFile namespace : discoverNamespacesInPath(paths)) {

            boolean toAdd = !compileDeclaredNamespaceOnly;
            for (String regex : namespaceFilterRegexs) {

                if (regex.startsWith("!")) {
                    // exclude regex
                    if (Pattern.compile("^" + regex.substring(1)).matcher(namespace.getName()).matches()) {
                        toAdd = false;
                        break;
                    }

                } else {
                    // include regex
                    if (Pattern.compile("^" + regex).matcher(namespace.getName()).matches()) {
                        toAdd = true;
                    }
                }
            }

            if (toAdd) {
                namespaces.add(namespace);
            } else if (log.isDebugEnabled()) {
                log.debug("Filtered namespace " + namespace.getName() + " from clojure build.");
            }
        }
        return namespaces.toArray(new NamespaceInFile[]{});

    }

    public List<NamespaceInFile> discoverNamespacesInPath(File... paths) throws MojoExecutionException {

        List<NamespaceInFile> namespaces = new ArrayList<NamespaceInFile>();
        for (File path : paths) {
            namespaces.addAll(discoverNamespacesIn(path, path));
        }
        return namespaces;
    }

    public List<NamespaceInFile> discoverNamespacesIn(File basePath, File scanPath) throws MojoExecutionException {

        List<NamespaceInFile> namespaces = new ArrayList<NamespaceInFile>();

        File[] files = scanPath.listFiles();
        if (files != null && files.length != 0) {
            for (File file : files) {
                if (!file.getName().startsWith(".")) {
                    log.debug("Searching " + file.getPath() + " for clojure namespaces");
                    if (file.isDirectory()) {
                        namespaces.addAll(discoverNamespacesIn(basePath, file));
                    } else if (file.getName().endsWith(".clj")) {
                        namespaces.addAll(findNamespaceInFile(basePath, file));
                    }
                }
            }
        }


        return namespaces;
    }

    private List<NamespaceInFile>
    findNamespaceInFile(File path, File file) throws MojoExecutionException {

        List<NamespaceInFile> namespaces = new ArrayList<NamespaceInFile>();

        Scanner scanner = null;
        try {
            scanner = new Scanner(file);

            scanner.useDelimiter("\n");

            while (scanner.hasNext()) {
                String line = scanner.next();

                Matcher matcher = nsPattern.matcher(line);

                if (matcher.find()) {
                    String ns = file.getPath();
                    ns = ns.substring(
                            path.getPath().length() + 1,
                            ns.length() - ".clj".length());
                    ns = ns.replace(File.separatorChar, '.');
                    ns = ns.replace('_', '-');

                    log.debug("Found namespace " + ns + " in file " + file.getPath());
                    namespaces.add(new NamespaceInFile(ns, file));
                }
            }
        } catch (FileNotFoundException e) {
            throw new MojoExecutionException(e.getMessage());
        }
        return namespaces;
    }
}
