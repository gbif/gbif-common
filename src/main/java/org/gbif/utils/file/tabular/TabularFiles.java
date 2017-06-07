package org.gbif.utils.file.tabular;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.common.base.Preconditions;

/**
 * Static utility methods related to {@link TabularDataFileReader} instances.
 */
public class TabularFiles {

  /**
   * Get a new TabularDataFileReader.
   *
   * @param reader
   * @param delimiterChar
   * @param endOfLineSymbols
   * @param quoteChar nullable
   * @param headerLine
   * @return
   */
  public static TabularDataFileReader<List<String>> newTabularFileReader(Reader reader, char delimiterChar,
                                                           String endOfLineSymbols, Character quoteChar, boolean headerLine) throws IOException {

    Preconditions.checkNotNull(reader, "A Reader must be provided");
    Preconditions.checkNotNull(endOfLineSymbols, "A endOfLineSymbols must be provided");
    return new JacksonCsvFileReader(reader, delimiterChar, endOfLineSymbols, quoteChar, headerLine);
  }

  /**
   * Get a new TabularDataFileReader using default quote char and default endOfLineSymbols.
   *
   * @param reader
   * @param delimiterChar
   * @param headerLine
   */
  public static TabularDataFileReader<List<String>> newTabularFileReader(Reader reader, char delimiterChar,
                                                                         boolean headerLine) throws IOException {
    return new JacksonCsvFileReader(reader, delimiterChar, new String(CsvSchema.DEFAULT_LINEFEED),
            CsvSchema.DEFAULT_QUOTE_CHAR, headerLine);
  }


}
