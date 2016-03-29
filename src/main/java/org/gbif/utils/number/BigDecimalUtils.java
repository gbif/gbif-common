package org.gbif.utils.number;

import java.math.BigDecimal;

import com.google.common.base.Preconditions;

/**
 * Utility class to work with BigDecimal.
 * See DoubleVsBigDecimal test class for more details about {@link BigDecimal}
 */
public class BigDecimalUtils {

  private BigDecimalUtils(){}

  /**
   * Convert a double to a BigDecimal.
   *
   * @param value non-null value to convert into a BigDecimal.
   * @param stripTrailingZeros should the trailing zero(s) be removed? e.g. 25.0 would become 25
   * @return instance of BigDecimal
   */
  public static BigDecimal fromDouble(Double value, boolean stripTrailingZeros){

    Preconditions.checkNotNull(value);

    //safer to create a BigDecimal from String than Double
    BigDecimal bd = new BigDecimal(Double.toString(value));
    if(stripTrailingZeros){
      //we do not use stripTrailingZeros() simply because it plays with the scale and returns a BigDecimal
      //that is numerically equals. see test in BigDecimalUtilsTest
      if(bd.remainder(BigDecimal.ONE).movePointRight(bd.scale()).intValue() == 0 ){
        bd = new BigDecimal(value.intValue());
      }
    }
    return bd;
  }

}
