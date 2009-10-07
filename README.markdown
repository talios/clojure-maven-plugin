Welcome to the clojure-maven-plugin plugin for Apache Maven 2.

## Compiling clojure sources

To use this plugin and start compiling clojure code as part of your maven build, add the following:

    <plugins>
      <plugin>
        <groupId>com.theoryinpractise</groupId>
        <artifactId>clojure-maven-plugin</artifactId>
        <version>1.0</version>
      </plugin>
    </plugins>

Without any configuration, the clojure-maven-plugin will compile *ANY* namespaces in ./src/main/clojure/*.clj and
./src/test/clojure/*.clj.

To change, or add additional source directories you can add the following configuration:

    <configuration>
      <sourceDirectories>
        <sourceDirectory>src/main/clojure</sourceDirectory>
      </sourceDirectories>
      <testSourceDirectories>
        <testSourceDirectory>src/test/clojure</testSourceDirectory>
      </testSourceDirectories>
    </configuration>

The plugin also provides a clojure:run and clojure:test goal, which will run clojure scripts defined by:

    <configuration>
      <script>src/test/clojure/com/jobsheet/jetty.clj</script>
      <testScript>src/test/clojure/com/jobsheet/test.clj</testScript>
    </configuration>

If you wish to limit or filter out namespaces during your compile, simply add a <namespaces>
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

Enjoy.

## clojure:repl and clojure:swank targets

clojure-maven-plugin supports two targets indented to make it easier
to developers to run interactive clojure shells in the context of
maven projects. This means that all dependencies in a project's
runtime and test scopes will be automatically added to the classpath
and available for experimentation. 

<table>
	<tr>
		<th>Goal</th>
		<th>Description</th>
	</tr>
	<tr>
		<td>clojure:repl</td>
		<td>
			Starts an interactive clojure REPL right on the command line.
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
</table>

### Dependencies

In order to run clojure:repl or clojure:swank, your project needs to
have a recent (1.0 or later) version of clojure as a dependency in
pom.xml.

Also, the clojure:swank target requires a recent version of
swank-clojure as a dependency. Unfortunatly, this library is currently
not available in the central maven repository, and has to be
downloaded and installed manually: 

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

### Configuration

The clojure:swank targets support the following options that can be
configured as system properties: 

<table>
	<tr>
		<th>Property</th>
		<th>Default value</th>
		<th>Description</th>
	</tr>
	<tr>
		<td>clojure.swank.port</td>
		<td>4005</td>
		<td>
			Only applicable for the <code>clojureshell:swank</code> target.
			The port number that the Swank server should listen to.
		</td>
	</tr>
	<tr>
		<td>clojure.swank.protocolVersion</td>
		<td>2009-09-14</td>
		<td>
			Only applicable for the <code>clojureshell:swank</code> target.
			Specifies the version of the swank protocol. 
		</td>
	</tr>
</table>




