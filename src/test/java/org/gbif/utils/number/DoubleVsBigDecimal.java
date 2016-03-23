package org.gbif.utils.number;

import java.math.BigDecimal;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 * Set of tests to illustrate the issue with double and how some problems can be solved with BigDecimal.
 * BigDecimal is specially useful to work with base 10 type of data (money, metric measures).
 *
 */
public class DoubleVsBigDecimal {

  @Test
  public void testOneTenthAdditions(){

    double oneInDouble = 0.1+0.1+0.1+0.1+0.1+0.1+0.1+0.1+0.1+0.1;
    assertFalse(oneInDouble == 1d); //oneInDouble value: 0.9999999999999999

    // Try the same with BigDecimal
    BigDecimal zeroPointOne = new BigDecimal("0.1");
    BigDecimal currentValue = zeroPointOne;
    for(int i=0;i<9;i++){
      currentValue = currentValue.add(zeroPointOne);
    }
    assertEquals(currentValue, new BigDecimal("1.0"));
    assertTrue(currentValue.doubleValue() == 1d);
  }

  @Test
  /**
   * Example from https://en.wikipedia.org/wiki/Floating_point#Accuracy_problems
   * Simply demonstrates that (a + b) + c is not necessarily equal to a + (b + c) using double.
   */
  public void testAdditionAssociativity(){
    double a = 1234.567, b = 45.67834, c = 0.0004;

    double t1 = (a + b) + c; //1280.2457399999998
    double t2 = a + (b + c); //1280.24574
    assertFalse(t1 == t2);

    // Try the same with BigDecimal
    BigDecimal a2 = new BigDecimal("1234.567"), b2 = new BigDecimal("45.67834"), c2 = new BigDecimal("0.0004");

    //BigDecimla is immutable
    BigDecimal t3 = a2.add(b2);
    t3 = t3.add(c2);

    BigDecimal t4 = b2.add(c2);
    t4 = a2.add(t4);

    assertEquals(t3, t4);
    assertEquals(new BigDecimal("1280.24574"), t3);
  }
}
