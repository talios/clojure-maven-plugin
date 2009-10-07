package com.theoryinpractise.clojure;

import java.util.*;
import junit.framework.*;

public class ClojureReplMojoTest extends TestCase {
    
	public void testJLineAvailable() throws Exception {
		ClojureReplMojo mojo = new ClojureReplMojo();
		assertTrue(mojo.isJLineAvailable(Arrays.asList("test/jline-0.9.94.jar")));
		assertTrue(mojo.isJLineAvailable(Arrays.asList("test/jline-0.9.95-SNAPSHOT.jar")));
		assertTrue(mojo.isJLineAvailable(Arrays.asList("test/test-0.1.jar", "test/jline-0.9.94.jar")));
		assertFalse(mojo.isJLineAvailable(Arrays.asList("test/test-0.1.jar")));
		assertFalse(mojo.isJLineAvailable(Collections.<String>emptyList()));
		assertFalse(mojo.isJLineAvailable(null));
	}
}