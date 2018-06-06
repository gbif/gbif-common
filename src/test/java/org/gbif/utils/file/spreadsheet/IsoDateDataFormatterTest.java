package org.gbif.utils.file.spreadsheet;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IsoDateDataFormatterTest {

  @Test
  public void testIsoDateDateFormatter() {
    IsoDateDataFormatter iddf = new IsoDateDataFormatter();

    assertEquals("1990-01-02", iddf.formatRawCellContents(32875.0, 0, "mm/dd/yy"));
    assertEquals("1990-01-05T17:00:00Z", iddf.formatRawCellContents(32878.708333333336, 0, "hh:mm:ss mm/dd/yy"));
  }
}
