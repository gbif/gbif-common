package org.gbif.utils;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit test related tp {@link AnnotationUtils}
 */
public class AnnotationUtilsTest {

  private enum TestEnum {
    A,
    @Deprecated
    B
  }

  private static class TestClass {
    @Deprecated
    private int a;
    private int b;
  }

  @Test
  public void testIsFieldDeprecated() {
    assertFalse(AnnotationUtils.isFieldDeprecated(TestEnum.class, "A"));
    assertTrue(AnnotationUtils.isFieldDeprecated(TestEnum.class, "B"));

    assertFalse(AnnotationUtils.isFieldDeprecated(TestEnum.class, "C"));

    assertTrue(AnnotationUtils.isFieldDeprecated(TestClass.class, "a"));
    assertFalse(AnnotationUtils.isFieldDeprecated(TestClass.class, "b"));

    assertFalse(AnnotationUtils.isFieldDeprecated(TestClass.class, "c"));
  }
}
