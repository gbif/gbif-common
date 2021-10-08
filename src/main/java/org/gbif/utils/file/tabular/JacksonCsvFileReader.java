/*
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
import java.text.ParseException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import static org.gbif.utils.file.tabular.JacksonUtils.buildCsvSchema;

/**
 * Internal {@link TabularDataFileReader} implementation backed by Jackson CSV.
 */
class JacksonCsvFileReader implements TabularDataFileReader<List<String>> {

  private final MappingIterator<List<String>> it;
  private List<String> headerLine;

  private long lastLineNumber = 0;
  private long recordNumber = 0;

  JacksonCsvFileReader(
      Reader reader,
      char delimiterChar,
      String endOfLineSymbols,
      Character quoteChar,
      boolean headerLineIncluded)
      throws IOException {
    this(reader, delimiterChar, endOfLineSymbols, quoteChar, headerLineIncluded, null);
  }

  /**
   * package protected constructor. Use {@link TabularFiles} to get instances.
   *
   * @param reader
   * @param delimiterChar
   * @param endOfLineSymbols
   * @param quoteChar          Nullable.
   * @param headerLineIncluded
   * @param lineToSkipBeforeHeader Nullable. How many line(s) is required to skip before reading the header or the data.
   * @throws IOException
   */
  JacksonCsvFileReader(
      Reader reader,
      char delimiterChar,
      String endOfLineSymbols,
      Character quoteChar,
      boolean headerLineIncluded,
      Integer lineToSkipBeforeHeader)
      throws IOException {

    Objects.requireNonNull(reader, "reader shall be provided");
    Objects.requireNonNull(endOfLineSymbols, "endOfLineSymbols shall be provided");

    CsvMapper mapper = new CsvMapper();
    mapper.enable(CsvParser.Feature.WRAP_AS_ARRAY);

    CsvSchema schema = buildCsvSchema(delimiterChar, endOfLineSymbols, quoteChar);

    it = mapper.readerFor(List.class).with(schema).readValues(reader);

    // if we have to skip lines before the header line
    for (int line = 0; line < Optional.ofNullable(lineToSkipBeforeHeader).orElse(0); line++) {
      it.next();
    }

    // ensure to pull the header line if we need to
    if (headerLineIncluded && it.hasNext()) {
      headerLine = it.next();
    }
  }

  @Override
  public List<String> getHeaderLine() throws IOException {
    return headerLine;
  }

  @Override
  public long getLastRecordLineNumber() {
    return lastLineNumber;
  }

  @Override
  public long getLastRecordNumber() {
    return recordNumber;
  }

  @Override
  public List<String> read() throws IOException, ParseException {
    try {
      while (it.hasNext()) {
        // get the current line number before we read the next record
        lastLineNumber = it.getCurrentLocation().getLineNr();
        List<String> row = it.next();
        // an empty line is returned as a list of one element, check if the element is empty before
        // skipping
        if (row.size() != 1 || StringUtils.isNotBlank(row.get(0))) {
          recordNumber++;
          return row;
        }
      }
    } catch (RuntimeException rtEx) {
      if (JsonParseException.class == rtEx.getCause().getClass()) {
        throw new ParseException(
            rtEx.getMessage(), ((JsonParseException) rtEx.getCause()).getLocation().getLineNr());
      } else {
        throw rtEx;
      }
    }
    return null;
  }

  @Override
  public void close() throws IOException {
    it.close();
  }
}
