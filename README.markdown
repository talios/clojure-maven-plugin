Welcome to the clojure-maven-plugin plugin for Apache Maven 2.

This plugin has been designed to make working with clojure as easy as possible, when working in a mixed language, enterprise project.

# Available goals
- clojure:add-source
- clojure:add-test-source
- clojure:compile
- clojure:test
- clojure:test-with-junit
- clojure:run
- clojure:repl
- clojure:nrepl
- clojure:swank
- clojure:nailgun
- clojure:gendoc
- clojure:autodoc
- clojure:marginalia

# Getting started with Clojure and Maven
To use this plugin and start working with clojure, start with a blank maven project and declare the plugin and add a dependency on clojure:

```
<packaging>clojure</packaging>
....
<build>
  <plugins>
    <plugin>
      <groupId>com.theoryinpractise</groupId>
      <artifactId>clojure-maven-plugin</artifactId>
      <version>1.8.3</version>
      <extensions>true</extensions>
    </plugin>
  </plugins>
</build>
....
<dependencies>
  <dependency>
    <groupId>org.clojure</groupId>
    <artifactId>clojure</artifactId>
    <version>1.10.0</version>
  </dependency>
</dependencies>
```

By changing your projects <packaging> type to clojure, the plugin will automatically bind itself to the compile, test-compile, and test maven phases.

Without any additional configuration, the clojure-maven-plugin will compile any namespaces in ./src/main/clojure/_.clj (or .cljc) and ./src/test/clojure/_.clj (or .cljc).

## Adding additional source directories
To change, or add additional source directories you can add the following configuration:

```
<configuration>
  <sourceDirectories>
    <sourceDirectory>src/main/clojure</sourceDirectory>
  </sourceDirectories>
  <testSourceDirectories>
    <testSourceDirectory>src/test/clojure</testSourceDirectory>
  </testSourceDirectories>
</configuration>
```

NOTE: The plugin will prepend the project's ${basedir} before each source/testSource directory specified.

## Temporary Compile Paths
If you wish to take advantage of the compilers syntax checking, but wish to prevent any AOT classes from appearing in the maven generated JAR file, you can tell the plugin to compile to a temporary directory:

```
<configuration>
  <temporaryOutputDirectory>true</temporaryOutputDirectory>
</configuration>
```

## Namespace configuration
If you wish to limit or filter out namespaces during your compile/test, simply add a `<namespaces>` or `<testNamespaces>` configuration section:

```
<configuration>
  <namespaces>
    <namespace>com.foo</namespace>
    <namespace>net.*</namespace>
    <namespace>!testing.*</namespace>
  </namespaces>
</configuration>
```

The namespace declaration is actually a regex match against discovered namespaces, and can also be prepended with an ! to filter the matching namespace.

If you wish to further limit test/compile usage to only the namespaces you define, you can enable this with the configuration block:

```
<configuration>
  <compileDeclaredNamespaceOnly>true</compileDeclaredNamespaceOnly>
  <testDeclaredNamespaceOnly>true</testDeclaredNamespaceOnly>
</configuration>
```

If you want that only compiled artifacts related to the above mentioned `<namespaces>` and `<compileDeclaredNamespaceOnly>` be kept, then add a `cleanAOTNamespaces` config param and set it to `true`.

For instance (1/2), with the following configuration ...

```
<configuration>
  <cleanAOTNamespaces>true</cleanAOTNamespaces>
  <namespaces>
    <namespace>!some.annoying.namespace</namespace>
  </namespaces>
<configuration>
```

... all aot-compiled classes created in the output directory will be kept as is, but the ones of the `some.annoying.namespace` namespace which will be deleted.

For instance (2/2), with the following configuration ...

```
<configuration>
  <cleanAOTNamespaces>true</cleanAOTNamespaces>
  <namespaces>
    <namespace>some.namespace.with.a.gen-class</namespace>
  </namespaces>
  <compileDeclaredNamespaceOnly>true</compileDeclaredNamespaceOnly>
<configuration>
```

... all aot-compiled classes will be deleted, but the ones of the `some.annoying.namespace` namespace.

# Interactive Coding
The plugin supports several goals intended to make it easier for developers to run interactive clojure shells in the context of maven projects.  This means that all dependencies in a project's runtime and test scopes will be automatically added to the classpath and available for experimentation.

By default these goals will use the test classpath, if you wish to only use the compile classpath/dependencies, you can disable this with:

```
<configuration>
  <runWithTests>false</runWithTests>
</configuration>
```

or by running maven with:

