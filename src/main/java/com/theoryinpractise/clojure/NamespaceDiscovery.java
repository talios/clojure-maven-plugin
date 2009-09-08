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

    private final Pattern nsPattern = Pattern.compile("^\\s*\\(ns.*");
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
    public String[] discoverNamespacesIn(String[] namespaceFilterRegexs, File... paths) throws MojoExecutionException {

        if (namespaceFilterRegexs == null || namespaceFilterRegexs.length == 0) {
            namespaceFilterRegexs = new String[]{".*"};
        }

        List<String> namespaces = new ArrayList<String>();

        for (String namespace : discoverNamespacesInPath(paths)) {

            boolean toAdd = !compileDeclaredNamespaceOnly;
            for (String regex : namespaceFilterRegexs) {

                if (regex.startsWith("!")) {
                    // exclude regex
                    if (Pattern.compile("^" + regex.substring(1)).matcher(namespace).matches()) {
                        toAdd = false;
                        break;
                    }

                } else {
                    // include regex
                    if (Pattern.compile("^" + regex).matcher(namespace).matches()) {
                        toAdd = true;
                    }
                }
            }

            if (toAdd) {
                namespaces.add(namespace);
            } else if (log.isDebugEnabled()) {
                log.debug("Filtered namespace " + namespace + " from clojure build.");
            }
        }
        return namespaces.toArray(new String[]{});

    }

    public List<String> discoverNamespacesInPath(File... paths) throws MojoExecutionException {

        List<String> namespaces = new ArrayList<String>();
        for (File path : paths) {
            namespaces.addAll(discoverNamespacesIn(path, path));
        }
        return namespaces;
    }

    public List<String> discoverNamespacesIn(File basePath, File scanPath) throws MojoExecutionException {

        List<String> namespaces = new ArrayList<String>();

        File[] files = scanPath.listFiles();
        if (files != null && files.length != 0) {
            for (File file : files) {
                log.debug("Searching " + file.getPath() + " for clojure namespaces");
                if (file.isDirectory()) {
                    namespaces.addAll(discoverNamespacesIn(basePath, file));
                } else if (file.getName().endsWith(".clj")) {
                    namespaces.addAll(findNamespaceInFile(basePath, file));
                }
            }
        }


        return namespaces;
    }

    private List<String> findNamespaceInFile(File path, File file) throws MojoExecutionException {

        List<String> namespaces = new ArrayList<String>();

        Scanner scanner = null;
        try {
            scanner = new Scanner(file);

            scanner.useDelimiter("\n");

            while (scanner.hasNext()) {
                String line = scanner.next();

                Matcher matcher = nsPattern.matcher(line);

                if (matcher.find()) {
                    String ns = file.getPath().substring(path.getPath().length() + 1, file.getPath().length() - 4).replaceAll("/", ".").replaceAll("_", "-");


                    log.debug("Found namespace " + ns + " in file " + file.getPath());
                    namespaces.add(ns);
                }
            }
        } catch (FileNotFoundException e) {
            throw new MojoExecutionException(e.getMessage());
        }
        return namespaces;
    }
}
