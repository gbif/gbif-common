package org.gbif.utils.number;

import java.math.BigDecimal;

import com.google.common.base.Preconditions;

/**
 * Utility class to work with BigDecimal
 */
public class BigDecimalUtils {

  private BigDecimalUtils(){}

  /**
   * Convert a double to a BigDecimal
   * @param value non-null value to convert into a BigDecimal.
   * @param stripTrailingZeros should the trailing zero(s) be removed? e.g. 25.0 would become 25
   * @return
   */
  public static BigDecimal fromDouble(Double value, boolean stripTrailingZeros){

    Preconditions.checkNotNull(value);

    //safer to create a BigDecimal from String than Double
    BigDecimal bd = new BigDecimal(Double.toString(value));
    if(stripTrailingZeros){
      return bd.stripTrailingZeros();
    }
    return bd;
  }

}