```
-Dclojure.runwith.test=false
```

## Goals
<table>
    <tr>
        <th>Goal</th>
        <th>Description</th>
    </tr>
    <tr>
        <td>clojure:repl</td>
        <td>
            <p>Starts an interactive clojure REPL right on the command line.</p>
            <table>
              <tr>
                <th>Property</th>
                <th>Variable</th>
                <th>Default</th>
                <th>Description</th>
              </tr>
              <tr>
                <td>replScript</td>
                <td></td>
                <td></td>
                <td>An initialization script can be specified in the pom using the replScript configuration element.</td>
              </tr>
            </table>
        </td>
    </tr>
    <tr>
      <td>clojure:nrepl</td>
      <td>
        <p>Starts a nREPL server that accepts connections.</p>
        <table>
          <tr>
            <th>Property</th>
            <th>Variable</th>
            <th>Default</th>
            <th>Description</th>
          </tr>
          <tr>
            <td>replScript</td>
            <td></td>
            <td></td>
            <td>The clojure script to run before starting the repl</td>
          </tr>
          <tr>
            <td>port</td>
            <td>clojure.nrepl.port</td>
            <td>4005</td>
            <td>The nREPL server port</td>
          </tr>
          <tr>
            <td>nreplHost</td>
            <td>clojure.nrepl.host</td>
            <td>localhost</td>
            <td>The host to bind the nREPL server to/td>
          </tr>
          <tr>
            <td>nreplHandler</td>
            <td>clojure.nrepl.handler</td>
            <td></td>
            <td>The nREPL Handler to use. i.e. <pre>cider.nrepl/cider-nrepl-handler</pre> from the <pre>cider-nepl</pre> project./td>
          </tr>
        </table>
      </td>
    </tr>
    <tr>
        <td>clojure:swank</td>
        <td>
            <p>Starts a Swank server that accepts connections.</p>
            <table>
              <tr>
                <th>Property</th>
                <th>Variable</th>
                <th>Default</th>
                <th>Description</th>
              </tr>
              <tr>
                <td>replScript</td>
                <td></td>
                <td></td>
                <td>The clojure script to run before starting the repl</td>
              </tr>
              <tr>
                <td>port</td>
                <td>clojure.swank.port</td>
                <td>4005</td>
                <td>The swank server port</td>
              </tr>
              <tr>
                <td>protocolVersion</td>
                <td>clojure.swank.protocolVersion</td>
                <td>2009-09-14</td>
                <td>The swank protocol version</td>
              </tr>
              <tr>
                <td>encoding</td>
                <td>clojure.swank.encoding</td>
                <td>iso-8859-1</td>
                <td>The swank encoding to use</td>
              </tr>
              <tr>
                <td>swankHost</td>
                <td>clojure.swank.host</td>
                <td>localhost</td>
                <td>The host to bind the swank server to/td>
              </tr>
            </table>
        </td>
    </tr>
    <tr>
        <td>clojure:nailgun</td>
        <td>
            <p>Starts a nailgun server.</p>
            <table>
              <tr>
                <th>Property</th>
                <th>Variable</th>
                <th>Default</th>
                <th>Description</th>
              </tr>
              <tr>
                <td>replScript</td>
                <td></td>
                <td></td>
                <td>The clojure script to run before starting the repl</td>
              </tr>
              <tr>
                <td>port</td>
                <td>clojure.nailgun.port</td>
                <td>2113</td>
                <td>The nailgun server port</td>
              </tr>
            </table>
        </td>
    </tr>
    <tr>
        <td>clojure:run</td>
        <td>
            <p>Runs a clojure script.</p>
            <table>
              <tr>
                <th>Property</th>
                <th>Variable</th>
                <th>Default</th>
                <th>Description</th>
              </tr>
              <tr>
                <td>script</td>
                <td>clojure.script</td>
                <td></td>
                <td>The clojure script to run</td>
              </tr>
              <tr>
                <td>scripts</td>
                <td></td>
                <td></td>
                <td>A list of clojure scripts to run</td>
              </tr>
              <tr>
                <td>mainClass</td>
                <td>clojure.mainClass</td>
                <td></td>
                <td>A java class to run</td>
              </tr>
              <tr>
                <td>args</td>
                <td>clojure.args</td>
                <td></td>
                <td>Arguments to the clojure script(s)</td>
              </tr>
            </table>
        </td>
    </tr>
    <tr>
        <td>clojure:add-source</td>
        <td>Includes clojure source directory in -sources.jar.</td>
    </tr>
    <tr>
        <td>clojure:add-test-source</td>
        <td>Includes clojure test source directory in -testsources.jar.</td>
    </tr>

