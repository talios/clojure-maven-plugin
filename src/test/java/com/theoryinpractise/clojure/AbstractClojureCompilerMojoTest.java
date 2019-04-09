package com.theoryinpractise.clojure;

import com.google.common.collect.ImmutableMap;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import static com.theoryinpractise.clojure.AbstractClojureCompilerMojo.getDefaultJavaHomeExecutable;
import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(Theories.class)
public class AbstractClojureCompilerMojoTest {

  @DataPoint public static String java_home1 = "TEST_ROOT";
  @DataPoint public static String java_home2 = "TEST_ROOT/";

  @Theory
  public void testJavaExecutable(String java_home) {
    assertThat(getDefaultJavaHomeExecutable(ImmutableMap.of("JAVA_HOME", java_home))).isEqualTo("TEST_ROOT/bin/java");
  }

}
