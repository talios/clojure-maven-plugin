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

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Collections;

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
