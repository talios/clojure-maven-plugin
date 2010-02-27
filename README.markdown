Welcome to the clojure-maven-plugin plugin for Apache Maven 2.

## Available goals

 * clojure:compile
 * clojure:test
 * clojure:run
 * clojure:repl
 * clojure:swank
 * clojure:nailgun

## Compiling clojure sources

To use this plugin and start compiling clojure code as part of your maven build, add the following:

    <plugins>
      <plugin>
        <groupId>com.theoryinpractise</groupId>
        <artifactId>clojure-maven-plugin</artifactId>
        <version>1.3.1</version>
      </plugin>
    </plugins>

Without any additional configuration, the clojure-maven-plugin will compile any
namespaces in ./src/main/clojure/*.clj and ./src/test/clojure/*.clj.

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

The plugin provides a clojure:run goal for run a predefined clojure script defined by:

    <configuration>
      <script>src/test/clojure/com/jobsheet/jetty.clj</script>
    </configuration>

whilst you could easily launch your tests from the clojure:run goal, the clojure:test goal is more appropriate,
without any additional configuration the plugin will generate and execute the following temporary clojure
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
        (System/exit (if (empty? (filter {:fail :error} @results)) 0 -1))))

The generated script requires any discovered *test* namespaces, runs all the tests, and fails the build when any FAIL or
ERROR cases are found.

If you require different test behaviour, you can provide your own test script with the following configuration:

    <configuration>
      <testScript>src/test/clojure/com/jobsheet/test.clj</testScript>
    </configuration>

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

If you want to provide additional arguments to all spawned java/clojure processes, add a
`<clojureOptions>` configuration element.  In addition, a `<warnOnReflection>` configuration
element is available as a shortcut to specifying the system property that controls whether or
not the AOT clojure compilation process emits reflection warnings:

    <configuration>
        <clojureOptions>-Xmx512m</clojureOptions>
        <warnOnReflection>true</warnOnReflection>
    </configuration>

Enjoy.

## clojure:run, clojure:repl, clojure:swank and clojure:nailgun goals

clojure-maven-plugin supports four goals intended to make it easier
to developers to run clojure shells in the context of maven projects.
This means that all dependencies in a project's runtime and test scopes
will be automatically added to the classpath and available for experimentation.

By default these goals will use the test classpath, if you wish to only use the
compile classpath/dependencies, you can disable this with:

    <configuration>
      <runWithTests>false</runWithTests>
    </configuration>

or by running maven with:

    -Dclojure.runwith.test=false

<table>
	<tr>
		<th>Goal</th>
		<th>Description</th>
	</tr>
	<tr>
		<td>clojure:repl</td>
		<td>
			Starts an interactive clojure REPL right on the command line. An
            initialisation script can be specified in the pom using the
            replScript configuration element.
		</td>
	</tr>
	<tr>
		<td>clojure:swank</td>
		<td>
			Starts a Swank server that accepts connections on port 4005
			(can be changed using the `-Dclojure.swank.port=X`option). You can
			connect to this server from emacs with `M-x slime-connect`.
		</td>
	</tr>
	<tr>
		<td>clojure:nailgun</td>
		<td>
			Starts a nailgun server that accepts connections on port 2113
			(can be changed using the `-Dclojure.nailgun.port=X`option). You can
			connect to this server from vim using vimclojure
			(http://kotka.de/projects/clojure/vimclojure.html).
		</td>
	</tr>
	<tr>
		<td>clojure:run</td>
		<td>
			Runs a clojure script specified in the pom using the &lt;script&gt; and/or &lt;scripts&gt;
            configuration element.
		</td>
	</tr>

</table>

### Dependencies

In order to run clojure:repl, clojure:swank or clojure:nailgun, your project
needs to have a recent (1.0 or later) version of clojure as a dependency in
pom.xml.

#### JLine

If JLine is detected in the classpath, it will be used to provide the
clojure:repl goal with history, tab completion, etc. A simple way of
enabling this is to put the following in your pom.xml:

		<dependency>
		   <groupId>jline</groupId>
		   <artifactId>jline</artifactId>
		   <version>0.9.94</version>
		</dependency>

#### Swank

The clojure:swank goal requires a recent version of swank-clojure as a
dependency. Unfortunatly, this library is currently not available in
the central maven repository, and has to be downloaded and installed
manually:

 1. Download `http://cloud.github.com/downloads/jochu/swank-clojure/swank-clojure-1.0-SNAPSHOT-distribution.zip`
 2. Unzip the distribution and extract the swank-clojure-1.0-SNAPSHOT.jar file within.
 3. Run the following command to install the jar file to your local repository:

    	mvn install:install-file -DgroupId=com.codestuffs.clojure -DartifactId=swank-clojure -Dversion=1.0-SNAPSHOT -Dpackaging=jar -Dfile=/path/to/jarfile

 4. Put the following in your pom.xml

    	<dependency>
		<groupId>com.codestuffs.clojure</groupId>
		<artifactId>swank-clojure</artifactId>
		<version>1.0-SNAPSHOT</version>
    	</dependency>

#### Nailgun

The clojure:nailgun goal requires a recent version of vimclojure as a
dependency. Unfortunatly, this library is currently not available in
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
</table>


### Support

Join the discussion mailing list at:

http://groups.google.com/group/clojure-maven-plugin

