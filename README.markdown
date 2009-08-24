Welcome to the clojure-maven-plugin plugin for Apache Maven 2.

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

Enjoy.
