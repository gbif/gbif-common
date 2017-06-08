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
    return newTabularFileReader(reader, delimiterChar, endOfLineSymbols, quoteChar, headerLine, null);
  }

  /**
   * Get a new TabularDataFileReader.
   *
   * @param reader
   * @param delimiterChar
   * @param endOfLineSymbols
   * @param quoteChar Nullable
   * @param headerLine do we expect the first line before the data to be a header line
   * @param lineToSkipBeforeHeader Nullable. How many line(s) is required to skip in the file before reading the header or the data.
   *                               This can be used to skip a comment block but if there is a header line, the comment block shall be before the header.
   * @return
   */
  public static TabularDataFileReader<List<String>> newTabularFileReader(Reader reader, char delimiterChar,
                                                                         String endOfLineSymbols, Character quoteChar, boolean headerLine,
                                                                         Integer lineToSkipBeforeHeader) throws IOException {

    Preconditions.checkNotNull(reader, "A Reader must be provided");
    Preconditions.checkNotNull(endOfLineSymbols, "A endOfLineSymbols must be provided");
    return new JacksonCsvFileReader(reader, delimiterChar, endOfLineSymbols, quoteChar, headerLine, lineToSkipBeforeHeader);
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