</table>

# Testing Clojure Code
Whilst you could easily launch your tests from the clojure:run goal, the plugin provides two goals targeted specifically to testing: clojure:test and clojure:test-with-junit

Without any additional configuration the plugin will run a temporary clojure "test launcher" script:

The script runs all discovered _test_ namespaces, and fails the build when any FAIL or ERROR cases are found.

If you require different test behavior, you can provide your own test script with the following configuration:

```
<configuration>
  <testScript>src/test/clojure/com/jobsheet/test.clj</testScript>
</configuration>
```

The first argument to the script is the name of a properties file that has in it a config for the user selected. These configs can be parsed out using the following code

```
(def props (Properties.))
(.load props (FileInputStream. (first *command-line-args*)))

;;namespaces to run tests for
(def namespaces  (into []
                       (for [[key val] props
                             :when (.startsWith key "ns.")]
                               (symbol val))))

;; should there be junit compatible output or not
(def junit (Boolean/valueOf (.get props "junit")))
;; what is the output directory that results should be written to
(def output-dir (.get props "outputDir"))
;; should we xml-escape *out* while the tests are running
(def xml-escape (Boolean/valueOf (.get props "xmlEscape")))
```

We reserve the right to add new configs in the future, and possibly new command line arguments as well.

# Configuring your clojure session
If you want to provide additional arguments to all spawned java/clojure processes, the plugin provides several configuration properties:
<table>
  <tr>
    <th>Property</th>
    <th>Variable</th>
    <th>Default</th>
    <th>Description</th>
  </tr>
  <tr>
    <td>vmargs</td>
    <td>clojure.vmargs</td>
    <td></td>
    <td>JVM Arguments</td>
  </tr>
  <tr>
    <td>clojureOptions</td>
    <td></td>
    <td></td>
    <td>Additional JVM Options such as system property definitions</td>
  </tr>
  <tr>
    <td>warnOnReflection</td>
    <td></td>
    <td>false</td>
    <td>Enable reflection warnings</td>
  </tr>
  <tr>
    <td>prependClasses</td>
    <td></td>
    <td></td>
    <td>A list of classnames to prepend to the command line before the mainClass</td>
  </tr>
</table>

The plugin can also copy source files to the output directory, filtered using the namespace mechanism that is used to control compilation. If you want to copy all compiled source files to the output:

```
<configuration>
  <copyAllCompiledNamespaces>true</copyAllCompiledNamespaces>
<configuration>
```

If you want to copy only a subset:

```
<configuration>
  <copiedNamespaces>
    <namespace>com.foo</namespace>
    <namespace>!com.foo.private.*</namespace>
  </copiedNamespaces>
  <copyDeclaredNamespaceOnly>true</copyDeclaredNamespaceOnly>
<configuration>
```

If you want to do no compilation at all, but copy all source files:

```
<configuration>
  <copyDeclaredNamespaceOnly>true</copyDeclaredNamespaceOnly>
  <namespaces>
    <namespace>!.*</namespace>
  </namespaces>
  <compileDeclaredNamespaceOnly>true</compileDeclaredNamespaceOnly>
<configuration>
```

Note that it will only copy clojure source files, which must a) end in .clj or .cljc and b) contain a namespace declaration.

Enjoy.

## Dependencies
In order to run clojure:repl, clojure:swank or clojure:nailgun, your project needs to have a recent (1.0 or later) version of clojure as a dependency in pom.xml.

In order to run clojure:autodoc, your project needs to have autodoc as a dependency in pom.xml.

In order to run clojure:nrepl, your project needs to have org.clojure/tools.nrepl as a dependency in pom.xml.

### JLine/IClojure/REPL-y
If JLine is detected in the classpath, it will be used to provide the clojure:repl goal with history, tab completion, etc. A simple way of enabling this is to put the following in your pom.xml:

```
    <dependency>
       <groupId>jline</groupId>
       <artifactId>jline</artifactId>
       <version>0.9.94</version>
    </dependency>
```

