package clojure;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import static org.fest.assertions.Assertions.assertThat;
import org.junit.Test;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.util.List;

public class NamespaceDiscoveryTest {

    @Test
    public void testNamespaceDiscovery() throws MojoExecutionException {

        NamespaceDiscovery namespaceDiscovery = new NamespaceDiscovery(mock(Log.class));

        List<String> namespaces = namespaceDiscovery.discoverNamespacesIn(new File("src/test/resources"));

        assertThat(namespaces)
                .isNotNull()
                .isNotEmpty()
                .contains("test")
                .contains("com.test")
                .contains("test.test3");

        System.out.println(namespaces);

    }

    @Test
    public void testNamespaceFiltering() throws MojoExecutionException {

        NamespaceDiscovery namespaceDiscovery = new NamespaceDiscovery(mock(Log.class));

        assertThat(namespaceDiscovery.discoverNamespacesIn(new String[]{"test.*"}, new File("src/test/resources")))
                .isNotNull()
                .isNotEmpty()
                .hasSize(2);

        assertThat(namespaceDiscovery.discoverNamespacesIn(new String[]{"!com.*", ".*"}, new File("src/test/resources")))
                .isNotNull()
                .isNotEmpty()
                .hasSize(2);

        assertThat(namespaceDiscovery.discoverNamespacesIn(new String[]{"test"}, new File("src/test/resources")))
                .isNotNull()
                .isNotEmpty()
                .hasSize(1);

        assertThat(namespaceDiscovery.discoverNamespacesIn(new String[]{"com.*"}, new File("src/test/resources")))
                .isNotNull()
                .isNotEmpty()
                .hasSize(1);


    }

}
