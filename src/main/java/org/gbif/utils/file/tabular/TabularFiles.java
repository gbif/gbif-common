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
   * Get a new TabularDataFileReader using default quote char (") and default endOfLineSymbols (\n).
   * Usage:
   * <pre>
   * {@code
   * try (TabularDataFileReader<List<String>> reader = TabularFiles.newTabularFileReader(
  Files.newBufferedReader(Paths.get("/tmp/test.csv"), StandardCharsets.UTF_8), ',', true)) {
  ...
  }
   * }
   * </pre>
   *
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