If you prefer [IClojure](https://github.com/cosmin/IClojure) you can add:

```
    <dependency>
       <groupId>com.offbytwo.iclojure</groupId>
       <artifactId>iclojure</artifactId>
       <version>1.1.0</version>
    </dependency>
```

Or [REPL-y](https://github.com/trptcolin/reply/):

```
    <dependency>
       <groupId>reply</groupId>
       <artifactId>reply</artifactId>
       <version>0.1.0-beta9</version>
    </dependency>
```

### Swank
The clojure:swank goal requires swank-clojure as a projet dependency. Unfortunatly, this library is currently not available in the central maven repository, but is available from clojars by first declaring the repository:

```
<repositories>
  <repository>
    <id>clojars</id>
    <url>http://clojars.org/repo/</url>
  </repository>
</repositories>
```

and then declaring the dependency itself:

```
<dependency>
  <groupId>swank-clojure</groupId>
  <artifactId>swank-clojure</artifactId>
  <version>1.3.0-SNAPSHOT</version>
</dependency>
```

By default the swank process will run against the local loopback device, if you wish to change the host your swank server runs against, you can configure it via:

```
<configuration>
  <swankHost>localhost</swankHost>
</configuration>
```

or by defining the clojure.swank.host system property.

#### nREPL

The clojure:nrepl goal requires `nrepl` as a project dependency as:

```
<dependency>
  <groupId>nrepl</groupId>
  <artifactId>nrepl</artifactId>
  <version>0.6.0</version>
</dependency>
```

By default the nREPL process will run against the local loopback device on port 4005, if you wish to change the host your nREPL server runs against or the port, you can configure it via:

```
<configuration>
  <nreplHost>localhost</nreplHost>
  <port>9001</port>
</configuration>
```

or by defining the clojure.nrepl.host and clojure.nrepl.port system properties.

It is also possible to specify a custom handler or server-side middleware to be added to the nREPL stack. This may be necessary for integrating with Clojure IDEs,
such as [LightTable](https://github.com/LightTable/LightTable) or [CIDER](https://github.com/clojure-emacs/cider). These IDEs
require custom nREPL middleware for best results or may not work at all with the default nREPL stack. nREPL middleware can be specified as follows:

```
<configuration>
    <nreplMiddlewares>
        <middleware>my.nrepl.middleware/my-middleware</middleware>
    </nreplMiddlewares>
</configuration>
```

Either a custom handler or as many middleware as necessary can be specified. Each middleware must be specified as a fully qualified symbol -
namespace/name. Thy symbol must resolve to a var referencing a middleware function. If the middleware is not
part of the project itself, it must be specified as a dependency. The same is true for custom nRepl handlers

LightTable configuration example:

```
<dependency>
    <groupId>lein-light-nrepl</groupId>
    <artifactId>lein-light-nrepl</artifactId>
    <version>0.3.3</version>
    <scope>test</scope>
</dependency>
...
<configuration>
    <nreplMiddlewares>
        <middleware>lighttable.nrepl.handler/lighttable-ops</middleware>
    </nreplMiddlewares>
</configuration>
```

CIDER configuration example:

```
<dependency>
    <groupId>cider</groupId>
    <artifactId>cider-nrepl</artifactId>
    <version>0.12.0</version>
    <scope>test</scope>
</dependency>
.......
<configuration>
        <nreplMiddlewares>
            <middleware>cider.nrepl/wrap-apropos</middleware>
            <middleware>cider.nrepl/wrap-classpath</middleware>
            <middleware>cider.nrepl/wrap-complete</middleware>
            <middleware>cider.nrepl/wrap-debug</middleware>
            <middleware>cider.nrepl/wrap-format</middleware>
            <middleware>cider.nrepl/wrap-info</middleware>
            <middleware>cider.nrepl/wrap-inspect</middleware>
            <middleware>cider.nrepl/wrap-macroexpand</middleware>
            <middleware>cider.nrepl/wrap-ns</middleware>
            <middleware>cider.nrepl/wrap-spec</middleware>
            <middleware>cider.nrepl/wrap-profile</middleware>
            <middleware>cider.nrepl/wrap-refresh</middleware>
            <middleware>cider.nrepl/wrap-resource</middleware>
            <middleware>cider.nrepl/wrap-stacktrace</middleware>
            <middleware>cider.nrepl/wrap-test</middleware>
            <middleware>cider.nrepl/wrap-trace</middleware>
            <middleware>cider.nrepl/wrap-out</middleware>
            <middleware>cider.nrepl/wrap-undef</middleware>
            <middleware>cider.nrepl/wrap-version</middleware>
            <middleware>cider.nrepl/wrap-xref</middleware>
        </nreplMiddlewares>
</configuration>
```

### Nailgun for Vimclojure < 2.2.0
The clojure:nailgun goal requires a recent version of vimclojure as a dependency. Unfortunately, this library is currently not available in the central maven repository, and has to be downloaded and installed manually:
1. Download vimclojure source code from `http://cloud.github.com/downloads/jochu/swank-clojure/swank-clojure-1.0-SNAPSHOT-distribution.zip`.
2. Follow the README to compile and install vimclojure.
- Locate vimclojure.jar and run the following command to install it to your local repository (replace X.X.X with your version of vimclojure):

  ```
  mvn install:install-file -DgroupId=de.kotka -DartifactId=vimclojure -Dversion=X.X.X -Dpackaging=jar -Dfile=/path/to/jarfile
  ```

- Put the following in your pom.xml (replace X.X.X with your version of vimclojure)

  ```
  <dependency>
  <groupId>de.kotka</groupId>
  <artifactId>vimclojure</artifactId>
  <version>X.X.X</version>
  </dependency>
  ```

- You will need to run `mvn clojure:nailgun -Dclojure.nailgun.server=com.martiansoftware.nailgun.NGServer` in order to

  work with the old version (pre 2.2.0) of vimclojure.

### Nailgun for Vimclojure >= 2.2.0
To use `clojure 1.2.0` comfortably, you will need to upgrade to `Vimclojure 2.2.0` which isn't backwards compatible with previous vimclojure versions.  Now you will need a dependency on the `vimclojure:server:2.2.0` which contains the modified Nailgun server.

```
<dependency>
    <groupId>vimclojure</groupId>
    <artifactId>server</artifactId>
    <version>2.2.0</version>
</dependency>
```

The jar can be found in [clojars](http://clojars.org/) maven repo (you'll have to add it to the `repositories` section)

```
<repository>
    <id>clojars</id>
    <name>Clojars</name>
    <url>http://clojars.org/repo/</url>
</repository>
```

The installation process for vimclojure remains the same (except for the `vimclojure.jar` which you don't need to install anymore).  Just get the vimclojure package from [http://kotka.de/projects/clojure/vimclojure.html](http://kotka.de/projects/clojure/vimclojure.html) and follow the README.

Notes for migration from the previous version of vimclojure:
- `clj_highlight_builtins` was deprecated in favor of `vimclojure#HighlightBuiltins`
- `clj_highlight_contrib` was removed
- `g:clj_paren_rainbow` was deprecated in favor of `vimclojure#ParenRainbow`
- `g:clj_want_gorilla` was deprecated in favor of `vimclojure#WantNailgun`

### Windows configuration
As the default Windows console doesn't allow to easily copy and paste code, you can use the `windowsConsole` configuration option to specify which console command to run in Windows. For example if you are using [http://code.google.com/p/conemu-maximus5/](http://code.google.com/p/conemu-maximus5/), you can configure the plugin with:

`<windowsConsole>"C:\\Program Files\\ConEmu\\ConEmu64.exe" /cmd</windowsConsole>`

which will give you a sane Windows console

## Configuration
The following options that can be configured as system properties:
<table>
    <tr>
        <th>Property</th>
        <th>Default value</th>
        <th>Description</th>
    </tr>
    <tr>
        <td>clojure.nailgun.port</td>
        <td>4005</td>
        <td>
            Only applicable for the <code>clojure:nailgun</code> goal.
            The port number that the Nailgun server should listen to.
        </td>
    </tr>
    <tr>
        <td>clojure.swank.port</td>
        <td>4005</td>
        <td>
            Only applicable for the <code>clojure:swank</code> goal.
            The port number that the Swank server should listen to.
        </td>
    </tr>
    <tr>
        <td>clojure.swank.protocolVersion</td>
        <td>2009-09-14</td>
        <td>
            Only applicable for the <code>clojure:swank</code> goal.
            Specifies the version of the swank protocol.
        </td>
    </tr>
    <tr>
        <td>clojure.swank.encoding</td>
        <td>iso-8859-1</td>
        <td>
            Only applicable for the <code>clojure:swank</code> goal.
            Specifies the encoding used by the swank protocol.
        </td>
    </tr>
    <tr>
        <td>clojure.nrepl.port</td>
        <td>4005</td>
        <td>
            Only applicable for the <code>clojure:nrepl</code> goal.
            The port number that the nREPL should listen to.
        </td>
    </tr>
    <tr>
        <td>clojure.nrepl.host</td>
        <td>4005</td>
        <td>
            Only applicable for the <code>clojure:nrepl</code> goal.
            The host that the nREPL should listen to.
        </td>
    </tr>
</table>

## Support
Join the discussion mailing list at:

[http://groups.google.com/group/clojure-maven-plugin](http://groups.google.com/group/clojure-maven-plugin)
