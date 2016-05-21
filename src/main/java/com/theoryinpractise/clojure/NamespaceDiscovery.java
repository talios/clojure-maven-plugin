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
import org.codehaus.plexus.compiler.util.scan.InclusionScanException;
import org.codehaus.plexus.compiler.util.scan.SimpleSourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.SourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.StaleSourceScanner;
import org.codehaus.plexus.compiler.util.scan.mapping.SuffixMapping;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NamespaceDiscovery {

  private static final String TEMPORARY_FILES_REGEXP = "^(\\.|#).*";

  private final Pattern nsPattern = Pattern.compile("^\\s*\\(\\s*ns(\\s.*|$)");
  private Log log;
  private boolean compileDeclaredNamespaceOnly;
  private File targetPath;
  private boolean includeStale;
  private String charset;

  public NamespaceDiscovery(Log log, File targetPath, String charset, boolean compileDeclaredNamespaceOnly) {
    this(log, targetPath, charset, compileDeclaredNamespaceOnly, true);
  }

  public NamespaceDiscovery(Log log, File targetPath, String charset, boolean compileDeclaredNamespaceOnly, boolean includeStale) {
    this.log = log;
    this.targetPath = targetPath;
    this.compileDeclaredNamespaceOnly = compileDeclaredNamespaceOnly;
    this.includeStale = includeStale;
    this.charset = charset;
  }

  /**
   * Discover namespaces in a list of source directories filtered by a list of namespace regexs
   *
   * @param namespaceFilterRegexs An array of regexs to use to filter files
   * @param paths The path to discover in
   * @return An array of {@link NamespaceInFile} instances.
   * @throws MojoExecutionException Something went wrong...
   */
  public NamespaceInFile[] discoverNamespacesIn(String[] namespaceFilterRegexs, File... paths) throws MojoExecutionException {

    if (namespaceFilterRegexs == null || namespaceFilterRegexs.length == 0) {
      namespaceFilterRegexs = new String[] {".*"};
    }

    Set<NamespaceInFile> namespaces = new HashSet<NamespaceInFile>();

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
    return namespaces.toArray(new NamespaceInFile[] {});
  }

  public List<NamespaceInFile> discoverNamespacesInPath(File... paths) throws MojoExecutionException {

    List<NamespaceInFile> namespaces = new ArrayList<NamespaceInFile>();
    for (File path : paths) {
      namespaces.addAll(discoverNamespacesIn(path));
    }
    return namespaces;
  }

  public List<NamespaceInFile> discoverNamespacesIn(File basePath) throws MojoExecutionException {

    if (!basePath.exists()) return Collections.EMPTY_LIST;

    SourceInclusionScanner scanner = getSourceInclusionScanner(includeStale);

    scanner.addSourceMapping(new SuffixMapping(".clj", new HashSet(Arrays.asList(".clj", "__init.class"))));
    scanner.addSourceMapping(new SuffixMapping(".cljc", new HashSet(Arrays.asList(".cljc", "__init.class"))));

    final Set<File> sourceFiles;

    try {
      sourceFiles = scanner.getIncludedSources(basePath, targetPath);
    } catch (InclusionScanException e) {
      throw new MojoExecutionException("Error scanning source path: \'" + basePath.getPath() + "\' " + "for  files to recompile.", e);
    }

    List<NamespaceInFile> namespaces = new ArrayList<NamespaceInFile>();
    for (File file : sourceFiles) {
      if (!file.getName().matches(TEMPORARY_FILES_REGEXP)) {
        namespaces.addAll(findNamespaceInFile(basePath, file));
      }
    }

    return namespaces;
  }

  protected SourceInclusionScanner getSourceInclusionScanner(boolean includeStale) {
    return includeStale ? new SimpleSourceInclusionScanner(Collections.singleton("**/*"), Collections.EMPTY_SET) : new StaleSourceScanner(1024);
  }

  private List<NamespaceInFile> findNamespaceInFile(File path, File file) throws MojoExecutionException {

    List<NamespaceInFile> namespaces = new ArrayList<NamespaceInFile>();

    Scanner scanner = null;
    try {
      scanner = new Scanner(file, charset != null ? charset : Charset.defaultCharset().name());

      scanner.useDelimiter("\n");

      while (scanner.hasNext()) {
        String line = scanner.next();

        Matcher matcher = nsPattern.matcher(line);

        if (matcher.find()) {
          String ns = file.getPath();
          ns = ns.substring(path.getPath().length() + 1, ns.lastIndexOf("."));
          ns = ns.replace(File.separatorChar, '.');
          ns = ns.replace('_', '-');

          log.debug("Found namespace " + ns + " in file " + file.getPath());
          namespaces.add(new NamespaceInFile(ns, file));
        }
      }
    } catch (FileNotFoundException e) {
      throw new MojoExecutionException(e.getMessage());
    } finally {
      if (scanner != null) {
        scanner.close();
      }
    }
    return namespaces;
  }
}
