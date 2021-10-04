/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
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
package org.gbif.utils.number;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 * Tests for BigDecimal utility class
 *
 */
public class BigDecimalUtilsTest {

  /**
   * This test is simply to illustrate why we use the BigDecimal(String) constructor.
   * new BigDecimal(23.1) equals 23.10000000000000142108547152020037174224853515625
   */
  @Test
  public void testBigDecimalConstructor() {
    assertNotEquals(new BigDecimal("23.1"), new BigDecimal(23.1d));
  }

  @Test
  public void testBigDecimalConstructorNull() {
    assertThrows(NullPointerException.class, () -> BigDecimalUtils.fromDouble(null, false));
  }

  /**
   * This test is simply to show why we are not using BigDecimal stripTrailingZeros function.
   */
  @Test
  public void demonstrateBigDecimalStripTrailingZerosEquality() {
    assertNotEquals(new BigDecimal("500.0").stripTrailingZeros(), new BigDecimal("500"));

    // but, compareTo function will work as expected
    assertEquals(0, new BigDecimal("500").compareTo(new BigDecimal("500.0").stripTrailingZeros()));
  }

  @Test
  public void testFromDouble() {
    assertEquals(new BigDecimal("23.4"), BigDecimalUtils.fromDouble(23.4, true));
    assertEquals(new BigDecimal("23.4"), BigDecimalUtils.fromDouble(23.4, false));

    assertEquals(new BigDecimal("23"), BigDecimalUtils.fromDouble(23d, true));
    assertEquals(new BigDecimal("23.0"), BigDecimalUtils.fromDouble(23d, false));

    assertEquals(new BigDecimal("23"), BigDecimalUtils.fromDouble(23.0, true));
    assertEquals(new BigDecimal("23.0"), BigDecimalUtils.fromDouble(23.0, false));

    assertEquals(new BigDecimal("23"), BigDecimalUtils.fromDouble(23.0000000000, true));
    assertEquals(new BigDecimal("23.0"), BigDecimalUtils.fromDouble(23.0000000000, false));

    assertEquals(
        new BigDecimal("23.000000000001"), BigDecimalUtils.fromDouble(23.000000000001, true));
    assertEquals(
        new BigDecimal("23.000000000001"), BigDecimalUtils.fromDouble(23.000000000001, false));

    assertEquals(new BigDecimal("500"), BigDecimalUtils.fromDouble(500d, true));
    assertEquals(new BigDecimal("500.0"), BigDecimalUtils.fromDouble(500d, false));

    assertEquals(new BigDecimal("50000"), BigDecimalUtils.fromDouble(50000d, true));
    assertEquals(new BigDecimal("50000.0"), BigDecimalUtils.fromDouble(50000d, false));

    assertEquals(new BigDecimal("50000.01"), BigDecimalUtils.fromDouble(50000.01, true));
    assertEquals(new BigDecimal("50000.01"), BigDecimalUtils.fromDouble(50000.01, false));
  }
}
