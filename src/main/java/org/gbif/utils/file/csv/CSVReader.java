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
package org.gbif.utils.file.csv;

import org.gbif.utils.file.ClosableReportingIterator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.text.StrTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class CSVReader implements ClosableReportingIterator<String[]> {

  private static final Logger LOG = LoggerFactory.getLogger(CSVReader.class);
  public final int headerRows;
  public final String encoding;
  public final String delimiter;
  public final Character quoteChar;
  public final String[] header;
  private final StrTokenizer tokenizer;
  private String row;
  private int rows;
  private int readRows;
  private final int emptyLinesCacheLimit;
  private final Map<Integer, String> emptyLines;
  private final BufferedReader br;
  private boolean rowError;
  private String errorMessage;
  private Exception exception;

  public CSVReader(File source, String encoding, String delimiter, Character quotes, Integer headerRows)
          throws IOException {
    this(new FileInputStream(source), encoding, delimiter, quotes, headerRows);
  }

  public CSVReader(InputStream stream, String encoding, String delimiter, Character quotes, Integer headerRows)
          throws IOException {
    this(stream, encoding, delimiter, quotes, headerRows, 1000);
  }

  public CSVReader(InputStream stream, String encoding, String delimiter, Character quotes, Integer headerRows, int emptyLineCache)
          throws IOException {
    this.emptyLinesCacheLimit = emptyLineCache;
    if (emptyLineCache > 0) {
      this.emptyLines = new ConcurrentHashMap<>(emptyLineCache);
    } else {
      emptyLines = null;
    }
    this.rows = 0;
    this.readRows = 0;
    this.delimiter = delimiter;
    this.encoding = encoding;
    this.quoteChar = quotes;
    this.headerRows = headerRows == null || headerRows < 0 ? 0 : headerRows;
    tokenizer = new StrTokenizer();
    tokenizer.setDelimiterString(delimiter);
    if (quotes != null) {
      tokenizer.setQuoteChar(quotes);
    }
    tokenizer.setIgnoreEmptyTokens(false);
    tokenizer.reset();
    InputStreamReader reader = new InputStreamReader(stream, encoding);
    br = new BufferedReader(reader);
    row = br.readLine();
    // parse header row
    if (row == null) {
      header = null;
    } else {
      tokenizer.reset(row);
      header = tokenizer.getTokenArray();
    }
    // skip initial header rows?
    while (headerRows != null && headerRows > 0) {
      headerRows--;
      row = br.readLine();
    }
  }

  /**
   * Get the header, or null if none
   */
  public String[] getHeader() {
    return header;
  }

  @Override
  public void close() {
    try {
      br.close();
    } catch (IOException e) {
      LOG.debug("Exception caught", e);
    }
  }

  /**
   * @return the current line number of the String[] iterator
   */
  public int currLineNumber() {
    return rows;
  }

  /**
   * @return a set of the line numbers of the firsts empty rows found in the file
   */
  public Set<Integer> getEmptyLines() {
    return emptyLines == null ? new HashSet<>() : emptyLines.keySet();
  }

  /**
   * @return the number of rows of data that were correctly read from the file
   */
  public int getReadRows() {
    return readRows;
  }

  /*
   * (non-Javadoc)
   * @see java.util.Iterator#hasNext()
   */
  @Override
  public boolean hasNext() {
    return row != null;
  }

  /*
   * (non-Javadoc)
   * @see java.util.Iterator#next()
   */
  @Override
  public String[] next() {
    if (row == null) {
      return null;
    }
    tokenizer.reset(row);
    resetReportingIterator();
    try {
      row = br.readLine();
      rows++;
      // skip empty lines
      while (row != null && row.length() == 0) {
        // save line number of empty line
        if (emptyLines != null && emptyLines.size() < emptyLinesCacheLimit) {
          emptyLines.put(rows + headerRows + 1, "");
        }
        row = br.readLine();
        rows++;
      }
      readRows++;
    } catch (IOException e) {
      LOG.debug("Exception caught", e);
      rowError = true;
      exception = e;

      // construct error message showing exception and problem row
      StringBuilder msg = new StringBuilder();
      msg.append("Exception caught: ");
      msg.append(e.getMessage());
      if (!Strings.isNullOrEmpty(row)) {
        msg.append("\n");
        msg.append("Row: ");
        msg.append(row);
      }
      errorMessage = msg.toString();

      // ensure iteration terminates
      row = null;
    }
    return tokenizer.getTokenArray();
  }

  /**
   * Reset all reporting parameters.
   */
  private void resetReportingIterator() {
    rowError = false;
    exception = null;
    errorMessage = null;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Remove not supported");
  }

  @Override
  public boolean hasRowError() {
    return rowError;
  }

  @Override
  public String getErrorMessage() {
    return errorMessage;
  }

  @Override
  public Exception getException() {
    return exception;
  }
}
