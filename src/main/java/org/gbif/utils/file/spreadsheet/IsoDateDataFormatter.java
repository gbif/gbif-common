package org.gbif.utils.file.spreadsheet;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;

import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Override Excel formats to use ISO dates, but leave everything else as it was.
 */
public class IsoDateDataFormatter extends DataFormatter {

  private DateFormat dateDateformat = new SimpleDateFormat("yyyy-MM-DD");
  private DateFormat dateTimeDateformat = new SimpleDateFormat("yyyy-MM-DD'T'HH:mm:ss'Z'");

  public IsoDateDataFormatter() {
    super(Locale.UK);
  }

  private Format getDateFormat(String pFormatStr) {
    // Within [] is an elapsed duration (hours?)

    // Just decide whether a time is present
    if (pFormatStr.contains("h")) {
      return dateTimeDateformat;
    } else {
      return dateDateformat;
    }
  }

  /**
   * Formats a date to ISO 8601, otherwise hands over to the superclass.
   */
  public String formatRawCellContents(double value, int formatIndex, String formatString) {
    // Is it a date?
    if (DateUtil.isADateFormat(formatIndex, formatString) && DateUtil.isValidExcelDate(value)) {
      Format dateFormat = getDateFormat(formatString);
      Date d = DateUtil.getJavaDate(value, false);
      return dateFormat.format(d);
    }

    return super.formatRawCellContents(value, formatIndex, formatString, false);
  }
}
