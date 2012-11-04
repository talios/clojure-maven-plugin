Welcome to the clojure-maven-plugin plugin for Apache Maven 2.

This plugin has been designed to make working with clojure as easy as possible, when working in a
mixed language, enterprise project.

## Available goals

 * clojure:add-source
 * clojure:add-test-source
 * clojure:compile
 * clojure:test
 * clojure:test-with-junit
 * clojure:run
 * clojure:repl
 * clojure:swank
 * clojure:nailgun
 * clojure:gendoc
 * clojure:autodoc
 * clojure:marginalia

## Getting started with Clojure and Maven

To use this plugin and start working with clojure, start with a blank maven project and declare the plugin and
add a dependency on clojure:

    <packaging>clojure</packaging>
    ....
    <plugins>
      <plugin>
        <groupId>com.theoryinpractise</groupId>
        <artifactId>clojure-maven-plugin</artifactId>
        <version>1.3.10</version>
        <extensions>true</extensions>
      </plugin>
    </plugins>
    ....
    <dependencies>
      <dependency>
        <groupId>org.clojure</groupId>
        <artifactId>clojure</artifactId>
        <version>1.2.0</version>
      </dependency>
    </dependencie>

By changing your projects <packaging> type to clojure, the plugin will automatically bind itself to the compile,
test-compile, and test maven phases.

