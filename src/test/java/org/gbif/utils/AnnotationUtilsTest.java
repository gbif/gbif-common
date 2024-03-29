/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    @Deprecated private int a;
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
