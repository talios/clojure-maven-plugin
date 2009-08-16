package clojure;

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

    private final Pattern nsPattern = Pattern.compile("^\\s*\\(ns\\s([a-zA-Z0-9\\.\\-]*)");
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
            namespaceFilterRegexs = new String[] {".*"};              
        }

        List<String> namespaces = new ArrayList<String>();

        for (String namespace : discoverNamespacesIn(paths)) {

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

    public List<String> discoverNamespacesIn(File... paths) throws MojoExecutionException {

        List<String> namespaces = new ArrayList<String>();

        for (File path : paths) {
            File[] files = path.listFiles();
            for (File file : files) {
                if (log.isDebugEnabled()) {
                    log.debug("Searching " + file.getPath() + " for clojure namespaces");
                }

                if (file.isDirectory()) {
                    namespaces.addAll(discoverNamespacesIn(file));
                } else if (file.getName().endsWith(".clj")) {
                    namespaces.addAll(findNamespaceInFile(file));
                }
            }
        }

        return namespaces;
    }

    private List<String> findNamespaceInFile(File file) throws MojoExecutionException {

        List<String> namespaces = new ArrayList<String>();

        Scanner scanner = null;
        try {
            scanner = new Scanner(file);

            scanner.useDelimiter("\n");

            while (scanner.hasNext()) {
                String line = scanner.next();

                Matcher matcher = nsPattern.matcher(line);

                if (matcher.find()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Found namespace " + matcher.group(1) + " in file " + file.getPath());
                    }
                    namespaces.add(matcher.group(1));
                }
            }
        } catch (FileNotFoundException e) {
            throw new MojoExecutionException(e.getMessage());
        }
        return namespaces;
    }

}