Without any further configuration, Maven will compile any clojure namespaces you include in ./src/main/clojure/*.clj
and ./src/test/clojure/*.clj.

### Adding additional source directories

To change, or add additional source directories you can add the following configuration:

    <configuration>
      <sourceDirectories>
        <sourceDirectory>src/main/clojure</sourceDirectory>
      </sourceDirectories>
      <testSourceDirectories>
        <testSourceDirectory>src/test/clojure</testSourceDirectory>
      </testSourceDirectories>
    </configuration>

NOTE: The plugin will prepend the project's ${basedir} before each source/testSource directory specified.

### Temporary Compile Paths

If you wish to take advantage of the compilers syntax checking, but wish to prevent any AOT classes from
appearing in the maven generated JAR file, you can tell the plugin to compile to a temporary directory:

    <configuration>
      <temporaryOutputDirectory>true</temporaryOutputDirectory>
    </configuration>

### Namespace configuration

If you wish to limit or filter out namespaces during your compile/test, simply add a `<namespaces>` or `<testNamespaces>`
configuration section:

    <configuration>
      <namespaces>
        <namespace>com.foo</namespace>
        <namespace>net.*</namespace>
        <namespace>!testing.*</namespace>
      </namespaces>
    </configuration>

The namespace declaration is actually a regex match against discovered namespaces, and can also be
prepended with an ! to filter the matching namespace.

If you wish to further limit test/compile usage to only the namespaces you define, you can enable this with the
configuration block:

    <configuration>
      <compileDeclaredNamespaceOnly>true</compileDeclaredNamespaceOnly>
      <testDeclaredNamespaceOnly>true</testDeclaredNamespaceOnly>
    </configuration>

## Interactive Coding

The plugin supports several goals intended to make it easier for developers to run interactive clojure shells
in the context of maven projects.  This means that all dependencies in a project's runtime and test scopes
will be automatically added to the classpath and available for experimentation.

By default these goals will use the test classpath, if you wish to only use the compile classpath/dependencies,
you can disable this with:

    <configuration>
      <runWithTests>false</runWithTests>
    </configuration>

or by running maven with:

    -Dclojure.runwith.test=false

### Goals
          
<table>
  <tr>   
  	<th></th>
    <th>Property</th>
    <th>Variable</th>
    <th>Default</th>
    <th>Description</th>
  </tr>
  <tr>
  	<td colspan="5"><b>clojure:repl</b>&nbsp;&mdash;&nbsp;Starts an interactive clojure REPL right on the command line.</td>
  </tr>
  <tr>
  	<td></td>
    <td>replScript</td>
    <td></td>
    <td></td>
    <td>An initialization script can be specified in the pom using the replScript configuration element.</td>
  </tr>
  <tr>
  	<td></td>
    <td>windowsRepl</td>
    <td></td>
    <td>cmd /c start</td>
    <td>Allows to configure the command line that will be executed in Windows.</td>
  </tr>
  <tr>
  	<td colspan="5"><b>clojure:swank</b>&nbsp;&mdash;&nbsp;Starts a Swank server that accepts connections.</td>
  </tr>
  <tr>
  	<td></td>
    <td>replScript</td>
    <td></td>
    <td></td>
    <td>The clojure script to run before starting the repl</td>
  </tr>
  <tr>
  	<td></td>
    <td>port</td>
    <td>clojure.swank.port</td>
    <td>4005</td>
    <td>The swank server port</td>
  </tr>
  <tr>
  	<td></td>
    <td>protocolVersion</td>
    <td>clojure.swank.protocolVersion</td>
    <td>2009-09-14</td>
    <td>The swank protocol version</td>
  </tr>
  <tr>
  	<td></td>
    <td>encoding</td>
    <td>clojure.swank.encoding</td>
    <td>iso-8859-1</td>
    <td>The swank encoding to use</td>
  </tr>
  <tr>
  	<td></td>
    <td>swankHost</td>
    <td>clojure.swank.host</td>
    <td>localhost</td>
    <td>The host to bind the swank server to/td>
  </tr>
  <tr>
  	<td colspan="5"><b>clojure:nailgun</b>&nbsp;&mdash;&nbsp;Starts a nailgun server.</td>
  </tr>
  <tr>
  	<td></td>
    <td>replScript</td>
    <td></td>
    <td></td>
    <td>The clojure script to run before starting the repl</td>
  </tr>
  <tr>
  	<td></td>
    <td>port</td>
    <td>clojure.nailgun.port</td>
    <td>2113</td>
    <td>The nailgun server port</td>
  </tr>
  <tr>
  	<td colspan="5"><b>clojure:run</b>&nbsp;&mdash;&nbsp;Runs a clojure script.</td>
  </tr>
  <tr>
  	<td></td>
    <td>script</td>
    <td>clojure.script</td>
    <td></td>
    <td>The clojure script to run</td>
  </tr>
  <tr>
  	<td></td>
    <td>scripts</td>
    <td></td>
    <td></td>
    <td>A list of clojure scripts to run</td>
  </tr>
  <tr>
  	<td></td>
    <td>mainClass</td>
    <td>clojure.mainClass</td>
    <td></td>
    <td>A java class to run</td>
  </tr>
  <tr>
  	<td></td>
    <td>args</td>
    <td>clojure.args</td>
    <td></td>
    <td>Arguments to the clojure script(s)</td>
  </tr>
  <tr>
  	<td colspan="5"><b>clojure:add-source</b>&nbsp;&mdash;&nbsp;Includes clojure source directory in -sources.jar.</td>
  </tr>
  <tr>
  	<td colspan="5"><b>clojure:add-testsource</b>&nbsp;&mdash;&nbsp;Includes clojure test source directory in -testsources.jar.</td>
  </tr>

</table>

## Testing Clojure Code

Whilst you could easily launch your tests from the clojure:run goal, the plugin provides two goals targeted
specifically to testing: clojure:test and clojure:test-with-junit

Without any additional configuration the plugin will generate and execute the following temporary clojure
"test launcher" script:

    (require 'one.require.for.each.discovered.namespace)
    (use 'clojure.test)

    (when-not *compile-files*
      (let [results (atom [])]
        (let [report-orig report]
          (binding [report (fn [x] (report-orig x)
                             (swap! results conj (:type x)))]
            (run-tests 'one.require.for.each.discovered.namespace)))
        (shutdown-agents)
        (System/exit (if (empty? (filter #{:fail :error} @results)) 0 -1))))

The generated script requires any discovered *test* namespaces, runs all the tests, and fails the build when any FAIL or
ERROR cases are found.

If you require different test behavior, you can provide your own test script with the following configuration:

    <configuration>
      <testScript>src/test/clojure/com/jobsheet/test.clj</testScript>
    </configuration>

## Configuring your clojure session

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

The plugin can also copy source files to the output directory, filtered using the namespace mechanism
that is used to control compilation. If you want to copy all compiled source files to the output:

    <configuration>
      <copyAllCompiledNamespaces>true</copyAllCompiledNamespaces>
    <configuration>

If you want to copy only a subset:

    <configuration>
      <copiedNamespaces>
        <namespace>com.foo</namespace>
        <namespace>!com.foo.private.*</namespace>
      </copiedNamespaces>
      <copyDeclaredNamespaceOnly>true</copyDeclaredNamespaceOnly>
    <configuration>

If you want to do no compilation at all, but copy all source files:

    <configuration>
      <copyDeclaredNamespaceOnly>true</copyDeclaredNamespaceOnly>
      <namespaces>
        <namespace>!.*</namespace>
      </namespaces>
      <compileDeclaredNamespaceOnly>true</compileDeclaredNamespaceOnly>
    <configuration>

Note that it will only copy clojure source files, which must a) end in .clj and b) contain a namespace declaration.

Enjoy.

### Dependencies

In order to run clojure:repl, clojure:swank or clojure:nailgun, your project
needs to have a recent (1.0 or later) version of clojure as a dependency in
pom.xml.

In order to run clojure:autodoc, your project needs to have autodoc as a
dependency in pom.xml.

#### JLine/IClojure/REPL-y

If JLine is detected in the classpath, it will be used to provide the
clojure:repl goal with history, tab completion, etc. A simple way of
enabling this is to put the following in your pom.xml:

		<dependency>
		   <groupId>jline</groupId>
		   <artifactId>jline</artifactId>
		   <version>0.9.94</version>
		</dependency>

If you prefer [IClojure](http://www.iclojure.com/) you can add:

		<dependency>
		   <groupId>org.offbytwo.iclojure</groupId>
		   <artifactId>iclojure</artifactId>
		   <version>1.1.0</version>
		</dependency>

Or [REPL-y](https://github.com/trptcolin/reply/):

		<dependency>
		   <groupId>reply</groupId>
		   <artifactId>reply</artifactId>
		   <version>0.1.0-beta9</version>
		</dependency>

#### Swank

The clojure:swank goal requires swank-clojure as a projet dependency.
Unfortunatly, this library is currently not available in the central maven
repository, but is available from clojars by first declaring the repository:

    <repositories>
      <repository>
        <id>clojars</id>
        <url>http://clojars.org/repo/</url>
      </repository>
    </repositories>

and then declaring the dependency itself:

    <dependency>
      <groupId>swank-clojure</groupId>
      <artifactId>swank-clojure</artifactId>
      <version>1.3.0-SNAPSHOT</version>
    </dependency>

By default the swank process will run against the local loopback device, if you wish to change the host your
swank server runs against, you can configure it via:

    <configuration>
      <swankHost>localhost</swankHost>
    </configuration>

or by defining the clojure.swank.host system property.

#### Nailgun for Vimclojure < 2.2.0

The clojure:nailgun goal requires a recent version of vimclojure as a
dependency. Unfortunately, this library is currently not available in
the central maven repository, and has to be downloaded and installed
manually:

 1. Download vimclojure source code from `http://cloud.github.com/downloads/jochu/swank-clojure/swank-clojure-1.0-SNAPSHOT-distribution.zip`.
 2. Follow the README to compile and install vimclojure.
 3. Locate vimclojure.jar and run the following command to install it to your local repository (replace X.X.X with your version of vimclojure):

    	mvn install:install-file -DgroupId=de.kotka -DartifactId=vimclojure -Dversion=X.X.X -Dpackaging=jar -Dfile=/path/to/jarfile

 4. Put the following in your pom.xml (replace X.X.X with your version of vimclojure)

    	<dependency>
		<groupId>de.kotka</groupId>
		<artifactId>vimclojure</artifactId>
		<version>X.X.X</version>
    	</dependency>

 5. You will need to run `mvn clojure:nailgun -Dclojure.nailgun.server=com.martiansoftware.nailgun.NGServer` in order to
    work with the old version (pre 2.2.0) of vimclojure.

#### Nailgun for Vimclojure >= 2.2.0

To use `clojure 1.2.0` comfortably, you will need to upgrade to `Vimclojure
2.2.0` which isn't backwards compatible with previous vimclojure versions.  Now
you will need a dependency on the `vimclojure:server:2.2.0` which contains the
modified Nailgun server.

    <dependency>
        <groupId>vimclojure</groupId>
        <artifactId>server</artifactId>
        <version>2.2.0</version>
    </dependency>

The jar can be found in [clojars](http://clojars.org/) maven repo (you'll have
to add it to the `repositories` section)

    <repository>
        <id>clojars</id>
        <name>Clojars</name>
        <url>http://clojars.org/repo/</url>
    </repository>

The installation process for vimclojure remains the same (except for the
`vimclojure.jar` which you don't need to install anymore).  Just get the
vimclojure package from http://kotka.de/projects/clojure/vimclojure.html and
follow the README.

Notes for migration from the previous version of vimclojure:

* `clj_highlight_builtins` was deprecated in favor of `vimclojure#HighlightBuiltins`
* `clj_highlight_contrib` was removed
* `g:clj_paren_rainbow` was deprecated in favor of `vimclojure#ParenRainbow`
* `g:clj_want_gorilla` was deprecated in favor of `vimclojure#WantNailgun`

#### Windows configuration

As the default Windows console doesn't allow to easily copy and paste code, you can use the `windowsConsole`
configuration option to specify which console command to run in Windows. For example if you are using
http://code.google.com/p/conemu-maximus5/, you can configure the plugin with:

`<windowsConsole>"C:\\Program Files\\ConEmu\\ConEmu64.exe" /cmd</windowsConsole>`

which will give you a sane Windows console

### Configuration

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
</table>

### Support

Join the discussion mailing list at:

http://groups.google.com/group/clojure-maven-plugin

