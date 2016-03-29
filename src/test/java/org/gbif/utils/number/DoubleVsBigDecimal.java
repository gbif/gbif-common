package org.gbif.utils.number;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 * Set of tests to illustrate the issue with double and how some problems can be solved with {@link BigDecimal}.
 * {@link BigDecimal} is specially useful to work with base 10 type of data (money, metric measures).
 *
 * Be aware that {@link BigDecimal} requires more memory to work with (it's an Object) and it's also slower on
 * computations (it's an immutable Object).
 *
 */
public class DoubleVsBigDecimal {

  /**
   * Demonstrates that the sum of 1/10 (10 times) is not exactly equals to 1d using double.
   */
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

  /**
   * Demonstrates the effect of Double approximations on equality.
   */
  @Test
  public void testDoubleApproximation(){
    assertTrue(999199.1231231235 == 999199.1231231236);
  }

  /**
   * Demonstrates the possible issue with the default rounding mode of NumberFormat (RoundingMode.HALF_EVEN).
   * This is a modified version from the blog post:
   * https://blogs.oracle.com/CoreJavaTechTips/entry/the_need_for_bigdecimal
   *
   * No idea why the blog says "why does 90.045 round down to 90.04 and not up to 90.05" because it is not the case.
   */
  @Test
  public void testRoundingUsingFormat(){

    //Simple default DecimalFormat to keep only 2 decimals
    //this simply demonstrates the default rounding behavior (RoundingMode.HALF_EVEN)
    NumberFormat myFormatter = new DecimalFormat("##.##");
    assertEquals("90.05", myFormatter.format(90.045)); //90.045
    assertEquals("90.14", myFormatter.format(90.145));
    assertEquals("90.25", myFormatter.format(90.245));
    assertEquals("90.34", myFormatter.format(90.345));

    //same thing with NumberFormat for Currency
    NumberFormat moneyFormatter = NumberFormat.getCurrencyInstance();
    assertTrue(moneyFormatter.format(90.045).contains("90.05"));
    assertTrue(moneyFormatter.format(90.145).contains("90.14"));
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
