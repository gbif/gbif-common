package org.gbif.utils.file.spreadsheet;

import java.io.Flushable;
import java.io.IOException;
import java.util.List;

/**
 * Interface for accepting the rows from an Excel/LibreOffice spreadsheet.
 */
public interface SpreadsheetConsumer extends Flushable {
  void write(List<String> row) throws IOException;

  long getCount();
}
